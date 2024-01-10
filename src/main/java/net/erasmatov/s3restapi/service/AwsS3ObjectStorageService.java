package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.common.FileUtils;
import net.erasmatov.s3restapi.config.AwsProperties;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Service
public class AwsS3ObjectStorageService {

    private final S3AsyncClient s3AsyncClient;
    private final AwsProperties s3ConfigProperties;

    public Mono<FileResponseDto> uploadObject(FilePart filePart) {

        String filename = filePart.filename();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("filename", filename);




        CompletableFuture<PutObjectResponse> uploadRequest = s3AsyncClient
                .putObject(PutObjectRequest.builder()
                        .key(filename)
                        .metadata(metadata)
                        .bucket(s3ConfigProperties.getS3BucketName())
                        .build(), AsyncRequestBody.);

        return Mono
                .fromFuture(uploadRequest)
                .map(response -> {
                    FileUtils.checkSdkResponse(response);
                    log.info("upload result: {}", response.toString());
                    return new FileResponseDto();
                });
    }
}
