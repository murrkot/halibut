package com.jc.halibut.auth;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.jc.halibut.Entity.AuditEvent;
import com.jc.halibut.Entity.ActiveSession;
import com.jc.halibut.Entity.Location;
import com.jc.halibut.Entity.LoginAccount;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class LoginPersistenceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LoginDataInitializer.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    SessionFactory provideSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(LoginAccount.class);
        configuration.addAnnotatedClass(ActiveSession.class);
        configuration.addAnnotatedClass(AuditEvent.class);
        configuration.addAnnotatedClass(Location.class);
        return configuration.buildSessionFactory();
    }
}
