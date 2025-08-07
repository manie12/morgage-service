package io.bank.mortgage.repo;

import io.bank.mortgage.datatype.DocumentStatus;
import io.bank.mortgage.domain.model.Document;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface DocumentRepositoryCustom {
    Mono<Document> insert(Document document);

    Mono<Boolean> updateStatus(UUID documentId, DocumentStatus newStatus, String checksum);

    Flux<Document> search(UUID applicationId, DocumentStatus status, Pageable pageable);
}