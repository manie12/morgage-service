package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.User;
import io.bank.mortgage.web.AuthenticationController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepositoryCustom {
    Mono<User> save(User user);

    Mono<User> createNewUser(AuthenticationController.RegistrationRequest request);

    Mono<User> findByNationalIdWithRoles(String nationalId);

    Mono<Void> addRole(Long userId, String roleName);

    Mono<Void> removeRole(Long userId, String roleName);

    Flux<String> rolesOf(Long userId);

    Mono<User> findById(Long userId);
}