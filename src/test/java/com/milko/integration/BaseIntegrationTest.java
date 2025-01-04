package com.milko.integration;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;

@MicronautTest
public abstract class BaseIntegrationTest {
    static PostgreSQLContainer<?> postgres;

    @BeforeAll
    static void startTestContainer() {
        postgres = new PostgreSQLContainer<>("postgres:16.0")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        postgres.start();

        System.setProperty("flyway.datasources.default.url", postgres.getJdbcUrl());
        System.setProperty("flyway.datasources.default.username", postgres.getUsername());
        System.setProperty("flyway.datasources.default.password", postgres.getPassword());

        System.setProperty("r2dbc.datasources.default.url", "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
        System.setProperty("r2dbc.datasources.default.username", postgres.getUsername());
        System.setProperty("r2dbc.datasources.default.password", postgres.getPassword());

        System.setProperty(
                "jpa.default.properties.hibernate.connection.url",
                "postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName()
        );
        System.setProperty("jpa.default.properties.hibernate.connection.username", postgres.getUsername());
        System.setProperty("jpa.default.properties.hibernate.connection.password", postgres.getPassword());
    }

    @AfterAll
    static void stopTestContainer() {
        if (postgres != null) {
            postgres.stop();
        }
    }
}