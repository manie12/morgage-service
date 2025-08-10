package io.bank.mortgage.config;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private long accessExpiration;          // seconds
    @Getter
    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration; // seconds

    private SecretKey getSecretKey() {
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String generateAccessToken(io.bank.mortgage.service.impl.CustomUserDetails user) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        var roles = user.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList();
        claims.put("roles", roles);
        claims.put("userId", user.getUserId());

        long now = System.currentTimeMillis();
        return io.jsonwebtoken.Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + accessExpiration * 1000L)) // seconds → ms
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public reactor.core.publisher.Mono<io.jsonwebtoken.Claims> validateAndParseClaims(String token) {
        return reactor.core.publisher.Mono.fromCallable(() ->
                        io.jsonwebtoken.Jwts.parser()
                                .verifyWith(getSecretKey())
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                )
                .onErrorResume(e -> reactor.core.publisher.Mono.empty());
    }
}