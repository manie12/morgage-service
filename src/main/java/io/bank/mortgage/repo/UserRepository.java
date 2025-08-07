package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserRepository extends
        org.springframework.data.repository.reactive.ReactiveCrudRepository<User, Long>,
        UserRepositoryCustom {

    Mono<User> findByNationalId(String nationalId);
}