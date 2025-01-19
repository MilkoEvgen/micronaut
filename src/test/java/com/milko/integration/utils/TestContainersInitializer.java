package com.milko.integration.utils;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@Context
@Requires(env = "test")
public class TestContainersInitializer implements AutoCloseable {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private final Environment environment;

    public TestContainersInitializer(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void initialize() {
        postgres.start();

        environment.addPropertySource(
                PropertySource.of("testcontainers", Map.of(
                        "FLYWAY_URL", postgres.getJdbcUrl(),
                        "FLYWAY_USER", postgres.getUsername(),
                        "FLYWAY_PASS", postgres.getPassword(),
                        "R2DBC_URL", "r2dbc:postgresql://" + postgres.getHost() + ":" +
                                postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName(),
                        "R2DBC_USER", postgres.getUsername(),
                        "R2DBC_PASS", postgres.getPassword()
                ))
        );
    }

    @PreDestroy
    @Override
    public void close() {
        postgres.stop();
    }
}
