package com.jc.halibut.auth;

import com.google.inject.Inject;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import com.jc.halibut.dto.LoginAccountDto;
import com.jc.halibut.dto.LoginAccountRole;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class LoginAccountRepository {
    private static final String DEFAULT_PASSWORD_HASH =
            "04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb";
    private static final String DEFAULT_SESSION_TIMEOUT = "30m";

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

    public Optional<LoginAccount> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        try (Session session = sessionFactory.openSession()) {
            return session.createSelectionQuery("from LoginAccount where id = :userId", LoginAccount.class)
                    .setParameter("userId", userId)
                    .setMaxResults(1)
                    .uniqueResultOptional();
        }
    }

    public List<LoginAccount> findAllActive() {
        try (Session session = sessionFactory.openSession()) {
            return session.createSelectionQuery(
                            "from LoginAccount where active = true order by role asc, username asc",
                            LoginAccount.class)
                    .getResultList();
        }
    }

    public boolean saveLoginAccount(LoginAccountDto account) {
        if (account == null || isBlank(account.getUsername()) || isBlank(account.getDisplayName())) {
            return false;
        }
        if (!isValidSessionTimeout(account.getSessionTimeout())) {
            return false;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            LoginAccount entity;
            if (account.getId() == null) {
                entity = new LoginAccount();
                if (!isBlank(account.getPlainPassword())) {
                    entity.setPasswordHash(sha256(account.getPlainPassword().trim()));
                } else {
                    entity.setPasswordHash(DEFAULT_PASSWORD_HASH);
                }
                applyDto(entity, account);
                session.persist(entity);
            } else {
                entity = session.find(LoginAccount.class, account.getId());
                if (entity == null) {
                    tx.rollback();
                    return false;
                }
                if (!isBlank(account.getPlainPassword())) {
                    entity.setPasswordHash(sha256(account.getPlainPassword().trim()));
                }
                applyDto(entity, account);
                session.merge(entity);
            }

            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return false;
        }
    }

    public boolean deleteLoginAccount(Long accountId) {
        if (accountId == null) {
            return false;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int deleted = session.createMutationQuery("delete from LoginAccount where id = :accountId")
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            tx.commit();
            return deleted > 0;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return false;
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

    public boolean changeOwnPassword(Long accountId, String currentPlainPassword, String newPlainPassword) {
        if (accountId == null || isBlank(currentPlainPassword) || isBlank(newPlainPassword)) {
            return false;
        }

        String currentHash = sha256(currentPlainPassword.trim());
        String newHash = sha256(newPlainPassword.trim());

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int updated = session.createMutationQuery(
                            "update LoginAccount set passwordHash = :newHash where id = :accountId and passwordHash = :currentHash")
                    .setParameter("newHash", newHash)
                    .setParameter("accountId", accountId)
                    .setParameter("currentHash", currentHash)
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return false;
        }
    }

    public boolean changePassword(Long accountId, String newPlainPassword) {
        if (accountId == null || isBlank(newPlainPassword)) {
            return false;
        }

        String newHash = sha256(newPlainPassword.trim());

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int updated = session.createMutationQuery(
                            "update LoginAccount set passwordHash = :hash where id = :accountId")
                    .setParameter("hash", newHash)
                    .setParameter("accountId", accountId)
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return false;
        }
    }

    public boolean updateAutoSessionRestorePreference(Long userId, boolean enabled) {
        if (userId == null) {
            return false;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int updated = session.createMutationQuery(
                            "update LoginAccount set autoSessionRestoreEnabled = :enabled where id = :userId")
                    .setParameter("enabled", enabled)
                    .setParameter("userId", userId)
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private void applyDto(LoginAccount entity, LoginAccountDto dto) {
        entity.setUsername(dto.getUsername().trim());
        entity.setDisplayName(dto.getDisplayName().trim());

        LoginAccountRole dtoRole = dto.getRole() == null ? LoginAccountRole.USER : dto.getRole();
        entity.setRole(LoginRole.valueOf(dtoRole.name()));

        entity.setAutoSessionRestoreEnabled(dto.isAutoSessionRestoreEnabled());
        entity.setActive(dto.isActive());
        entity.setSessionTimeout(normalizeSessionTimeout(dto.getSessionTimeout()));
    }

    private LoginAccount createAccount(SeedAccount seed) {
        LoginAccount account = new LoginAccount();
        account.setUsername(seed.username());
        account.setDisplayName(seed.displayName());
        account.setPasswordHash(seed.passwordHash());
        account.setRole(seed.role());
        account.setAutoSessionRestoreEnabled(seed.autoSessionRestoreEnabled());
        account.setSessionTimeout(normalizeSessionTimeout(seed.sessionTimeout()));
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeSessionTimeout(String raw) {
        if (isBlank(raw)) {
            return DEFAULT_SESSION_TIMEOUT;
        }
        String trimmed = raw.trim().toLowerCase();
        if ("1h".equals(trimmed) || "60m".equals(trimmed)) {
            return "1h";
        }
        return trimmed;
    }

    private boolean isValidSessionTimeout(String raw) {
        if (isBlank(raw)) {
            return true;
        }
        String trimmed = raw.trim().toLowerCase();
        if ("1h".equals(trimmed) || "60m".equals(trimmed)) {
            return true;
        }
        if (!trimmed.endsWith("m")) {
            return false;
        }
        String minutesPart = trimmed.substring(0, trimmed.length() - 1);
        try {
            int minutes = Integer.parseInt(minutesPart);
            return minutes >= 1 && minutes <= 59;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public record SeedAccount(String username, String passwordHash, String displayName, LoginRole role,
                              boolean autoSessionRestoreEnabled, String sessionTimeout) {
    }
}
