package com.jc.halibut.location;

import com.google.inject.Inject;
import com.jc.halibut.Entity.Location;
import com.jc.halibut.dto.LocationDto;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationRepository {
    private static final String DEFAULT_TIME_ZONE = "UTC";
    private static final List<String> AVAILABLE_TIME_ZONES = buildTimeZones();

    private final SessionFactory sessionFactory;

    @Inject
    public LocationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Location> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createSelectionQuery("from Location order by name asc", Location.class)
                    .getResultList();
        }
    }

    public Location findById(Long locationId) {
        if (locationId == null) {
            return null;
        }

        try (Session session = sessionFactory.openSession()) {
            return session.find(Location.class, locationId);
        }
    }

    public boolean saveLocation(LocationDto locationDto) {
        if (locationDto == null || isBlank(locationDto.getName()) || isBlank(locationDto.getDescription())) {
            return false;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Location entity;
            if (locationDto.getId() == null) {
                entity = new Location();
                applyDto(entity, locationDto);
                session.persist(entity);
            } else {
                entity = session.find(Location.class, locationDto.getId());
                if (entity == null) {
                    tx.rollback();
                    return false;
                }
                applyDto(entity, locationDto);
                session.merge(entity);
            }

            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return false;
        }
    }

    public boolean deleteLocation(Long locationId) {
        if (locationId == null) {
            return false;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int deleted = session.createMutationQuery("delete from Location where id = :locationId")
                    .setParameter("locationId", locationId)
                    .executeUpdate();
            tx.commit();
            return deleted > 0;
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return false;
        }
    }

    public void saveSeedLocationsIfTableEmpty(List<SeedLocation> locations) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createSelectionQuery("select count(l.id) from Location l", Long.class)
                    .getSingleResult();
            if (count != null && count > 0) {
                return;
            }
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            for (SeedLocation seed : locations) {
                Location location = new Location();
                location.setName(seed.name());
                location.setDescription(seed.description());
                location.setTimeZoneId(randomSeedTimeZone());
                session.persist(location);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private void applyDto(Location entity, LocationDto dto) {
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription().trim());
        entity.setTimeZoneId(resolveTimeZoneId(dto.getTimeZoneId()));
    }

    private String resolveTimeZoneId(String timeZoneId) {
        if (isBlank(timeZoneId)) {
            return DEFAULT_TIME_ZONE;
        }
        return timeZoneId.trim();
    }

    private String randomSeedTimeZone() {
        if (AVAILABLE_TIME_ZONES.isEmpty()) {
            return DEFAULT_TIME_ZONE;
        }
        int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(AVAILABLE_TIME_ZONES.size());
        return AVAILABLE_TIME_ZONES.get(index);
    }

    private static List<String> buildTimeZones() {
        List<String> zones = new ArrayList<>();
        for (String id : ZoneId.getAvailableZoneIds()) {
            if ("UTC".equals(id) || id.contains("/")) {
                zones.add(id);
            }
        }
        Collections.sort(zones);
        return zones;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record SeedLocation(String name, String description) {
    }
}
