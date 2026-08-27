package com.croi.security;

import com.croi.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtProvider(JwtConfig jwtConfig) {
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = jwtConfig.getExpirationMs();
        // Never logs the secret itself — just enough to eyeball "is every process using the
        // same secret" across restarts/containers without exposing the key value.
        log.info("JwtProvider initialized: secret length={}, expirationMs={}",
                jwtConfig.getSecret() == null ? 0 : jwtConfig.getSecret().length(), expirationMs);
    }

    public String generateToken(String subjectEmail) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subjectEmail)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT rejected: expired at {}", e.getClaims().getExpiration());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT rejected: signature does not match this server's jwt.secret "
                    + "(token was likely issued by a different process/secret) — {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT rejected: {} — {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
