package net.erasmatov.s3restapi.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AwsS3Config {

    private final AwsProperties s3ConfigProperties;

    @Bean
    public BasicAWSCredentials awsCredentials() {
        return new BasicAWSCredentials(s3ConfigProperties.getAccessKey(), s3ConfigProperties.getSecretKey());
    }

    @Bean
    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                .withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(s3ConfigProperties.getEndpoint(), s3ConfigProperties.getRegion()))
                .build();
    }
}
