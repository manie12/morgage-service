package io.bank.mortgage.domain.repo.Impl;

import io.bank.mortgage.domain.model.Application;
import io.bank.mortgage.domain.repo.ProductRepositoryCustom;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final R2dbcEntityTemplate entityTemplate;

    public ProductRepositoryCustomImpl(R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Application> insert(Application product) {
        return entityTemplate.insert(Application.class)
                .using(product);
    }
}