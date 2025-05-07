package org.samaan.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.samaan.model.User;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import java.time.ZoneId;
import java.time.ZonedDateTime;


@Service
public class JwtService{


    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    private Key secretKey;


    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Date issuedAt = Date.from(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toInstant());
        Date expiration = Date.from(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toInstant().plusMillis(jwtExpirationMs));

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .claim("role", user.getRole())
                .claim("name", user.getName())
                .claim("id", user.getId())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).getPayload().get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaims(token).getPayload().getExpiration();
    }

    private Jws<Claims> extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toInstant()));
    }

    public boolean validateToken(String token, User user) {
        return user.getEmail().equals(extractUsername(token)) && !isTokenExpired(token);
    }
}
