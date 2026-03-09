package com.jc.halibut.location;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class LocationServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LocationRepository.class).in(Singleton.class);
        bind(LocationMapper.class).in(Singleton.class);
        bind(LocationDataInitializer.class).asEagerSingleton();
    }
}
