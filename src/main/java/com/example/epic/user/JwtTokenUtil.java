package com.example.epic.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    // JWT Token 발급
    public static String createToken(String email, String secretKey, long expireTimeMs) {
        // Claim = Jwt Token에 들어갈 정보
        // Claim에 loginId를 넣어 줌으로써 나중에 loginId를 꺼낼 수 있음
        Claims claims = Jwts.claims();
        claims.put("email", email);
        // deprecated signWith(algorithm, key) 메서드 수정을 위해 hmacShaKey() 메서드 사용
        // Base64로 인코딩 된 secretKey를 디코딩하여 hmacShaKeyFor() 메서드로 고정된 길이의 키 생성
        SecretKey key4Al = Keys.hmacShaKeyFor(Base64.getMimeDecoder().decode(secretKey));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs))
                // .signWith(SignatureAlgorithm.HS512, key)
                // .signWith(SignatureAlgorithm.HS256, secretKey)
                // signWith(algorithm, key) is deprecated
                .signWith(key4Al, SignatureAlgorithm.HS512)
                .compact();
    }
    // Claims에서 loginId 꺼내기
    public static String getLoginId(String token, String secretKey) {
        SecretKey key4Al = Keys.hmacShaKeyFor(Base64.getMimeDecoder().decode(secretKey));
        return extractClaims(token, key4Al).get("email").toString();
    }

    // 발급된 Token이 만료 시간이 지났는지 체크
    public static boolean isExpired(String token, String secretKey) {
        SecretKey key4Al = Keys.hmacShaKeyFor(Base64.getMimeDecoder().decode(secretKey));
        Date expiredDate = extractClaims(token, key4Al).getExpiration();
        // Token의 만료 날짜가 지금보다 이전인지 check
        return expiredDate.before(new Date());
    }

    // SecretKey를 사용해 Token Parsing
    private static Claims extractClaims(String token, SecretKey secretKey) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}