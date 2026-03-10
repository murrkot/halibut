package com.jc.halibut.location;

import com.jc.halibut.Entity.Location;
import com.jc.halibut.dto.LocationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationMapperTest {

    private LocationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LocationMapper();
    }

    @Test
    void toDto_mapsEntityFieldsCorrectly() {
        Location entity = createLocation(1L, "Office A", "Main office");

        LocationDto dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Office A", dto.getName());
        assertEquals("Main office", dto.getDescription());
    }

    @Test
    void toDto_returnsNullWhenEntityIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDtoList_mapsAllEntities() {
        List<Location> entities = Arrays.asList(
                createLocation(1L, "Office A", "First office"),
                createLocation(2L, "Office B", "Second office")
        );

        List<LocationDto> dtos = mapper.toDtoList(entities);

        assertEquals(2, dtos.size());
        assertEquals("Office A", dtos.get(0).getName());
        assertEquals("Office B", dtos.get(1).getName());
    }

    @Test
    void toDtoList_returnsEmptyListWhenInputIsNull() {
        List<LocationDto> dtos = mapper.toDtoList(null);

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void toDtoList_returnsEmptyListWhenInputIsEmpty() {
        List<LocationDto> dtos = mapper.toDtoList(Collections.emptyList());

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    private Location createLocation(Long id, String name, String description) {
        Location location = new Location();
        location.setName(name);
        location.setDescription(description);
        // Location.id has no setter (generated), use reflection for testing
        try {
            var idField = Location.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(location, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
        return location;
    }
}
