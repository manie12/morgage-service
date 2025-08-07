package io.bank.mortgage.domain.repo;

import io.bank.mortgage.domain.model.Application;
import reactor.core.publisher.Mono;

public interface ProductRepositoryCustom {
    Mono<Application> insert(Application product);
}
