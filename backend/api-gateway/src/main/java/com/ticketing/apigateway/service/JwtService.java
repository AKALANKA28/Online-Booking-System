package com.ticketing.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long tokenValidityHours;

    public JwtService(@Value("${app.security.jwt-secret}") String jwtSecret,
                      @Value("${app.security.token-validity-hours}") long tokenValidityHours) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.tokenValidityHours = tokenValidityHours;
    }

    public String generateToken(DemoUserService.DemoUser user) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiry = now.plusHours(tokenValidityHours);

        return Jwts.builder()
                .subject(user.username())
                .claim("userId", user.userId())
                .claim("email", user.email())
                .claim("role", user.role())
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiry.toInstant()))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public OffsetDateTime getExpiry(String token) {
        return parse(token).getExpiration().toInstant().atOffset(ZoneOffset.UTC);
    }
}
