package io.bank.mortgage.service.impl;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class CustomUserDetails extends User {
    private final Long userId;

    public CustomUserDetails(Long userId, String username, String password,
                            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }
}