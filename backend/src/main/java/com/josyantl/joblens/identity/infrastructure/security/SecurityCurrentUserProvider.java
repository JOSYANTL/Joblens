package com.josyantl.joblens.identity.infrastructure.security;

import com.josyantl.joblens.identity.domain.repository.UserAccountRepository;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityCurrentUserProvider implements CurrentUserProvider {
    private final UserAccountRepository repository;

    @Override
    public Long userId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        return repository.findByEmail(authentication.getName())
                .filter(account -> account.enabled())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authentication required"))
                .id();
    }
}
