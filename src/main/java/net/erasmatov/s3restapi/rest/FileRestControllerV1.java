package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.service.FileService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileRestControllerV1 {

    private final FileService fileService;

    @PostMapping("/upload")
    public Mono<?> upload(Authentication authentication, @RequestPart("file-data") Mono<FilePart> filePart) {
        return filePart.map(file -> fileService.uploadFile(file, authentication.getName()));
    }

}