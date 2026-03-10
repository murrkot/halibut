package com.jc.halibut.location;

import com.jc.halibut.Entity.Location;
import com.jc.halibut.Entity.LoginAccount;
import com.jc.halibut.Entity.LoginRole;
import com.jc.halibut.auth.ActiveSessionRepository;
import com.jc.halibut.auth.LoginAccountRepository;
import com.jc.halibut.dto.LocationDto;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private ActiveSessionRepository activeSessionRepository;
    @Mock
    private LoginAccountRepository accountRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationMapper locationMapper;

    private LocationServiceImpl service;

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-abc";
    private static final String SECURITY_TOKEN = "token-xyz";

    @BeforeEach
    void setUp() {
        service = new LocationServiceImpl(activeSessionRepository, accountRepository, locationRepository, locationMapper);
    }

    // --- Authorization helper ---

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

    // ===================== getLocations =====================

    @Nested
    class GetLocations {

        @Test
        void returnsLocations_whenAuthorizedAsAdmin() {
            mockAuthorizedSession(LoginRole.ADMIN);
            List<Location> entities = Arrays.asList(new Location(), new Location());
            List<LocationDto> dtos = Arrays.asList(
                    new LocationDto(1L, "A", "Desc A"),
                    new LocationDto(2L, "B", "Desc B")
            );
            when(locationRepository.findAll()).thenReturn(entities);
            when(locationMapper.toDtoList(entities)).thenReturn(dtos);

            List<LocationDto> result = service.getLocations(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertEquals(2, result.size());
            assertEquals("A", result.get(0).getName());
            verify(locationRepository).findAll();
        }

        @Test
        void returnsLocations_whenAuthorizedAsManager() {
            mockAuthorizedSession(LoginRole.MANAGER);
            List<Location> entities = List.of(new Location());
            List<LocationDto> dtos = List.of(new LocationDto(1L, "A", "Desc A"));
            when(locationRepository.findAll()).thenReturn(entities);
            when(locationMapper.toDtoList(entities)).thenReturn(dtos);

            List<LocationDto> result = service.getLocations(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertEquals(1, result.size());
        }

        @Test
        void returnsEmptyList_whenSessionIsInactive() {
            mockInactiveSession();

            List<LocationDto> result = service.getLocations(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertTrue(result.isEmpty());
            verifyNoInteractions(locationRepository);
        }

        @Test
        void returnsEmptyList_whenUserRoleIsUser() {
            mockAuthorizedSession(LoginRole.USER);

            List<LocationDto> result = service.getLocations(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertTrue(result.isEmpty());
            verifyNoInteractions(locationRepository);
        }

        @Test
        void returnsEmptyList_whenAccountNotFound() {
            when(activeSessionRepository.isSessionActive(USER_ID, SESSION_ID, SECURITY_TOKEN)).thenReturn(true);
            when(accountRepository.findById(USER_ID)).thenReturn(Optional.empty());

            List<LocationDto> result = service.getLocations(USER_ID, SESSION_ID, SECURITY_TOKEN);

            assertTrue(result.isEmpty());
            verifyNoInteractions(locationRepository);
        }
    }

    // ===================== getLocationById =====================

    @Nested
    class GetLocationById {

        @Test
        void returnsLocationDto_whenAuthorizedAsAdmin() {
            mockAuthorizedSession(LoginRole.ADMIN);
            Location entity = new Location();
            LocationDto dto = new LocationDto(5L, "Warehouse", "Main warehouse");
            when(locationRepository.findById(5L)).thenReturn(entity);
            when(locationMapper.toDto(entity)).thenReturn(dto);

            LocationDto result = service.getLocationById(USER_ID, SESSION_ID, SECURITY_TOKEN, 5L);

            assertNotNull(result);
            assertEquals("Warehouse", result.getName());
        }

        @Test
        void returnsNull_whenSessionIsInactive() {
            mockInactiveSession();

            LocationDto result = service.getLocationById(USER_ID, SESSION_ID, SECURITY_TOKEN, 5L);

            assertNull(result);
            verifyNoInteractions(locationRepository);
        }

        @Test
        void returnsNull_whenUserRoleIsUser() {
            mockAuthorizedSession(LoginRole.USER);

            LocationDto result = service.getLocationById(USER_ID, SESSION_ID, SECURITY_TOKEN, 5L);

            assertNull(result);
            verifyNoInteractions(locationRepository);
        }
    }

    // ===================== saveLocation =====================

    @Nested
    class SaveLocation {

        @Test
        void returnsTrue_whenAuthorizedAndSaveSucceeds() {
            mockAuthorizedSession(LoginRole.ADMIN);
            LocationDto dto = new LocationDto(null, "New Location", "New desc");
            when(locationRepository.saveLocation(dto)).thenReturn(true);

            boolean result = service.saveLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, dto);

            assertTrue(result);
            verify(locationRepository).saveLocation(dto);
        }

        @Test
        void returnsFalse_whenAuthorizedButSaveFails() {
            mockAuthorizedSession(LoginRole.MANAGER);
            LocationDto dto = new LocationDto(null, "New Location", "New desc");
            when(locationRepository.saveLocation(dto)).thenReturn(false);

            boolean result = service.saveLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, dto);

            assertFalse(result);
        }

        @Test
        void returnsFalse_whenSessionIsInactive() {
            mockInactiveSession();
            LocationDto dto = new LocationDto(null, "New Location", "New desc");

            boolean result = service.saveLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, dto);

            assertFalse(result);
            verifyNoInteractions(locationRepository);
        }

        @Test
        void returnsFalse_whenUserRoleIsUser() {
            mockAuthorizedSession(LoginRole.USER);
            LocationDto dto = new LocationDto(null, "New Location", "New desc");

            boolean result = service.saveLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, dto);

            assertFalse(result);
            verifyNoInteractions(locationRepository);
        }
    }

    // ===================== deleteLocation =====================

    @Nested
    class DeleteLocation {

        @Test
        void returnsTrue_whenAuthorizedAndDeleteSucceeds() {
            mockAuthorizedSession(LoginRole.ADMIN);
            when(locationRepository.deleteLocation(10L)).thenReturn(true);

            boolean result = service.deleteLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L);

            assertTrue(result);
            verify(locationRepository).deleteLocation(10L);
        }

        @Test
        void returnsFalse_whenAuthorizedButDeleteFails() {
            mockAuthorizedSession(LoginRole.MANAGER);
            when(locationRepository.deleteLocation(10L)).thenReturn(false);

            boolean result = service.deleteLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L);

            assertFalse(result);
        }

        @Test
        void returnsFalse_whenSessionIsInactive() {
            mockInactiveSession();

            boolean result = service.deleteLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L);

            assertFalse(result);
            verifyNoInteractions(locationRepository);
        }

        @Test
        void returnsFalse_whenUserRoleIsUser() {
            mockAuthorizedSession(LoginRole.USER);

            boolean result = service.deleteLocation(USER_ID, SESSION_ID, SECURITY_TOKEN, 10L);

            assertFalse(result);
            verifyNoInteractions(locationRepository);
        }
    }
}
