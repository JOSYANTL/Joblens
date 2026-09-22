package com.josyantl.joblens.identity.domain.repository;

import com.josyantl.joblens.identity.domain.model.UserAccount;
import java.util.Optional;

public interface UserAccountRepository {
    UserAccount save(UserAccount account);
    Optional<UserAccount> findByEmail(String email);
}
