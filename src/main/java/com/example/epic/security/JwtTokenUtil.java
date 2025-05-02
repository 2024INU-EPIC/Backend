package com.example.epic.security;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.example.epic.user.SiteUser;
import com.example.epic.user.UserRepository;
import com.example.epic.user.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class JwtTokenUtil {
    private final UserRepository userRepository;
    @Value("${jwt.secret}")
    private String secretkey;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long reissueLimit = 48;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecretKey createDecodedToken() {
        // deprecated signWith(algorithm, key) 메서드 수정을 위해 hmacShaKey() 메서드 사용
        // Base64로 인코딩 된 secretKey 를 디코딩하여 hmacShaKeyFor() 메서드로 고정된 길이의 키 생성
        return Keys.hmacShaKeyFor(Base64.getMimeDecoder().decode(secretkey));
    }

    private final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    // 액세스 토큰 발급
    public String createAccessToken(String email) {
        // Token 유효 시간 = 30분
        // Claim = Jwt Token 에 들어갈 정보
        // Claim 에 loginId를 넣어 줌으로써 나중에 loginId를 꺼낼 수 있음
        Claims claims = Jwts.claims();
        claims.put("email", email);

        SecretKey key4Al = createDecodedToken();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
                .signWith(key4Al, SignatureAlgorithm.HS512)
                .compact();
    }

    // 리프레쉬 토큰 관련 메서드
    // 리프레쉬 토큰 발급
    public String createRefreshToken() {
        // RefreshToken 유효 시간 = 24시간
        SecretKey key4Al = createDecodedToken();
        return Jwts.builder()
                // RefreshToken 에는 사용자 관련 정보가 필요 없음
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(5, ChronoUnit.HOURS)))
                .signWith(key4Al, SignatureAlgorithm.HS512)
                .compact();
    }
    
    // 액세스 토큰 재발급
    @Transactional
    public String recreateAccessToken(String token) {
        String email = getLoginId(token);
        // AccessToken 이 만료되었으므로 다시 발급
        return createAccessToken(email);
    }

    //
    public void updateRefreshToken(SiteUser _siteuser, String refreshToken) {
        RefreshToken existingRefreshToken = refreshTokenRepository.findById(_siteuser.getId()).orElse(null);
        if(existingRefreshToken != null) {
            existingRefreshToken.updateRefreshToken(refreshToken);
            refreshTokenRepository.save(existingRefreshToken);
        } else {
            refreshTokenRepository.save(new RefreshToken(_siteuser, refreshToken));
        }
    }

    // Claims 에서 loginId 꺼내기
    public String getLoginId(String token) {
        return extractClaims(token).get("email").toString();
    }

    // 발급된 Token 이 만료 시간이 지났는지 체크
    public boolean isExpired(String token) {
        Date expiredDate = extractClaims(token).getExpiration();
        // Token 의 만료 날짜가 지금보다 이전인지 check
        return expiredDate.before(new Date());
    }

    // SecretKey 를 사용해 Token Parsing
    private Claims extractClaims(String token) throws ExpiredJwtException {
        try {
            SecretKey key4Al = createDecodedToken();
            return Jwts.parserBuilder().setSigningKey(key4Al).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}