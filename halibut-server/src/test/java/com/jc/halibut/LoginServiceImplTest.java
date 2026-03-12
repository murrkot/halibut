package com.jc.halibut;

import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import com.jc.halibut.auth.ActiveSessionRepository;
import com.jc.halibut.auth.LoginAccountMapper;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.dto.LoginAccountDto;
import com.jc.halibut.dto.LoginAccountRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private LoginAccountRepository accountRepository;
    @Mock
    private ActiveSessionRepository activeSessionRepository;
    @Mock
    private LoginAccountMapper loginAccountMapper;

    private LoginServiceImpl service;

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-abc";
    private static final String SECURITY_TOKEN = "token-xyz";

    @BeforeEach
    void setUp() {
        service = new LoginServiceImpl(accountRepository, activeSessionRepository, loginAccountMapper);
    }

    private void mockAuthorizedSession(LoginRole role) {
        when(activeSessionRepository.isSessionActive(USER_ID, SESSION_ID, SECURITY_TOKEN)).thenReturn(true);
        LoginAccount account = createAccount(USER_ID, role);
        when(accountRepository.findById(USER_ID)).thenReturn(Optional.of(account));
    }

    private void mockInactiveSession() {
        when(activeSessionRepository.isSessionActive(USER_ID, SESSION_ID, SECURITY_TOKEN)).thenReturn(false);
    }

    private LoginAccount createAccount(Long id, LoginRole role) {
        LoginAccount account = new LoginAccount();
        account.setRole(role);
        try {
            var idField = LoginAccount.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(account, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return account;
    }

    // ===================== changePassword =====================

    @Nested
    class ChangePassword {

        @Test
        void returnsTrue_whenAuthorizedAsAdminAndChangeSucceeds() {
            mockAuthorizedSession(LoginRole.ADMIN);
            when(accountRepository.changePassword(10L, "newPass123")).thenReturn(true);

            boolean result = service.changePassword(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L, "newPass123");

            assertTrue(result);
            verify(accountRepository).changePassword(10L, "newPass123");
        }

        @Test
        void returnsTrue_whenAuthorizedAsManagerAndChangeSucceeds() {
            mockAuthorizedSession(LoginRole.MANAGER);
            when(accountRepository.changePassword(10L, "newPass123")).thenReturn(true);

            boolean result = service.changePassword(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L, "newPass123");

            assertTrue(result);
        }

        @Test
        void returnsFalse_whenAuthorizedButChangeFails() {
            mockAuthorizedSession(LoginRole.ADMIN);
            when(accountRepository.changePassword(10L, "newPass123")).thenReturn(false);

            boolean result = service.changePassword(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L, "newPass123");

            assertFalse(result);
        }

        @Test
        void returnsFalse_whenSessionIsInactive() {
            mockInactiveSession();

            boolean result = service.changePassword(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L, "newPass123");

            assertFalse(result);
            verify(accountRepository, never()).changePassword(anyLong(), anyString());
        }

        @Test
        void returnsFalse_whenUserRoleIsUser() {
            mockAuthorizedSession(LoginRole.USER);

            boolean result = service.changePassword(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L, "newPass123");

            assertFalse(result);
            verify(accountRepository, never()).changePassword(anyLong(), anyString());
        }

        @Test
        void returnsFalse_whenAccountNotFound() {
            when(activeSessionRepository.isSessionActive(USER_ID, SESSION_ID, SECURITY_TOKEN)).thenReturn(true);
            when(accountRepository.findById(USER_ID)).thenReturn(Optional.empty());

            boolean result = service.changePassword(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L, "newPass123");

            assertFalse(result);
            verify(accountRepository, never()).changePassword(anyLong(), anyString());
        }
    }

    // ===================== changeOwnPassword =====================

    @Nested
    class ChangeOwnPassword {

        @Test
        void returnsTrue_whenSessionIsActiveAndCurrentPasswordMatches() {
            when(activeSessionRepository.isSessionActive(USER_ID, SESSION_ID, SECURITY_TOKEN)).thenReturn(true);
            when(accountRepository.changeOwnPassword(USER_ID, "oldPass", "newPass")).thenReturn(true);

            boolean result = service.changeOwnPassword(USER_ID, SESSION_ID, SECURITY_TOKEN, "oldPass", "newPass");

            assertTrue(result);
            verify(accountRepository).changeOwnPassword(USER_ID, "oldPass", "newPass");
        }

        @Test
        void returnsFalse_whenSessionIsInactive() {
            mockInactiveSession();

            boolean result = service.changeOwnPassword(USER_ID, SESSION_ID, SECURITY_TOKEN, "oldPass", "newPass");

            assertFalse(result);
            verify(accountRepository, never()).changeOwnPassword(anyLong(), anyString(), anyString());
        }

        @Test
        void returnsFalse_whenCurrentPasswordIsWrong() {
            when(activeSessionRepository.isSessionActive(USER_ID, SESSION_ID, SECURITY_TOKEN)).thenReturn(true);
            when(accountRepository.changeOwnPassword(USER_ID, "wrongPass", "newPass")).thenReturn(false);

            boolean result = service.changeOwnPassword(USER_ID, SESSION_ID, SECURITY_TOKEN, "wrongPass", "newPass");

            assertFalse(result);
        }

        @Test
        void worksForUserRole() {
            when(activeSessionRepository.isSessionActive(USER_ID, SESSION_ID, SECURITY_TOKEN)).thenReturn(true);
            when(accountRepository.changeOwnPassword(USER_ID, "oldPass", "newPass")).thenReturn(true);

            boolean result = service.changeOwnPassword(USER_ID, SESSION_ID, SECURITY_TOKEN, "oldPass", "newPass");

            assertTrue(result);
        }
    }

    // ===================== saveLoginAccount with password =====================

    @Nested
    class SaveLoginAccountWithPassword {

        @Test
        void delegatesToRepository_whenAuthorized() {
            mockAuthorizedSession(LoginRole.ADMIN);
            LoginAccountDto dto = new LoginAccountDto(null, "newuser", "New User",
                    LoginAccountRole.USER, false, true);
            dto.setPlainPassword("myPassword");
            when(accountRepository.saveLoginAccount(dto)).thenReturn(true);

            boolean result = service.saveLoginAccount(USER_ID, SESSION_ID, SECURITY_TOKEN, dto);

            assertTrue(result);
            verify(accountRepository).saveLoginAccount(dto);
            assertEquals("myPassword", dto.getPlainPassword());
        }

        @Test
        void returnsFalse_whenSessionIsInactive() {
            mockInactiveSession();
            LoginAccountDto dto = new LoginAccountDto(null, "newuser", "New User",
                    LoginAccountRole.USER, false, true);
            dto.setPlainPassword("myPassword");

            boolean result = service.saveLoginAccount(USER_ID, SESSION_ID, SECURITY_TOKEN, dto);

            assertFalse(result);
            verify(accountRepository, never()).saveLoginAccount(any());
        }
    }

    // ===================== getLoginAccounts =====================

    @Nested
    class GetLoginAccounts {

        @Test
        void returnsAccounts_whenAuthorizedAsAdmin() {
            mockAuthorizedSession(LoginRole.ADMIN);
            List<LoginAccount> entities = Arrays.asList(new LoginAccount(), new LoginAccount());
            List<LoginAccountDto> dtos = Arrays.asList(
                    new LoginAccountDto(1L, "admin", "Admin", LoginAccountRole.ADMIN, false, true),
                    new LoginAccountDto(2L, "user", "User", LoginAccountRole.USER, false, true)
            );
            when(accountRepository.findAllActive()).thenReturn(entities);
            when(loginAccountMapper.toDtoList(entities)).thenReturn(dtos);

            List<LoginAccountDto> result = service.getLoginAccounts(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertEquals(2, result.size());
        }

        @Test
        void returnsEmptyList_whenSessionIsInactive() {
            mockInactiveSession();

            List<LoginAccountDto> result = service.getLoginAccounts(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertTrue(result.isEmpty());
        }

        @Test
        void returnsEmptyList_whenUserRoleIsUser() {
            mockAuthorizedSession(LoginRole.USER);

            List<LoginAccountDto> result = service.getLoginAccounts(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertTrue(result.isEmpty());
        }
    }

    // ===================== deleteLoginAccount =====================

    @Nested
    class DeleteLoginAccount {

        @Test
        void returnsTrue_whenAuthorizedAndDeleteSucceeds() {
            mockAuthorizedSession(LoginRole.ADMIN);
            when(accountRepository.deleteLoginAccount(10L)).thenReturn(true);

            boolean result = service.deleteLoginAccount(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L);

            assertTrue(result);
        }

        @Test
        void returnsFalse_whenSessionIsInactive() {
            mockInactiveSession();

            boolean result = service.deleteLoginAccount(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L);

            assertFalse(result);
        }

        @Test
        void returnsFalse_whenUserRoleIsUser() {
            mockAuthorizedSession(LoginRole.USER);

            boolean result = service.deleteLoginAccount(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L);

            assertFalse(result);
        }
    }
}
