package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.entity.FileEntity;
import net.erasmatov.s3restapi.repository.FileRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public Flux<FileEntity> findAllFiles() {
        return fileRepository.findAll();
    }

    public Mono<FileEntity> findFileById(Long id) {
        return fileRepository.findById(id);
    }

    public Mono<FileEntity> findFileByFilename(String filename) {
        return fileRepository.findByFilename(filename);
    }

}
