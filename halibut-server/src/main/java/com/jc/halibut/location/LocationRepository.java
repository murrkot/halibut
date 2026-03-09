package com.jc.halibut.location;

import com.google.inject.Inject;
import com.jc.halibut.Entity.Location;
import com.jc.halibut.dto.LocationDto;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class LocationRepository {
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
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record SeedLocation(String name, String description) {
    }
}
