package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.OutboxEvent;

import java.util.UUID;


public interface OutboxEventRepository extends
        org.springframework.data.repository.reactive.ReactiveCrudRepository<OutboxEvent, UUID>,
        OutboxEventRepositoryCustom {
}
