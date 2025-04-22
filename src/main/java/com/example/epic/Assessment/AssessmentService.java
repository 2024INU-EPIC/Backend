package com.example.epic.Assessment;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessageImageContentItem;
import com.azure.ai.openai.models.ChatMessageImageUrl;
import com.azure.ai.openai.models.ChatMessageTextContentItem;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.KeyCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AssessmentService {

    //========================================
    //  1. 환경변수 및 클라이언트
    //========================================
    @Value("${SPEECH_KEY}")
    private String azureSpeechApiKey;

    @Value("${SERVICE_REGION}")
    private String azureSpeechApiRegion;

    @Value("${OPENAI_API_KEY}")
    private String openAiApiKey;

    @Value("${AZURE_OPENAI_MODEL_ID}")
    private String openAiModelId;

    private OpenAIAsyncClient openAIClient;

    // ObjectMapper 캐싱 (JSON 파싱 최소화를 위해)
    private static final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void initializeOpenAIClient() {
        openAIClient = new OpenAIClientBuilder()
                .credential(new KeyCredential(openAiApiKey))
                .buildAsyncClient();
    }

    //========================================
    //  2. 공통 헬퍼 메서드
    //========================================

    /**
     * GPT 응답 문자열에서 외부 따옴표와 이스케이프 문자를 제거하여 순수한 문자열로 반환합니다.
     */
    private String cleanGptResponse(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        // 외부 따옴표가 감싸고 있다면 제거
        if (response.startsWith("\"") && response.endsWith("\"")) {
            response = response.substring(1, response.length() - 1);
        }
        // \n과 \" 등의 이스케이프 시퀀스 복원
        response = response.replace("\\n", "\n").replace("\\\"", "\"");
        return response;
    }

    /**
     * GPT의 응답 문자열에서 JSON 부분만 추출합니다.
     */
    private String extractJsonFromGptResponse(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }
        return response;
    }

    /**
     * Azure Speech API 원시 JSON을 파싱하여 간소화된 평가 결과 ObjectNode로 반환.
     */
    private ObjectNode simplifySpeechAssessment(String speechApiRawJson) {
        ObjectNode simplifiedJson = mapper.createObjectNode();
        try {
            JsonNode root = mapper.readTree(speechApiRawJson);

            // 1) 전사된 텍스트 추출
            String recognizedText = root.path("DisplayText").asText();
            if (recognizedText.isEmpty() && root.path("NBest").isArray() && root.path("NBest").size() > 0) {
                recognizedText = root.path("NBest").get(0).path("Display").asText();
            }
            simplifiedJson.put("UserResponse", recognizedText);

            // 2) 발음 평가 정보 설정
            if (root.path("NBest").isArray() && root.path("NBest").size() > 0) {
                JsonNode pronAssessment = root.path("NBest").get(0).path("PronunciationAssessment");
                simplifiedJson.set("PronunciationAssessment", pronAssessment);
            }

            // 3) IssueWords 추출 (정확도 낮은 단어/음소)
            ArrayNode issueWords = mapper.createArrayNode();
            JsonNode wordsNode = root.path("NBest").get(0).path("Words");
            if (wordsNode.isArray()) {
                for (JsonNode wordNode : wordsNode) {
                    double wordAcc = wordNode.path("PronunciationAssessment").path("AccuracyScore").asDouble();
                    String errorType = wordNode.path("PronunciationAssessment").path("ErrorType").asText();
                    if (wordAcc <= 80 || !"None".equalsIgnoreCase(errorType)) {
                        ObjectNode issueWord = mapper.createObjectNode();
                        issueWord.put("word", wordNode.path("Word").asText());
                        issueWord.put("AccuracyScore", wordAcc);
                        issueWord.put("ErrorType", errorType);

                        ArrayNode lowScorePhonemes = mapper.createArrayNode();
                        JsonNode phonemesNode = wordNode.path("Phonemes");
                        if (phonemesNode.isArray()) {
                            for (JsonNode phonemeNode : phonemesNode) {
                                double phonemeAcc = phonemeNode.path("PronunciationAssessment").path("AccuracyScore").asDouble();
                                if (phonemeAcc <= 80) {
                                    ObjectNode phonemeObj = mapper.createObjectNode();
                                    phonemeObj.put("phoneme", phonemeNode.path("Phoneme").asText());
                                    phonemeObj.put("AccuracyScore", phonemeAcc);
                                    lowScorePhonemes.add(phonemeObj);
                                }
                            }
                        }
                        issueWord.set("LowScorePhonemes", lowScorePhonemes);
                        issueWords.add(issueWord);
                    }
                }
            }
            simplifiedJson.set("IssueWords", issueWords);
        } catch (Exception e) {
            ObjectNode errorJson = mapper.createObjectNode();
            errorJson.put("error", "Failed to simplify speech result: " + e.getMessage());
            return errorJson;
        }
        return simplifiedJson;
    }

    /**
     * Speech 인식 및 발음 평가를 비동기 방식으로 수행.
     * – Blocking 호출을 제거하고, CompletableFuture 체인을 활용하여 결과를 ObjectNode로 반환합니다.
     */
    private CompletableFuture<ObjectNode> performPronunciationAssessmentAsync(String referenceScript, String audioFilePath, String failureMessage) {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureSpeechApiKey, azureSpeechApiRegion);
        // 영어 발음 평가를 위해 언어 설정을 en-US로 설정
        speechConfig.setSpeechRecognitionLanguage("en-US");
        AudioConfig audioConfig = AudioConfig.fromWavFileInput(audioFilePath);
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);

        PronunciationAssessmentConfig pronunciationConfig = new PronunciationAssessmentConfig(
                referenceScript,
                PronunciationAssessmentGradingSystem.HundredMark,
                PronunciationAssessmentGranularity.Phoneme,
                true
        );
        pronunciationConfig.enableProsodyAssessment();
        pronunciationConfig.applyTo(recognizer);

        // SDK의 recognizeOnceAsync()를 CompletableFuture로 변환
        CompletableFuture<SpeechRecognitionResult> recognitionFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return recognizer.recognizeOnceAsync().get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        return recognitionFuture
                .thenApply(result -> {
                    String rawJson;
                    if (result.getReason() == ResultReason.RecognizedSpeech) {
                        rawJson = result.getProperties().getProperty(PropertyId.SpeechServiceResponse_JsonResult);
                    } else {
                        rawJson = failureMessage + result.getReason();
                    }
                    return simplifySpeechAssessment(rawJson);
                })
                .whenComplete((res, ex) -> {
                    // 리소스 명시적 해제
                    recognizer.close();
                    speechConfig.close();
                    audioConfig.close();
                });
    }

    /**
     * (스크립트 없는) 말하기 평가를 비동기 방식으로 수행하며, 평가 결과를 ObjectNode로 반환.
     */
    public CompletableFuture<ObjectNode> evaluateSpeechWithoutScriptAsync(String audioFilePath) {
        return performPronunciationAssessmentAsync("", audioFilePath, "Scriptless speaking evaluation failed: ");
    }

    /**
     * [파트 1] (스크립트 있는) 발음 평가를 수행하고 최종 결과를 JSON 문자열로 반환.
     */
    public CompletableFuture<String> evaluateSpeechPronunciationAsync(String referenceScript, String audioFilePath) {
        return performPronunciationAssessmentAsync(referenceScript, audioFilePath, "Pronunciation assessment failed: ")
                .thenApply(objectNode -> {
                    try {
                        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
                    } catch (Exception e) {
                        return "{\"error\": \"Failed to convert result to string: " + e.getMessage() + "\"}";
                    }
                });
    }

    /**
     * GPT 평가를 위한 비동기 요청.
     */
    private CompletableFuture<JsonNode> requestGptEvaluationAsync(List<ChatRequestMessage> chatMessages) {
        return CompletableFuture.supplyAsync(() -> {
            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
                    options.setMaxTokens(2048);

                    String response = openAIClient.getChatCompletions(openAiModelId, options)
                            .toFuture().get()  // CompletableFuture에서 blocking으로 호출
                            .getChoices().get(0).getMessage().getContent();

                    String cleanedResponse = cleanGptResponse(response);
                    String jsonPart = extractJsonFromGptResponse(cleanedResponse);

                    JsonNode parsed = mapper.readTree(jsonPart);

                    // 유효성 검증 추가 (예: 필수 키가 없으면 실패로 간주)
                    if (isValidGptEvaluation(parsed)) {
                        return parsed;
                    } else {
                        throw new Exception("GPT 응답에 필수 키 누락: " + jsonPart);
                    }

                } catch (Exception e) {
                    attempt++;
                    if (attempt >= maxRetries) {
                        ObjectNode errorNode = mapper.createObjectNode();
                        errorNode.put("error", "GPT evaluation failed after " + maxRetries + " attempts: " + e.getMessage());
                        return errorNode;
                    }
                    try {
                        Thread.sleep(500);  // 0.5초 딜레이 후 재시도
                    } catch (InterruptedException ignored) {}
                }
            }

            ObjectNode failNode = mapper.createObjectNode();
            failNode.put("error", "GPT evaluation failed unexpectedly");
            return failNode;
        });
    }

    // gpt 출력의 유효성을 검사하는 메서드
    private boolean isValidGptEvaluation(JsonNode node) {
        return node != null &&
                node.has("grammar") &&
                node.has("topic") &&
                node.has("vocabulary") &&
                node.has("suggestions");
    }

    /**
     * 기본 시스템 메시지(역할 지시 메시지)를 생성.
     * GPT가 추가 텍스트나 코드블럭 없이 오직 순수 JSON 객체만 반환하도록 명확한 지시를 내립니다.
     */
    private ChatRequestSystemMessage createDefaultSystemMessage() {
        String prompt = "You are an expert English instructor. Your primary task is to evaluate the provided speech transcription for grammar, topic coherence, and vocabulary usage. " +
                "Provide scores (0-100) for each category and output a JSON object with the following structure:\n" +
                "{\n" +
                "  \"grammar\": <grammar score>,\n" +
                "  \"topic\": <topic score>,\n" +
                "  \"vocabulary\": <vocabulary score>,\n" +
                "  \"suggestions\": {\n" +
                "    \"grammar\": \"<detailed Korean feedback on grammar>\",\n" +
                "    \"topic coherence\": \"<detailed Korean feedback on topic coherence>\",\n" +
                "    \"vocabulary\": \"<detailed Korean feedback on vocabulary usage>\",\n" +
                "    \"eval\": \"<overall summary evaluation in Korean>\"\n" +
                "  }\n" +
                "}\n" +
                "If an image is provided, analyze its overall context and scene to help understand the scenario but do not let this analysis affect the primary evaluation of the transcription. " +
                "Do not include any additional text, commentary, or markdown formatting. Only output the raw JSON object.";
        return new ChatRequestSystemMessage(prompt);
    }

    /**
     * 간소화된 Speech 평가 결과(ObjectNode)에서 전사 텍스트 추출.
     */
    private String extractTranscriptionFromSpeechResult(ObjectNode speechResult) {
        return speechResult.path("UserResponse").asText("");
    }

    /**
     * Azure 평가 결과와 GPT 평가 결과를 결합하여 최종 JSON 문자열로 반환.
     * – 이미 ObjectNode 형태의 Speech 결과를 재사용하여 중복 파싱을 피합니다.
     */
    private CompletableFuture<String> combineEvaluations(ObjectNode speechResult, CompletableFuture<JsonNode> gptEvaluationFuture) {
        return gptEvaluationFuture.thenApply(gptResultJson -> {
            ObjectNode combinedResult = mapper.createObjectNode();
            combinedResult.set("azureEvaluation", speechResult);
            combinedResult.set("gptEvaluation", gptResultJson);
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(combinedResult);
            } catch (Exception e) {
                return "Error combining evaluations: " + e.getMessage();
            }
        });
    }

    /**
     * 공통 GPT 평가 로직:
     * – Speech 평가 후, 추가 컨텐츠(텍스트/이미지)를 포함한 GPT 메시지 구성 및 평가 결과 결합.
     */
    private CompletableFuture<String> evaluateWithGpt(String audioFilePath,
                                                      List<com.azure.ai.openai.models.ChatMessageContentItem> additionalContentItems) {
        return evaluateSpeechWithoutScriptAsync(audioFilePath)
                .thenCompose(speechResult -> {
                    String recognizedTranscription = extractTranscriptionFromSpeechResult(speechResult);
                    List<ChatRequestMessage> messages = new ArrayList<>();
                    messages.add(createDefaultSystemMessage());

                    List<com.azure.ai.openai.models.ChatMessageContentItem> contentItems = new ArrayList<>();
                    // 전사 섹션 추가
                    contentItems.add(new ChatMessageTextContentItem("=== Transcription ===\n" + recognizedTranscription));

                    // 추가 컨텐츠가 있는 경우 별도의 섹션으로 추가
                    if (additionalContentItems != null && !additionalContentItems.isEmpty()) {
                        contentItems.add(new ChatMessageTextContentItem("=== Additional Context ==="));
                        contentItems.addAll(additionalContentItems);
                    }

                    messages.add(new ChatRequestUserMessage(contentItems));
                    return combineEvaluations(speechResult, requestGptEvaluationAsync(messages));
                });
    }

    //========================================
    //  3. 평가 메서드 (파트별)
    //========================================

    /**
     * [파트 2] Question 이미지 기반 평가.
     */
    public CompletableFuture<String> evaluateSpeechWithQuestionImageAsync(String audioFilePath, String questionImage) {
        List<com.azure.ai.openai.models.ChatMessageContentItem> additionalItems = new ArrayList<>();
        additionalItems.add(new ChatMessageImageContentItem(new ChatMessageImageUrl(questionImage)));
        return evaluateWithGpt(audioFilePath, additionalItems);
    }

    /**
     * [파트 3] Situation 텍스트 + Question 텍스트 기반 평가.
     */
    public CompletableFuture<String> evaluateSpeechWithSituationTextAsync(String audioFilePath, String situationText, String questionText) {
        List<com.azure.ai.openai.models.ChatMessageContentItem> additionalItems = new ArrayList<>();
        additionalItems.add(new ChatMessageTextContentItem("\nSituation: " + situationText));
        additionalItems.add(new ChatMessageTextContentItem("\nQuestion: " + questionText));
        return evaluateWithGpt(audioFilePath, additionalItems);
    }

    /**
     * [파트 4] Situation 이미지 + 텍스트 + Question 텍스트 기반 평가.
     */
    public CompletableFuture<String> evaluateSpeechWithSituationImageAsync(String audioFilePath, String situationImage,
                                                                           String situationText, String questionText) {
        List<com.azure.ai.openai.models.ChatMessageContentItem> additionalItems = new ArrayList<>();
        additionalItems.add(new ChatMessageTextContentItem("\nSituation Text: " + situationText));
        additionalItems.add(new ChatMessageTextContentItem("\nQuestion: " + questionText));
        additionalItems.add(new ChatMessageImageContentItem(new ChatMessageImageUrl(situationImage)));
        return evaluateWithGpt(audioFilePath, additionalItems);
    }

    /**
     * [파트 5] Question 텍스트 기반 평가.
     */
    public CompletableFuture<String> evaluateSpeechWithQuestionTextAsync(String audioFilePath, String questionText) {
        List<com.azure.ai.openai.models.ChatMessageContentItem> additionalItems = new ArrayList<>();
        additionalItems.add(new ChatMessageTextContentItem("\nQuestion: " + questionText));
        return evaluateWithGpt(audioFilePath, additionalItems);
    }
}