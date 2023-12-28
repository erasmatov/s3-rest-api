package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.common.FileUtils;
import net.erasmatov.s3restapi.config.AwsProperties;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import net.erasmatov.s3restapi.entity.UploadStatus;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
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

        Map<String, String> metadata = Map.of("filename", filename);

        MediaType mediaType = ObjectUtils.defaultIfNull(filePart.headers().getContentType(), MediaType.APPLICATION_OCTET_STREAM);

        CompletableFuture<CreateMultipartUploadResponse> s3AsyncClientMultipartUpload = s3AsyncClient
                .createMultipartUpload(CreateMultipartUploadRequest.builder()
                        .contentType(mediaType.toString())
                        .key(filename)
                        .metadata(metadata)
                        .bucket(s3ConfigProperties.getS3BucketName())
                        .build());

        UploadStatus uploadStatus = new UploadStatus(Objects.requireNonNull(filePart.headers().getContentType()).toString(), filename);

        return Mono.fromFuture(s3AsyncClientMultipartUpload)
                .flatMapMany(response -> {
                    FileUtils.checkSdkResponse(response);
                    uploadStatus.setUploadId(response.uploadId());
                    log.info("Upload object with ID={}", response.uploadId());
                    return filePart.content();
                })
                .bufferUntil(dataBuffer -> {
                    uploadStatus.addBuffered(dataBuffer.readableByteCount());

                    if (uploadStatus.getBuffered() >= s3ConfigProperties.getMultipartMinPartSize()) {
                        log.info("BufferUntil - returning true, bufferedBytes={}, partCounter={}, uploadId={}",
                                uploadStatus.getBuffered(), uploadStatus.getPartCounter(), uploadStatus.getUploadId());

                        uploadStatus.setBuffered(0);
                        return true;
                    }

                    return false;
                })
                .map(FileUtils::dataBufferToByteBuffer)
                .flatMap(byteBuffer -> uploadPartObject(uploadStatus, byteBuffer))
                .onBackpressureBuffer()
                .reduce(uploadStatus, (status, completedPart) -> {
                    log.info("Completed: PartNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                    (status).getCompletedParts().put(completedPart.partNumber(), completedPart);
                    return status;
                })
                .flatMap(uploadStatus1 -> completeMultipartUpload(uploadStatus))
                .map(response -> {
                    FileUtils.checkSdkResponse(response);
                    log.info("upload result: {}", response.toString());
                    return new FileResponseDto(filename, uploadStatus.getUploadId(), response.location(), uploadStatus.getContentType(), response.eTag());
                });
    }

    private Mono<CompletedPart> uploadPartObject(UploadStatus uploadStatus, ByteBuffer buffer) {
        final int partNumber = uploadStatus.getAddedPartCounter();
        log.info("UploadPart - partNumber={}, contentLength={}", partNumber, buffer.capacity());

        CompletableFuture<UploadPartResponse> uploadPartResponseCompletableFuture = s3AsyncClient.uploadPart(UploadPartRequest.builder()
                        .bucket(s3ConfigProperties.getS3BucketName())
                        .key(uploadStatus.getFileKey())
                        .partNumber(partNumber)
                        .uploadId(uploadStatus.getUploadId())
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

    private Mono<CompleteMultipartUploadResponse> completeMultipartUpload(UploadStatus uploadStatus) {
        log.info("CompleteUpload - fileKey={}, completedParts.size={}",
                uploadStatus.getFileKey(), uploadStatus.getCompletedParts().size());

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(uploadStatus.getCompletedParts().values())
                .build();

        return Mono.fromFuture(s3AsyncClient.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(s3ConfigProperties.getS3BucketName())
                .uploadId(uploadStatus.getUploadId())
                .multipartUpload(multipartUpload)
                .key(uploadStatus.getFileKey())
                .build()));
    }
}
