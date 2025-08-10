package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.User;
import io.bank.mortgage.dto.NewApplicationCreateRequest;
import io.bank.mortgage.dto.RegistrationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepositoryCustom {

//    Mono<User> insertUser(NewApplicationCreateRequest user);

    Mono<User> findByNationalIdWithRoles(String nationalId);

    Mono<Void> addRole(Long userId, String roleName);

    Mono<Void> removeRole(Long userId, String roleName);

    Flux<String> rolesOf(Long userId);

    Mono<User> findById(Long userId);

    Mono<User> registerUser(RegistrationRequest request);
}
