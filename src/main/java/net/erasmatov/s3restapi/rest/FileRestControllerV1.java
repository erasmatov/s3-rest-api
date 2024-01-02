package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.common.FileUtils;
import net.erasmatov.s3restapi.security.CustomPrincipal;
import net.erasmatov.s3restapi.service.FileService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileRestControllerV1 {

    private final FileService fileService;

    @PostMapping("/upload")
    public Mono<?> upload(Principal principal, @RequestPart("file-data") Mono<FilePart> filePart) {

        CustomPrincipal customPrincipal = (CustomPrincipal) principal;

        return filePart.map(file -> {
            FileUtils.filePartValidator(file);
            return file;
        })

    }


}