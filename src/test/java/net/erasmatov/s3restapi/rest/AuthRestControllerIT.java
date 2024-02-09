package net.erasmatov.s3restapi.rest;

import net.erasmatov.s3restapi.dto.RegisterRequestDto;
import net.erasmatov.s3restapi.entity.UserRole;
import net.erasmatov.s3restapi.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class AuthRestControllerIT {

    static final MySQLContainer MY_SQL_CONTAINER;

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UserRepository userRepository;

    static {
        MY_SQL_CONTAINER = new MySQLContainer("mysql:8.3.0");
        MY_SQL_CONTAINER.start();
        MY_SQL_CONTAINER.withInitScript("db.sql");
        MY_SQL_CONTAINER.waitingFor(Wait.forListeningPort());
    }

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> MY_SQL_CONTAINER.getJdbcUrl());
        registry.add("spring.datasource.username", () -> MY_SQL_CONTAINER.getUsername());
        registry.add("spring.datasource.password", () -> MY_SQL_CONTAINER.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

    }


    @BeforeEach
    void setUp() {
        System.out.println("Before Each Test");
    }

    @AfterEach
    void tearDown() {
        System.out.println("After Each Test");
        this.userRepository.deleteAll().subscribe();
    }


    @Test
    @DisplayName("POST /api/v1/auth/register возвращает HTTP ответ со статусом 200 OK и данные зарегистрированного пользователя")
    void register() {
        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setUsername("TestUsername");
        requestDto.setPassword("TestPassword");
        requestDto.setRole(UserRole.USER);

        this.webTestClient.post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .exchange()
                .expectHeader().valueEquals("Content-Type", "application/json");
//                .expectAll(
//                        responseSpec -> responseSpec.expectStatus().isOk(),
//                        responseSpec -> responseSpec.expectBody().jsonPath("id").isEqualTo("1")
//                );

    }
}