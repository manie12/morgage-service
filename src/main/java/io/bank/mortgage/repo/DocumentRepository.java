package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.Document;

import reactor.core.publisher.Flux;

import java.util.UUID;


public interface DocumentRepository extends
        org.springframework.data.repository.reactive.ReactiveCrudRepository<Document, UUID>,
        DocumentRepositoryCustom {

    Flux<Document> findByUserId(UUID applicationId);
}