package com.jc.halibut.audit;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import com.google.inject.Inject;
import com.jc.halibut.Entity.AuditEvent;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import com.jc.halibut.auth.ActiveSessionRepository;
import com.jc.halibut.auth.AuditEventRepository;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.auth.ServerInjector;
import com.jc.halibut.dto.AuditEventDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("serial")
public class AuditServiceImpl extends RemoteServiceServlet implements AuditService {
    private final ActiveSessionRepository activeSessionRepository;
    private final LoginAccountRepository accountRepository;
    private final AuditEventRepository auditEventRepository;

    public AuditServiceImpl() {
        this(
                ServerInjector.getInjector().getInstance(ActiveSessionRepository.class),
                ServerInjector.getInjector().getInstance(LoginAccountRepository.class),
                ServerInjector.getInjector().getInstance(AuditEventRepository.class)
        );
    }

    @Inject
    public AuditServiceImpl(ActiveSessionRepository activeSessionRepository,
                            LoginAccountRepository accountRepository,
                            AuditEventRepository auditEventRepository) {
        this.activeSessionRepository = activeSessionRepository;
        this.accountRepository = accountRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public List<AuditEventDto> getAuditEvents(Long userId, String sessionId, String securityToken, int limit)
            throws IllegalArgumentException {
        if (!isAuthorizedForAudit(userId, sessionId, securityToken)) {
            return new ArrayList<>();
        }
        List<AuditEvent> events = auditEventRepository.findRecent(limit);
        return toDtoList(events);
    }

    private boolean isAuthorizedForAudit(Long userId, String sessionId, String securityToken) {
        boolean validSession = activeSessionRepository.isSessionActive(userId, sessionId, securityToken);
        if (!validSession) {
            return false;
        }

        Optional<LoginAccount> requester = accountRepository.findById(userId);
        if (requester.isEmpty()) {
            return false;
        }

        return requester.get().getRole() == LoginRole.ADMIN;
    }

    private List<AuditEventDto> toDtoList(List<AuditEvent> events) {
        List<AuditEventDto> dtos = new ArrayList<>();
        if (events == null) {
            return dtos;
        }
        for (AuditEvent event : events) {
            if (event == null) {
                continue;
            }
            long eventTime = event.getEventTime() == null ? 0L : event.getEventTime().toEpochMilli();
            dtos.add(new AuditEventDto(
                    event.getId(),
                    eventTime,
                    event.getEventType(),
                    null,
                    event.getUserName(),
                    event.getSessionId(),
                    event.getRemoteAddress(),
                    event.isSuccess(),
                    event.getDetails()
            ));
        }
        return dtos;
    }
}
