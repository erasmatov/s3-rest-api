package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.common.FileUtils;
import net.erasmatov.s3restapi.config.AwsProperties;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import net.erasmatov.s3restapi.entity.UploadState;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
        MediaType mediaType = ObjectUtils.defaultIfNull(filePart.headers().getContentType(), MediaType.APPLICATION_OCTET_STREAM);
        metadata.put("filename", filename);

        UploadState uploadState = new UploadState(filename, Objects.requireNonNull(filePart.headers().getContentType()).toString());
        CompletableFuture<CreateMultipartUploadResponse> uploadRequest = s3AsyncClient
                .createMultipartUpload(CreateMultipartUploadRequest.builder()
                        .contentType(mediaType.toString())
                        .key(filename)
                        .metadata(metadata)
                        .bucket(s3ConfigProperties.getS3BucketName())
                        .build());

        return Mono
                .fromFuture(uploadRequest)
                .flatMapMany((response) -> {
                    FileUtils.checkSdkResponse(response);
                    uploadState.setUploadId(response.uploadId());
                    return filePart.content();
                })
                .bufferUntil(dataBuffer -> {
                    uploadState.addBuffered(dataBuffer.readableByteCount());
                    if (uploadState.getBuffered() >= s3ConfigProperties.getMultipartMinPartSize()) {
                        log.info("BufferUntil - returning true, bufferedBytes={}, partCounter={}, uploadId={}",
                                uploadState.getBuffered(), uploadState.getPartCounter(), uploadState.getUploadId());
                        uploadState.setBuffered(0);
                        return true;
                    }
                    return false;
                })
                .map(FileUtils::dataBufferToByteBuffer)
                .flatMap(byteBuffer -> uploadPartObject(uploadState, byteBuffer))
                .onBackpressureBuffer()
                .reduce(uploadState, (state, completedPart) -> {
                    log.info("Completed: PartNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                    (state).getCompletedParts().put(completedPart.partNumber(), completedPart);
                    return state;
                })
                .flatMap(state -> completeMultipartUpload(uploadState))
                .map(response -> {
                    FileUtils.checkSdkResponse(response);
                    log.info("upload result: {}", response.toString());
                    return new FileResponseDto(filename, uploadState.getUploadId(), response.location(), uploadState.getContentType(), response.eTag());
                });
    }

    private Mono<CompletedPart> uploadPartObject(UploadState uploadState, ByteBuffer buffer) {
        final int partNumber = uploadState.getAddedPartCounter();
        log.info("UploadPart - partNumber={}, contentLength={}", partNumber, buffer.capacity());

        CompletableFuture<UploadPartResponse> uploadPartResponseCompletableFuture = s3AsyncClient.uploadPart(UploadPartRequest.builder()
                        .bucket(s3ConfigProperties.getS3BucketName())
                        .key(uploadState.getFileKey())
                        .partNumber(partNumber)
                        .uploadId(uploadState.getUploadId())
                        .contentLength((long) buffer.capacity())
                        .build(),
                AsyncRequestBody.fromPublisher(Mono.just(buffer)));

        return Mono
                .fromFuture(uploadPartResponseCompletableFuture)
                .map(uploadPartResult -> {
                    FileUtils.checkSdkResponse(uploadPartResult);
                    log.info("UploadPart - complete: part={}, etag={}", partNumber, uploadPartResult.eTag());
                    return CompletedPart.builder()
                            .eTag(uploadPartResult.eTag())
                            .partNumber(partNumber)
                            .build();
                });
    }

    private Mono<CompleteMultipartUploadResponse> completeMultipartUpload(UploadState uploadState) {
        log.info("CompleteUpload - fileKey={}, completedParts.size={}",
                uploadState.getFileKey(), uploadState.getCompletedParts().size());

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(uploadState.getCompletedParts().values())
                .build();

        return Mono.fromFuture(s3AsyncClient.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(s3ConfigProperties.getS3BucketName())
                .uploadId(uploadState.getUploadId())
                .multipartUpload(multipartUpload)
                .key(uploadState.getFileKey())
                .build()));
    }
}
