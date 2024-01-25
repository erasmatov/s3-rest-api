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
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.time.Instant;

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
        String path = "https://" + s3ConfigProperties.getS3BucketName() + "." + s3ConfigProperties.getEndpoint() + "/" + keyName;

        return filePart.content()
                .flatMap(dataBuffer -> {
                    ByteBuffer content = ByteBuffer.allocate(dataBuffer.readableByteCount());
                    dataBuffer.toByteBuffer(content);
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(content.array());
                    PutObjectRequest objectRequest = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);

                    try {
                        amazonS3.putObject(objectRequest);
                    } catch (SdkClientException e) {
                        return Mono.error(e);
                    }

                    return Mono.just(new FileResponseDto(keyName, path, instantTime, instantTime));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .single();
    }
}
