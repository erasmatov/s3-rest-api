package net.erasmatov.s3restapi.rest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import net.erasmatov.s3restapi.TestContainersExtension;
import net.erasmatov.s3restapi.config.AwsProperties;
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
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestContainersExtension.class)
@DirtiesContext
@AutoConfigureWebTestClient
class FileRestControllerIT {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private AwsProperties awsProperties;

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

    private final MockMultipartFile file = new MockMultipartFile(
            "testfile",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
    );

    @Test
    @DisplayName("POST /api/v1/files/upload возвращает HTTP ответ 200 ОК и данные загруженного файла")
    void handleUploadFile_BearerTokenAndFileIsValid_ReturnsValidResponseFileData() {
        String rawPassword = "firstPassword";

        UserEntity user = UserEntity.builder()
                .username("first_username")
                .password(this.pbkdf2Encoder.encode(rawPassword))
                .role(UserRole.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .status(EntityStatus.ACTIVE)
                .build();
        this.userRepository.save(user).block();

        TokenDetails tokenDetails = this.securityService.authenticate(user.getUsername(), rawPassword).block();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file-data", file.getResource());

        this.http.post()
                .uri("/api/v1/files/upload")
                .headers(http -> http.setBearerAuth(tokenDetails.getToken()))
                .bodyValue(builder.build())
                .exchange()
                .expectAll(
                        http -> http.expectStatus().isOk(),
                        http -> http.expectBody()
                                .jsonPath("id").isNotEmpty()
                                .jsonPath("filename").isEqualTo(file.getOriginalFilename())
                                .jsonPath("location").isNotEmpty()
                                .jsonPath("created_at").isNotEmpty()
                                .jsonPath("updated_at").isNotEmpty()
                                .jsonPath("status").isEqualTo(EntityStatus.ACTIVE.toString()));

        S3Object s3Object = this.amazonS3.getObject(this.awsProperties.getS3BucketName(), file.getOriginalFilename());

        assertEquals(s3Object.getKey(), file.getOriginalFilename());
    }
}