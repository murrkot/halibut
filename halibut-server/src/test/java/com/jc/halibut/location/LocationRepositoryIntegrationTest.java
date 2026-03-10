package com.jc.halibut.location;

import com.jc.halibut.Entity.Location;
import com.jc.halibut.dto.LocationDto;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationRepositoryIntegrationTest extends BaseIntegrationTest {

    private LocationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LocationRepository(sessionFactory);
        clearLocations();
    }

    private void clearLocations() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("delete from Location").executeUpdate();
            tx.commit();
        }
    }

    private Location insertLocation(String name, String description) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Location location = new Location();
            location.setName(name);
            location.setDescription(description);
            session.persist(location);
            tx.commit();
            return location;
        }
    }

    // --- findAll ---

    @Test
    void findAll_returnsAllLocationsSortedByName() {
        insertLocation("Zulu Warehouse", "Last alphabetically");
        insertLocation("Alpha Office", "First alphabetically");

        List<Location> result = repository.findAll();

        assertEquals(2, result.size());
        assertEquals("Alpha Office", result.get(0).getName());
        assertEquals("Zulu Warehouse", result.get(1).getName());
    }

    @Test
    void findAll_returnsEmptyListWhenNoLocations() {
        List<Location> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    // --- findById ---

    @Test
    void findById_returnsLocationWhenExists() {
        Location inserted = insertLocation("Office A", "Main office");

        Location result = repository.findById(inserted.getId());

        assertNotNull(result);
        assertEquals("Office A", result.getName());
        assertEquals("Main office", result.getDescription());
    }

    @Test
    void findById_returnsNullWhenNotFound() {
        Location result = repository.findById(99999L);

        assertNull(result);
    }

    @Test
    void findById_returnsNullWhenIdIsNull() {
        Location result = repository.findById(null);

        assertNull(result);
    }

    // --- saveLocation (create) ---

    @Test
    void saveLocation_createsNewLocation() {
        LocationDto dto = new LocationDto(null, "New Office", "Brand new");

        boolean saved = repository.saveLocation(dto);

        assertTrue(saved);
        List<Location> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals("New Office", all.get(0).getName());
    }

    @Test
    void saveLocation_trimsNameAndDescription() {
        LocationDto dto = new LocationDto(null, "  Trimmed  ", "  Desc  ");

        boolean saved = repository.saveLocation(dto);

        assertTrue(saved);
        Location location = repository.findAll().get(0);
        assertEquals("Trimmed", location.getName());
        assertEquals("Desc", location.getDescription());
    }

    @Test
    void saveLocation_returnsFalseForNullDto() {
        assertFalse(repository.saveLocation(null));
    }

    @Test
    void saveLocation_returnsFalseForBlankName() {
        LocationDto dto = new LocationDto(null, "  ", "Description");

        assertFalse(repository.saveLocation(dto));
    }

    @Test
    void saveLocation_returnsFalseForNullName() {
        LocationDto dto = new LocationDto(null, null, "Description");

        assertFalse(repository.saveLocation(dto));
    }

    @Test
    void saveLocation_returnsFalseForBlankDescription() {
        LocationDto dto = new LocationDto(null, "Name", "  ");

        assertFalse(repository.saveLocation(dto));
    }

    // --- saveLocation (update) ---

    @Test
    void saveLocation_updatesExistingLocation() {
        Location existing = insertLocation("Old Name", "Old Desc");
        LocationDto dto = new LocationDto(existing.getId(), "Updated Name", "Updated Desc");

        boolean saved = repository.saveLocation(dto);

        assertTrue(saved);
        Location updated = repository.findById(existing.getId());
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Desc", updated.getDescription());
    }

    @Test
    void saveLocation_returnsFalseForNonExistentId() {
        LocationDto dto = new LocationDto(99999L, "Name", "Desc");

        assertFalse(repository.saveLocation(dto));
    }

    // --- deleteLocation ---

    @Test
    void deleteLocation_removesExistingLocation() {
        Location inserted = insertLocation("To Delete", "Will be deleted");

        boolean deleted = repository.deleteLocation(inserted.getId());

        assertTrue(deleted);
        assertNull(repository.findById(inserted.getId()));
    }

    @Test
    void deleteLocation_returnsFalseForNonExistentId() {
        assertFalse(repository.deleteLocation(99999L));
    }

    @Test
    void deleteLocation_returnsFalseForNullId() {
        assertFalse(repository.deleteLocation(null));
    }

    // --- saveSeedLocationsIfTableEmpty ---

    @Test
    void saveSeedLocationsIfTableEmpty_insertsWhenTableIsEmpty() {
        List<LocationRepository.SeedLocation> seeds = List.of(
                new LocationRepository.SeedLocation("Seed 1", "Desc 1"),
                new LocationRepository.SeedLocation("Seed 2", "Desc 2")
        );

        repository.saveSeedLocationsIfTableEmpty(seeds);

        List<Location> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void saveSeedLocationsIfTableEmpty_doesNotInsertWhenTableHasData() {
        insertLocation("Existing", "Already here");

        List<LocationRepository.SeedLocation> seeds = List.of(
                new LocationRepository.SeedLocation("Seed 1", "Desc 1")
        );

        repository.saveSeedLocationsIfTableEmpty(seeds);

        List<Location> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals("Existing", all.get(0).getName());
    }
}
