package com.jc.halibut.auth;

import com.google.inject.Inject;
import com.jc.halibut.Entity.AuditEvent;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.Instant;

public class AuditEventRepository {
    private final SessionFactory sessionFactory;

    @Inject
    public AuditEventRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void logEvent(AuditEventType type, Long userId, String userName, String sessionId,
                         String remoteAddress, boolean success, String details, String locationName) {
        if (type == null) {
            return;
        }

        AuditEvent event = new AuditEvent();
        event.setEventTime(Instant.now());
        event.setEventType(type.name());
        event.setUserName(userName);
        event.setLocationName(locationName);
        event.setSessionId(sessionId);
        event.setRemoteAddress(remoteAddress);
        event.setSuccess(success);
        event.setDetails(details);

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(event);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        }
    }

    public java.util.List<AuditEvent> findRecent(int limit) {
        int safeLimit = limit <= 0 ? 0 : Math.min(limit, 500);
        try (Session session = sessionFactory.openSession()) {
            var query = session.createSelectionQuery(
                            "from AuditEvent order by eventTime desc",
                            AuditEvent.class);
            if (safeLimit > 0) {
                query.setMaxResults(safeLimit);
            }
            return query.getResultList();
        }
    }

    public boolean hasEventForSession(String sessionId, String eventType) {
        if (sessionId == null || sessionId.trim().isEmpty() || eventType == null || eventType.trim().isEmpty()) {
            return false;
        }

        try (Session session = sessionFactory.openSession()) {
            Long count = session.createSelectionQuery(
                            "select count(e.id) from AuditEvent e where e.sessionId = :sessionId and e.eventType = :eventType",
                            Long.class)
                    .setParameter("sessionId", sessionId)
                    .setParameter("eventType", eventType)
                    .getSingleResult();
            return count != null && count > 0;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
