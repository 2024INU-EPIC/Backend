package com.example.epic.mocktest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
public class TTSController {

    @Value("${SPEECH_KEY}")
    private String key;

    @Value("${SERVICE_REGION}")
    private String region;

    @PostMapping("/api/tts")
    public ResponseEntity<byte[]> tts(@RequestBody Map<String, String> request) throws IOException, InterruptedException {
        String text = request.get("text");

        String ssml = """
        <speak version='1.0' xml:lang='en-US'>
          <voice name='en-US-EchoTurboMultilingualNeural'>%s</voice>
        </speak>
        """.formatted(text);

        HttpRequest azureRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://" + region + ".tts.speech.microsoft.com/cognitiveservices/v1"))
                .header("Content-Type", "application/ssml+xml")
                .header("Ocp-Apim-Subscription-Key", key)
                .header("X-Microsoft-OutputFormat", "audio-16khz-32kbitrate-mono-mp3")
                .header("User-Agent", "MyTTSApp")
                .POST(HttpRequest.BodyPublishers.ofString(ssml))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<byte[]> response = client.send(azureRequest, HttpResponse.BodyHandlers.ofByteArray());

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(response.body());
    }
}