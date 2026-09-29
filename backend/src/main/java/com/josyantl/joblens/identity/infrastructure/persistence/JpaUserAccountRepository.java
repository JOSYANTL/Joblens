package com.josyantl.joblens.identity.infrastructure.persistence;

import com.josyantl.joblens.identity.domain.model.UserAccount;
import com.josyantl.joblens.identity.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserAccountRepository implements UserAccountRepository {
    private final SpringDataUserAccountRepository repository;

    @Override
    public UserAccount save(UserAccount account) {
        UserAccountJpaEntity entity = new UserAccountJpaEntity();
        entity.setId(account.id());
        entity.setEmail(account.email());
        entity.setPasswordHash(account.passwordHash());
        entity.setDisplayName(account.displayName());
        entity.setEnabled(account.enabled());
        entity.setCreatedAt(account.createdAt());
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return repository.findByEmail(UserAccount.normalizeEmail(email)).map(this::toDomain);
    }

    private UserAccount toDomain(UserAccountJpaEntity entity) {
        return new UserAccount(entity.getId(), entity.getEmail(), entity.getPasswordHash(),
                entity.getDisplayName(), entity.isEnabled(), entity.getCreatedAt());
    }
}
