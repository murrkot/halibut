package com.jc.halibut.location;

import com.google.inject.Singleton;
import com.jc.halibut.Entity.Location;
import com.jc.halibut.dto.LocationDto;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class LocationMapper {
    public LocationDto toDto(Location entity) {
        if (entity == null) {
            return null;
        }
        return new LocationDto(entity.getId(), entity.getName(), entity.getDescription(), entity.getTimeZoneId());
    }

    public List<LocationDto> toDtoList(List<Location> entities) {
        List<LocationDto> result = new ArrayList<>();
        if (entities == null) {
            return result;
        }

        for (Location entity : entities) {
            LocationDto dto = toDto(entity);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }
}
