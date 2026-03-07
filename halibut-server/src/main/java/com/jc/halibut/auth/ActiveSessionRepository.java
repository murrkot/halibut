package com.jc.halibut.auth;

import com.google.inject.Inject;
import com.jc.halibut.Entity.ActiveSession;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ActiveSessionRepository {
    private final SessionFactory sessionFactory;

    @Inject
    public ActiveSessionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public ActiveSession createAuthenticatedSession(Long userId, String deviceId, Duration ttl) {
        Instant now = Instant.now();
        ActiveSession activeSession = new ActiveSession();
        activeSession.setUserId(userId);
        activeSession.setDeviceId(deviceId);
        activeSession.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        activeSession.setSecurityToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        activeSession.setSessionExpirationTimestamp(now.plus(ttl));

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(activeSession);
            tx.commit();
            return activeSession;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public boolean isSessionActive(Long userId, String sessionId, String securityToken) {
        if (userId == null || isBlank(sessionId) || isBlank(securityToken)) {
            return false;
        }

        try (Session session = sessionFactory.openSession()) {
            Optional<ActiveSession> matched = session.createSelectionQuery(
                            "from ActiveSession where userId = :userId and sessionId = :sessionId and securityToken = :securityToken",
                            ActiveSession.class)
                    .setParameter("userId", userId)
                    .setParameter("sessionId", sessionId)
                    .setParameter("securityToken", securityToken)
                    .setMaxResults(1)
                    .uniqueResultOptional();

            if (matched.isEmpty()) {
                return false;
            }

            return matched.get().getSessionExpirationTimestamp().isAfter(Instant.now());
        }
    }

    public boolean deactivateSession(Long userId, String sessionId, String securityToken) {
        if (userId == null || isBlank(sessionId) || isBlank(securityToken)) {
            return false;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int deleted = session.createMutationQuery(
                            "delete from ActiveSession where userId = :userId and sessionId = :sessionId and securityToken = :securityToken")
                    .setParameter("userId", userId)
                    .setParameter("sessionId", sessionId)
                    .setParameter("securityToken", securityToken)
                    .executeUpdate();
            tx.commit();
            return deleted > 0;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
