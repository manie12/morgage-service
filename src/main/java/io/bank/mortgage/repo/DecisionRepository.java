package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.Decision;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface DecisionRepository extends
        org.springframework.data.repository.reactive.ReactiveCrudRepository<Decision, UUID>,
        DecisionRepositoryCustom {

  Flux<Decision> findByApplicationIdOrderByDecidedAtDesc(UUID applicationId);

  Mono<Decision> findTop1ByApplicationIdOrderByDecidedAtDesc(UUID applicationId);
}