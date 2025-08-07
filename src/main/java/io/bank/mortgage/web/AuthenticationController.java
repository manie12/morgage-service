package io.bank.mortgage.web;

import io.bank.mortgage.config.JwtTokenProvider;
import io.bank.mortgage.domain.model.RefreshToken;
import io.bank.mortgage.repo.Impl.RefreshTokenRepositoryImpl;
import io.bank.mortgage.repo.Impl.UserRepositoryImpl;
import io.bank.mortgage.service.impl.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepositoryImpl refreshTokenRepository;
    private final UserRepositoryImpl userRepository;
    
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody LoginRequest request) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                request.nationalId(), 
                request.password()
        );
        
        return authenticationManager.authenticate(authentication)
                .map(auth -> (CustomUserDetails) auth.getPrincipal())
                .flatMap(userDetails -> {
                    String accessToken = tokenProvider.generateAccessToken(userDetails);
                    String refreshToken = tokenProvider.generateRefreshToken();
                    
                    RefreshToken tokenEntity = RefreshToken.builder()
                            .token(UUID.fromString(refreshToken))
                            .userId(userDetails.getUserId())
                            .expiresAt(OffsetDateTime.now().plusSeconds(tokenProvider.getRefreshExpiration() / 1000))
                            .revoked(false)
                            .createdAt(OffsetDateTime.now())
                            .build();
                    
                    return refreshTokenRepository.insert(tokenEntity)
                            .thenReturn(ResponseEntity.ok(Map.of(
                                    "accessToken", accessToken,
                                    "refreshToken", refreshToken
                            )));
                });
    }
    
    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, String>>> refresh(@RequestBody RefreshRequest request) {
        return refreshTokenRepository.findById(UUID.fromString(request.refreshToken()))
                .filter(token -> !token.getRevoked() && token.getExpiresAt().isAfter(OffsetDateTime.now()))
                .flatMap(token -> {
                    token.setRevoked(true);
                    return refreshTokenRepository.revokeToken(token.getToken())
                            .then(userRepository.findById(token.getUserId()))
                            .flatMap(user -> userRepository.findByNationalIdWithRoles(user.getNationalId()));
                })
                .map(user -> {
                    CustomUserDetails userDetails = new CustomUserDetails(
                            user.getId(),
                            user.getNationalId(),
                            user.getPasswordHash(),
                            user.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList())
                    );
                    
                    String newAccessToken = tokenProvider.generateAccessToken(userDetails);
                    String newRefreshToken = tokenProvider.generateRefreshToken();
                    
                    RefreshToken newTokenEntity = RefreshToken.builder()
                            .token(UUID.fromString(newRefreshToken))
                            .userId(userDetails.getUserId())
                            .expiresAt(OffsetDateTime.now().plusSeconds(tokenProvider.getRefreshExpiration() / 1000))
                            .revoked(false)
                            .createdAt(OffsetDateTime.now())
                            .build();
                    
                    refreshTokenRepository.insert(newTokenEntity).subscribe();
                    
                    return ResponseEntity.ok(Map.of(
                            "accessToken", newAccessToken,
                            "refreshToken", newRefreshToken
                    ));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(401).build()));
    }
    
    public record LoginRequest(String nationalId, String password) {}
    public record RefreshRequest(String refreshToken) {}
}