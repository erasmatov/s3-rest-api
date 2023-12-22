package net.erasmatov.s3restapi.rest;

import com.amazonaws.services.s3.transfer.model.UploadResult;
import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileRestControllerV1 {
    private S3Service s3Service;

    @PostMapping(value = "/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        UploadResult result = s3Service.putObject(file);
        return ResponseEntity.ok(result.getKey());
    }

}