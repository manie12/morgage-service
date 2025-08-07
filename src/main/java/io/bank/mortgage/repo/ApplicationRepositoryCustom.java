package io.bank.mortgage.repo;

import io.bank.mortgage.datatype.Status;
import io.bank.mortgage.domain.model.Application;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface ApplicationRepositoryCustom {
    Mono<Application> insert(Application application);

    Mono<Boolean> updateStatus(UUID id, Status newStatus, int expectedVersion);

    Flux<Application> search(Status status, Instant createdFrom, Instant createdTo, String nationalIdHash, Pageable pageable);
}