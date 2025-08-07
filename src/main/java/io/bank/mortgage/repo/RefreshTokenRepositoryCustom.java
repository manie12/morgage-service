package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.RefreshToken;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenRepositoryCustom {
    Mono<RefreshToken> insert(RefreshToken token);

    Mono<Boolean> revokeToken(UUID token);

    Mono<Integer> purgeExpired();

    // Add this method to your RefreshTokenRepositoryImpl class
    Mono<RefreshToken> findById(UUID token);
}
