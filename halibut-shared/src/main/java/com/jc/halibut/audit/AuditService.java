package com.jc.halibut.audit;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.jc.halibut.dto.AuditEventDto;

import java.util.List;

@RemoteServiceRelativePath("audit")
public interface AuditService extends RemoteService {
    List<AuditEventDto> getAuditEvents(Long userId, String sessionId, String securityToken, int limit)
            throws IllegalArgumentException;
}
