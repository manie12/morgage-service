package io.bank.mortgage.repo.Impl;

import io.bank.mortgage.datatype.Status;
import io.bank.mortgage.domain.model.Application;
import io.bank.mortgage.repo.ApplicationRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ApplicationRepositoryImpl implements ApplicationRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Application> insert(Application app) {
        return template.insert(Application.class).using(app);
    }

    /**
     * Update the status (and timestamps) using R2dbcEntityTemplate’s fluent Update API,
     * while enforcing optimistic‑locking on the version column.
     */
    @Override
    public Mono<Boolean> updateStatus(UUID id, Status status, int expectedVersion) {
        int newVersion = expectedVersion + 1;
        Update update = Update.update("status", status.name())
                .set("decided_at", Instant.now())
                .set("version", newVersion);

        Query query = Query.query(
                Criteria.where("id").is(id)
                        .and("version").is(expectedVersion)
        );

        return template.update(query, update, Application.class)
                .map(rows -> rows == 1);
    }

    @Override
    public Flux<Application> search(Status status,
                                    Instant createdFrom,
                                    Instant createdTo,
                                    String nationalIdHash,
                                    Pageable pageable) {
        Criteria crit = Criteria.empty();
        if (status != null) crit = crit.and(Criteria.where("status").is(status.name()));
        if (createdFrom != null) crit = crit.and(Criteria.where("created_at").greaterThanOrEquals(createdFrom));
        if (createdTo != null) crit = crit.and(Criteria.where("created_at").lessThanOrEquals(createdTo));
        if (nationalIdHash != null) crit = crit.and(Criteria.where("national_id_hash").is(nationalIdHash));

        // If no filters, Criteria.empty() is acceptable; we still apply pagination.
        Query query = Query.query(crit).with(pageable);
        return template.select(query, Application.class);
    }
}
