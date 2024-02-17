package net.erasmatov.s3restapi.rest;

import net.erasmatov.s3restapi.TestContainersExtension;
import net.erasmatov.s3restapi.dto.AuthRequestDto;
import net.erasmatov.s3restapi.dto.UserRegisterRequestDto;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.entity.UserRole;
import net.erasmatov.s3restapi.repository.EventRepository;
import net.erasmatov.s3restapi.repository.FileRepository;
import net.erasmatov.s3restapi.repository.UserRepository;
import net.erasmatov.s3restapi.security.Pbkdf2Encoder;
import net.erasmatov.s3restapi.security.SecurityService;
import net.erasmatov.s3restapi.security.TokenDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Instant;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestContainersExtension.class)
@DirtiesContext
@AutoConfigureWebTestClient
class AuthRestControllerIT {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private Pbkdf2Encoder pbkdf2Encoder;

    @Autowired
    private WebTestClient http;

    @AfterEach
    void execute() {
        this.eventRepository.deleteAll().block();
        this.fileRepository.deleteAll().block();
        this.userRepository.deleteAll().block();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register возвращает HTTP ответ 200 OK и ответ с зарегистрированными данными пользователя")
    void handleRegisterUser_RegisterRequestIsValid_ReturnsValidResponseUserData() {
        UserRegisterRequestDto registerRequest = new UserRegisterRequestDto();
        registerRequest.setUsername("first_username");
        registerRequest.setPassword("password");
        registerRequest.setRole(UserRole.USER);

        this.http.post()
                .uri("/api/v1/auth/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(registerRequest))
                .exchange()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectBody()
                                .jsonPath("id").isNotEmpty()
                                .jsonPath("username").isEqualTo(registerRequest.getUsername())
                                .jsonPath("role").isEqualTo(registerRequest.getRole().toString())
                                .jsonPath("status").isEqualTo(EntityStatus.ACTIVE.toString())
                                .jsonPath("created_at").isNotEmpty()
                );
    }

    @Test
    @DisplayName("POST /api/v1/auth/login возвращает HTTP ответ 200 OK и данные JWT токена")
    void handleLoginUser_LoginRequestIsValid_ReturnsValidResponseJwtData() {
        String rawPassword = "secondPassword";

        UserEntity user = UserEntity.builder()
                .username("second_username")
                .password(this.pbkdf2Encoder.encode(rawPassword))
                .role(UserRole.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .status(EntityStatus.ACTIVE)
                .build();
        this.userRepository.save(user).block();


        AuthRequestDto authRequest = new AuthRequestDto();
        authRequest.setUsername(user.getUsername());
        authRequest.setPassword(rawPassword);

        this.http.post()
                .uri("/api/v1/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(authRequest))
                .exchange()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectBody()
                                .jsonPath("user_id").isNotEmpty()
                                .jsonPath("token").isNotEmpty()
                                .jsonPath("issued_at").isNotEmpty()
                                .jsonPath("expires_at").isNotEmpty()
                );
    }

    @Test
    @DisplayName("GET /api/v1/auth/info возвращает HTTP ответ 200 OK и информацию пользователя")
    void handleGetInfoUser_BearerAuthRequestIsValid_ReturnsValidResponseUserInfo() {
        String rawPassword = "thirdPassword";

        UserEntity user = UserEntity.builder()
                .username("third_username")
                .password(this.pbkdf2Encoder.encode(rawPassword))
                .role(UserRole.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .status(EntityStatus.ACTIVE)
                .build();
        this.userRepository.save(user).block();

        TokenDetails tokenDetails = this.securityService.authenticate(user.getUsername(), rawPassword).block();

        this.http.get()
                .uri("/api/v1/auth/info")
                .accept(MediaType.APPLICATION_JSON)
                .headers(http -> http.setBearerAuth(tokenDetails.getToken()))
                .exchange()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectBody()
                                .jsonPath("id").isNotEmpty()
                                .jsonPath("username").isEqualTo(user.getUsername())
                                .jsonPath("role").isEqualTo(UserRole.USER.toString())
                                .jsonPath("events").isEmpty()
                                .jsonPath("created_at").isNotEmpty()
                                .jsonPath("updated_at").isNotEmpty()
                                .jsonPath("status").isEqualTo(EntityStatus.ACTIVE.toString())
                );
    }
}
