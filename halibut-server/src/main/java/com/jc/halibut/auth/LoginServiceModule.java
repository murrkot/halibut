package com.jc.halibut.auth;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class LoginServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LoginAccountRepository.class).in(Singleton.class);
        bind(ActiveSessionRepository.class).in(Singleton.class);
        bind(LoginAccountMapper.class).in(Singleton.class);
    }
}
