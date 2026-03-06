package com.jc.halibut.auth;

import com.google.inject.AbstractModule;

public class LoginGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new LoginPersistenceModule());
        install(new LoginServiceModule());
    }
}
