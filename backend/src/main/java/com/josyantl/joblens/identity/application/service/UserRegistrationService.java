package com.josyantl.joblens.identity.application.service;

import com.josyantl.joblens.identity.application.exception.EmailAlreadyRegisteredException;
import com.josyantl.joblens.identity.domain.model.UserAccount;
import com.josyantl.joblens.identity.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserAccount register(String email, String rawPassword, String displayName) {
        String normalizedEmail = UserAccount.normalizeEmail(email);
        if (repository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }
        validatePassword(rawPassword);
        try {
            return repository.save(UserAccount.register(normalizedEmail,
                    passwordEncoder.encode(rawPassword), displayName, Instant.now()));
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.length() > 72) {
            throw new IllegalArgumentException("Password must contain 12 to 72 characters");
        }
    }
}
