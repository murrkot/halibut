package com.jc.halibut.auth;

import com.google.inject.Guice;
import com.google.inject.Injector;

public final class ServerInjector {
    private static final Injector INJECTOR = Guice.createInjector(new LoginGuiceModule());

    private ServerInjector() {
    }

    public static Injector getInjector() {
        return INJECTOR;
    }
}
