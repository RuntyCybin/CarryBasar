package com.carry.basar.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${security.jwt.expiration-time}")
    private long EXPIRATION_TIME;

    private Key generateKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(generateKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(generateKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(generateKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}