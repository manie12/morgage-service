package io.bank.mortgage.repo;

import io.bank.mortgage.datatype.DecisionType;
import io.bank.mortgage.domain.model.Decision;
import org.springframework.data.domain.Pageable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DecisionRepositoryCustom {
    Mono<Decision> insert(Decision decision);

    Flux<Decision> search(UUID applicationId, DecisionType type, Pageable pageable);
}