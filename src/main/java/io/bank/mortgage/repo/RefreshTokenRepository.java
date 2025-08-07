package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

/************************************
 * PRIMARY REFRESH TOKEN REPO       *
 ************************************/
public interface RefreshTokenRepository extends
        org.springframework.data.repository.reactive.ReactiveCrudRepository<RefreshToken, UUID>,
        RefreshTokenRepositoryCustom {

    Mono<RefreshToken> findByTokenAndRevokedFalse(UUID token);
    Flux<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
}
