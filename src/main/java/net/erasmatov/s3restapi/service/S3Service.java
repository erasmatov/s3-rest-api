package net.erasmatov.s3restapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private AmazonS3 s3;
    private TransferManager transferManager;

    @Value("${aws.s3-bucket-name}")
    private String bucketName;


    @SneakyThrows
    public UploadResult putObject(MultipartFile file) {
        String keyName = "file_" + UUID.randomUUID();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        Upload upload = transferManager.upload(this.bucketName, keyName, file.getInputStream(), metadata);
        UploadResult result = upload.waitForUploadResult();
        transferManager.shutdownNow(false);

        log.info("Object uploaded to bucket({}): {}", this.bucketName, keyName);
        return result;
    }

    public URL getObjectUrl(String keyName) {
        return s3.getUrl(this.bucketName, keyName);
    }

    public List<S3ObjectSummary> listObjects() {
        return s3.listObjects(bucketName).getObjectSummaries();
    }

}
