package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.dto.FileDto;
import net.erasmatov.s3restapi.mapper.FileMapper;
import net.erasmatov.s3restapi.security.CustomPrincipal;
import net.erasmatov.s3restapi.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileRestControllerV1 {

    private final FileService fileService;
    private final FileMapper fileMapper;

    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR')")
    @PostMapping("/upload")
    public Mono<FileDto> uploadFile(Authentication authentication, @RequestPart("file-data") FilePart fileData) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return fileService.uploadFile(fileData, principal.getName())
                .map(fileMapper::map);
    }

    @GetMapping("/{fileId}")
    public Mono<FileDto> getFile(Authentication authentication, @PathVariable("fileId") Long fileId) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        if (authentication.getAuthorities().contains("ADMIN")) {
            return fileService.getFileById(fileId)
                    .map(fileMapper::map);
        } else if (authentication.getAuthorities().contains("MODERATOR")) {
            return Flux.merge(fileService.getUserFiles(), fileService.getFilesByUserId(principal.getId()))
                    .distinct()
                    .filter(fileEntity -> fileEntity.getId().equals(fileId))
                    .singleOrEmpty()
                    .map(fileMapper::map);
        } else {
            return fileService.getFilesByUserId(principal.getId())
                    .distinct()
                    .filter(fileEntity -> fileEntity.getId().equals(fileId))
                    .singleOrEmpty()
                    .map(fileMapper::map);
        }
    }

    @GetMapping
    public Flux<FileDto> getFiles(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();
        return fileService.getFilesByUserId(customPrincipal.getId())
                .map(fileMapper::map);
    }

    @DeleteMapping("/{fileId}")
    public Mono<ResponseEntity<Void>> deleteFile(@PathVariable("fileId") Long fileId) {
        return fileService.deleteFileById(fileId)
                .map(fileEntity -> ResponseEntity.status(HttpStatus.OK).build());
    }
}