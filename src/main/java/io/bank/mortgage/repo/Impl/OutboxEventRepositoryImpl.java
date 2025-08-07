package io.bank.mortgage.repo.Impl;

import io.bank.mortgage.domain.model.OutboxEvent;
import io.bank.mortgage.repo.OutboxEventRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
class OutboxEventRepositoryImpl implements OutboxEventRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<OutboxEvent> insert(OutboxEvent event) {
        return template.insert(OutboxEvent.class).using(event);
    }

    @Override
    public Flux<OutboxEvent> fetchUnpublished(int limit) {
        Query q = Query.query(Criteria.where("published_at").isNull())
                .sort(Sort.by("occurred_at"))
                .limit(limit);
        return template.select(q, OutboxEvent.class);
    }

    @Override
    public Mono<Boolean> markPublished(UUID id) {
        Update upd = Update.update("published_at", Instant.now());
        Query q = Query.query(Criteria.where("id").is(id));
        return template.update(q, upd, OutboxEvent.class)
                .map(rows -> rows == 1);
    }

    @Override
    public Mono<Void> incrementAttempts(UUID id) {
        Update upd = Update.update("attempts", "attempts + 1"); // literal expression
        Query q = Query.query(Criteria.where("id").is(id));
        return template.update(q, upd, OutboxEvent.class).then();
    }
}
