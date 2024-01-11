package net.erasmatov.s3restapi.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.config.AwsProperties;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Service
public class AwsS3ObjectStorageService {

    private final AwsProperties s3ConfigProperties;
    private final AmazonS3 amazonS3;

    public Mono<FileResponseDto> uploadObject(FilePart filePart) {
        String keyName = filePart.filename();
        String bucketName = s3ConfigProperties.getS3BucketName();
        Instant instantTime = Instant.now();
        String path = "https://"
                + s3ConfigProperties.getS3BucketName()
                + "."
                + s3ConfigProperties.getEndpoint()
                + "/"
                + keyName;

        CompletableFuture.runAsync(() -> {
            ByteBuffer dataByteBuffer = filePart.content().flatMap(dataBuffer -> {
                ByteBuffer content = ByteBuffer.allocate(dataBuffer.readableByteCount());
                dataBuffer.toByteBuffer(content);
                return Mono.just(content);
            }).blockFirst();

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(Objects.requireNonNull(dataByteBuffer).capacity());

            ByteArrayInputStream inputStream = new ByteArrayInputStream(dataByteBuffer.array());

            PutObjectRequest objectRequest = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);

            try {
                amazonS3.putObject(objectRequest);
            } catch (SdkClientException e) {
                throw new RuntimeException(e);
            }
        });

        return Mono.just(new FileResponseDto(keyName, path, instantTime, instantTime));
    }
}
