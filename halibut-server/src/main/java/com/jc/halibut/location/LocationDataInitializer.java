package com.jc.halibut.location;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@com.google.inject.Singleton
public class LocationDataInitializer {
    @Inject
    public LocationDataInitializer(LocationRepository locationRepository) {
        List<LocationRepository.SeedLocation> seedLocations = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            seedLocations.add(new LocationRepository.SeedLocation(
                    "Location " + i,
                    "Description for Location " + i
            ));
        }

        locationRepository.saveSeedLocationsIfTableEmpty(seedLocations);
    }
}
