package io.bank.mortgage.repo.Impl;

import io.bank.mortgage.datatype.DecisionType;
import io.bank.mortgage.domain.model.Decision;
import io.bank.mortgage.repo.DecisionRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Repository
@RequiredArgsConstructor
class DecisionRepositoryImpl implements DecisionRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Decision> insert(Decision decision) {
        return template.insert(Decision.class).using(decision);
    }

    @Override
    public Flux<Decision> search(UUID applicationId, DecisionType type, Pageable pageable) {
        Criteria c = Criteria.empty();
        if (applicationId != null) c = c.and(Criteria.where("application_id").is(applicationId));
        if (type != null)          c = c.and(Criteria.where("decision").is(type.name()));
        Query query = Query.query(c).with(pageable);
        return template.select(query, Decision.class);
    }
}
