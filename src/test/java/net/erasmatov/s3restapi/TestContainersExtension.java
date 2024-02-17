package net.erasmatov.s3restapi;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.UUID;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class TestContainersExtension implements BeforeAllCallback, AfterAllCallback {
    private MySQLContainer<?> mySqlContainer;
    private LocalStackContainer localStackContainer;
    private String BUCKET_NAME;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws IOException, InterruptedException {
        mySqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.3-oracle"))
                .withDatabaseName("db_module25")
                .withUsername("root")
                .withPassword("password")
                .withExposedPorts(3306);

        localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0.2"))
                .withServices(S3);

        mySqlContainer.start();
        localStackContainer.start();

        BUCKET_NAME = UUID.randomUUID().toString();
        localStackContainer.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);

        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s",
                mySqlContainer.getHost(),
                mySqlContainer.getFirstMappedPort(),
                mySqlContainer.getDatabaseName());
        System.setProperty("spring.flyway.url", jdbcUrl);

        String r2dbcUrl = String.format("r2dbc:mysql://%s:%d/%s",
                mySqlContainer.getHost(),
                mySqlContainer.getFirstMappedPort(),
                mySqlContainer.getDatabaseName());
        System.setProperty("spring.r2dbc.url", r2dbcUrl);

        System.setProperty("aws.access-key", localStackContainer.getAccessKey());
        System.setProperty("aws.secret-key", localStackContainer.getSecretKey());
        System.setProperty("aws.region", localStackContainer.getRegion());
        System.setProperty("aws.s3-bucket-name", BUCKET_NAME);
        System.setProperty("aws.endpoint", localStackContainer.getEndpointOverride(S3).toString());
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {

    }
}
