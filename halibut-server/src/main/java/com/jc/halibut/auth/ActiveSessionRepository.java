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

    public enum SessionStatus {
        ACTIVE,
        EXPIRED,
        NOT_FOUND
    }

    @Inject
    public ActiveSessionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public ActiveSession createAuthenticatedSession(Long userId, String deviceId,
                                                    Long locationId, String locationName,
                                                    Duration ttl) {
        Instant now = Instant.now();
        ActiveSession activeSession = new ActiveSession();
        activeSession.setUserId(userId);
        activeSession.setDeviceId(deviceId);
        activeSession.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        activeSession.setSecurityToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        activeSession.setSessionExpirationTimestamp(now.plus(ttl));
        activeSession.setLocationId(locationId);
        activeSession.setLocationName(locationName);

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

    public void updateSessionLocation(Long userId, String sessionId, String securityToken,
                                      Long locationId, String locationName) {
        if (userId == null || isBlank(sessionId) || isBlank(securityToken)) {
            return;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery(
                            "update ActiveSession set locationId = :locationId, locationName = :locationName " +
                                    "where userId = :userId and sessionId = :sessionId and securityToken = :securityToken")
                    .setParameter("locationId", locationId)
                    .setParameter("locationName", locationName)
                    .setParameter("userId", userId)
                    .setParameter("sessionId", sessionId)
                    .setParameter("securityToken", securityToken)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        }
    }

    public Optional<ActiveSession> findActiveSession(Long userId, String sessionId, String securityToken) {
        if (userId == null || isBlank(sessionId) || isBlank(securityToken)) {
            return Optional.empty();
        }

        try (Session session = sessionFactory.openSession()) {
            return session.createSelectionQuery(
                            "from ActiveSession where userId = :userId and sessionId = :sessionId and securityToken = :securityToken",
                            ActiveSession.class)
                    .setParameter("userId", userId)
                    .setParameter("sessionId", sessionId)
                    .setParameter("securityToken", securityToken)
                    .setMaxResults(1)
                    .uniqueResultOptional();
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

    public SessionStatus getSessionStatus(Long userId, String sessionId, String securityToken) {
        if (userId == null || isBlank(sessionId) || isBlank(securityToken)) {
            return SessionStatus.NOT_FOUND;
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
                return SessionStatus.NOT_FOUND;
            }

            Instant expiresAt = matched.get().getSessionExpirationTimestamp();
            return expiresAt != null && expiresAt.isAfter(Instant.now())
                    ? SessionStatus.ACTIVE
                    : SessionStatus.EXPIRED;
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

    public int deleteExpiredSessions() {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int deleted = session.createMutationQuery(
                            "delete from ActiveSession where sessionExpirationTimestamp <= :now")
                    .setParameter("now", Instant.now())
                    .executeUpdate();
            tx.commit();
            return deleted;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return 0;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
