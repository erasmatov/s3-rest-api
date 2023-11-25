package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.entity.FileEntity;
import net.erasmatov.s3restapi.repository.FileRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public Mono<FileEntity> findFileById(Long id) {
        return fileRepository.findById(id);
    }
}
