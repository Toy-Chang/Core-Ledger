package com.coreledger.security;

import com.coreledger.config.CoreLedgerProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final CoreLedgerProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(CoreLedgerProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(CurrentUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.id())
                .claim("roles", user.roles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.jwt().expirationSeconds())))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long expirationSeconds() {
        return properties.jwt().expirationSeconds();
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(Claims claims) {
        Object roles = claims.get("roles");
        return roles instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }
}
