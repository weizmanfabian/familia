package com.weiz.familia;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para pruebas de integración: levanta un PostgreSQL real en un
 * contenedor y apunta el datasource a él. El esquema se genera con
 * ddl-auto=create-drop desde las entidades (ver
 * src/test/resources/application.properties).
 *
 * disabledWithoutDocker = true hace que las subclases se salten (skip) en
 * vez de fallar cuando Testcontainers no encuentra un entorno Docker
 * disponible, manteniendo el build verde en máquinas sin Docker.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
abstract class AbstractPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
