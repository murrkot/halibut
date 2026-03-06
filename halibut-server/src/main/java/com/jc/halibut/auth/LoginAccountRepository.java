package com.jc.halibut.auth;

import com.google.inject.Inject;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class LoginAccountRepository {
    private final SessionFactory sessionFactory;

    @Inject
    public LoginAccountRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<LoginAccount> findActiveByCredentials(String username, String plainPassword) {
        String passwordHash = sha256(plainPassword);

        try (Session session = sessionFactory.openSession()) {
            return session.createSelectionQuery(
                            "from LoginAccount where username = :username and passwordHash = :passwordHash and active = true",
                            LoginAccount.class)
                    .setParameter("username", username)
                    .setParameter("passwordHash", passwordHash)
                    .setMaxResults(1)
                    .uniqueResultOptional();
        }
    }

    public void saveSeedAccountsIfTableEmpty(List<SeedAccount> accounts) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createSelectionQuery("select count(a.id) from LoginAccount a", Long.class)
                    .getSingleResult();

            if (count != null && count > 0) {
                return;
            }
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            for (SeedAccount seed : accounts) {
                session.persist(createAccount(seed));
            }

            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private LoginAccount createAccount(SeedAccount seed) {
        LoginAccount account = new LoginAccount();
        account.setUsername(seed.username());
        account.setDisplayName(seed.displayName());
        account.setPasswordHash(seed.passwordHash());
        account.setRole(seed.role());
        account.setActive(true);
        return account;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public record SeedAccount(String username, String passwordHash, String displayName, LoginRole role) {
    }
}
