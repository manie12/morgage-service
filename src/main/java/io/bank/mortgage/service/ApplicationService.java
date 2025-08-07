package io.bank.mortgage.service;

import io.bank.mortgage.datatype.Status;
import io.bank.mortgage.domain.model.Application;
import io.bank.mortgage.dto.DecisionRequest;
import io.bank.mortgage.dto.NewApplicationCreateRequest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface ApplicationService
{
    @Transactional
    Mono<Application> create(NewApplicationCreateRequest req,
                             String applicantUserId,
                             String correlationId);

    Mono<Application> getById(UUID id, String requesterId, boolean officer);

    Flux<Application> list(Status status,
                           Instant from,
                           Instant to,
                           String nationalIdHash,
                           int page,
                           int size);

    @Transactional
    Mono<Application> decide(UUID id,
                             DecisionRequest req,
                             String officerId,
                             String officerName,
                             int expectedVersion,
                             String correlationId);
}
