package net.erasmatov.s3restapi.repository;

import net.erasmatov.s3restapi.entity.FileEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface FileRepository extends R2dbcRepository<FileEntity, Long> {
}
