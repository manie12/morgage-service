package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.OutboxEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface OutboxEventRepositoryCustom {
    Mono<OutboxEvent> insert(OutboxEvent event);

    /**
     * Fetch unpublished events ordered by occurrence time (oldest first).
     */
    Flux<OutboxEvent> fetchUnpublished(int limit);

    /**
     * Mark as published and set timestamp.
     */
    Mono<Boolean> markPublished(UUID id);

    /**
     * Increment attempts counter after a failed publish.
     */
    Mono<Void> incrementAttempts(UUID id);
}