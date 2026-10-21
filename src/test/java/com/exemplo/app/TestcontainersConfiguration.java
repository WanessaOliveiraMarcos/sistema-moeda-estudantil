package com.exemplo.app;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Sobe um MySQL descartável em Docker para os testes (mesma versão do docker-compose.yml).
 * O container é único e compartilhado por todos os contextos de teste: classes com
 * configurações diferentes (ex.: @MockitoBean) não sobem um banco novo cada.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"));

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return MYSQL;
    }
}
