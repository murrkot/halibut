package com.jc.halibut.auth;

import com.google.inject.AbstractModule;
import com.jc.halibut.location.LocationServiceModule;

public class LoginGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new LoginPersistenceModule());
        install(new LoginServiceModule());
        install(new LocationServiceModule());
    }
}
