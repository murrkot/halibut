package com.jc.halibut.location;

import com.jc.halibut.Entity.ActiveSession;
import com.jc.halibut.Entity.Location;
import com.jc.halibut.Entity.LoginAccount;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that require a real PostgreSQL database.
 * <p>
 * Spins up a PostgreSQL Testcontainer and builds a Hibernate {@link SessionFactory}
 * configured against it. Subclasses can use {@link #sessionFactory} to create
 * repositories or interact with the database directly.
 * </p>
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("halibut_test")
            .withUsername("test")
            .withPassword("test");

    protected static SessionFactory sessionFactory;

    @BeforeAll
    static void initSessionFactory() {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.jdbc.time_zone", "UTC");

        configuration.addAnnotatedClass(LoginAccount.class);
        configuration.addAnnotatedClass(ActiveSession.class);
        configuration.addAnnotatedClass(Location.class);

        sessionFactory = configuration.buildSessionFactory();
    }

    @AfterAll
    static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
