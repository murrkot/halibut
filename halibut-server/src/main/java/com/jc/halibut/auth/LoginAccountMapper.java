package com.jc.halibut.auth;

import com.google.inject.Singleton;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.dto.LoginAccountDto;
import com.jc.halibut.dto.LoginAccountRole;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class LoginAccountMapper {
    public LoginAccountDto toDto(LoginAccount entity) {
        if (entity == null) {
            return null;
        }

        LoginAccountRole role = entity.getRole() == null
                ? LoginAccountRole.USER
                : LoginAccountRole.valueOf(entity.getRole().name());

        return new LoginAccountDto(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                role,
                entity.isAutoSessionRestoreEnabled(),
                entity.isActive(),
                entity.getSessionTimeout()
        );
    }

    public List<LoginAccountDto> toDtoList(List<LoginAccount> entities) {
        List<LoginAccountDto> result = new ArrayList<>();
        if (entities == null) {
            return result;
        }

        for (LoginAccount entity : entities) {
            LoginAccountDto dto = toDto(entity);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }
}
