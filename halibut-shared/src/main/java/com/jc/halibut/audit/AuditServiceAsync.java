package com.jc.halibut.audit;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.jc.halibut.dto.AuditEventDto;

import java.util.List;

public interface AuditServiceAsync {
    void getAuditEvents(Long userId, String sessionId, String securityToken, int limit,
                        AsyncCallback<List<AuditEventDto>> callback);
}
