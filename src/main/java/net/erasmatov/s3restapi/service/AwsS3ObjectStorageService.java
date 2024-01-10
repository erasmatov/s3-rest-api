package net.erasmatov.s3restapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.config.AwsProperties;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Service
public class AwsS3ObjectStorageService {

    private final AmazonS3 s3Client;
    private final AwsProperties s3ConfigProperties;

    public Mono<FileResponseDto> uploadObject(FilePart filePart) {
        String keyName = filePart.filename();

        CompletableFuture<PutObjectResult> uploadRequest = CompletableFuture.supplyAsync(() -> {
            byte[] file = filePart.content().map(dataBuffer -> dataBuffer.asByteBuffer().array()).blockFirst();

            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(s3ConfigProperties.getS3BucketName(),
                            keyName, new ByteArrayInputStream(file), new ObjectMetadata());
            return s3Client.putObject(putObjectRequest);
        });
        return Mono.fromFuture(uploadRequest)
                .map(objectResult -> new FileResponseDto(filePart.name(), objectResult.getVersionId().toString(), objectResult.getContentMd5().toString(), objectResult.getETag().toString(), objectResult.toString()));
    }
}
