package net.erasmatov.s3restapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws", ignoreUnknownFields = false)
public class AwsProperties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String S3BucketName;
    private String endpoint;
}
