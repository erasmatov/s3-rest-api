package net.erasmatov.s3restapi.repository;

import net.erasmatov.s3restapi.entity.FileEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface FileRepository extends R2dbcRepository<FileEntity, Long> {
    Mono<FileEntity> findByFilename(String filename);
}
