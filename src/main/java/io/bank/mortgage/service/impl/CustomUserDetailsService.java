package io.bank.mortgage.service.impl;

import io.bank.mortgage.domain.model.User;
import io.bank.mortgage.repo.UserRepository; // <- interface, not Impl
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByNationalIdWithRoles(username)
                .map(this::toUserDetails);
    }

    private CustomUserDetails toUserDetails(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getNationalId(),
                user.getPasswordHash(),
                user.getRoles().stream()
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }
}