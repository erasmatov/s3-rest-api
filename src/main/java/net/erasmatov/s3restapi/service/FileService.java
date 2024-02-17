package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import net.erasmatov.s3restapi.dto.FileUpdateRequestDto;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.EventEntity;
import net.erasmatov.s3restapi.entity.FileEntity;
import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.repository.EventRepository;
import net.erasmatov.s3restapi.repository.FileRepository;
import net.erasmatov.s3restapi.repository.UserRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final EventRepository eventRepository;
    private final AwsS3ObjectStorageService objectStorageService;

    public Mono<FileEntity> uploadFile(FilePart filePart, String username) {
        return Mono.zip(objectStorageService.uploadObject(filePart),
                        userRepository.findByUsername(username))
                .flatMap(tuples -> {
                    FileResponseDto fileResponseDto = tuples.getT1();
                    UserEntity userEntity = tuples.getT2();

                    FileEntity fileEntity = FileEntity.builder()
                            .filename(fileResponseDto.getName())
                            .location(fileResponseDto.getPath())
                            .createdAt(fileResponseDto.getCreatedAt())
                            .updatedAt(fileResponseDto.getUpdatedAt())
                            .status(EntityStatus.ACTIVE)
                            .build();
                    return fileRepository.save(fileEntity)
                            .flatMap(savedFileEntity -> {
                                EventEntity eventEntity = EventEntity.builder()
                                        .userId(userEntity.getId())
                                        .fileId(savedFileEntity.getId())
                                        .createdAt(Instant.now())
                                        .status(EntityStatus.ACTIVE)
                                        .build();
                                return eventRepository.save(eventEntity)
                                        .thenReturn(fileEntity);
                            });
                });
    }

    public Mono<FileEntity> deleteFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .flatMap(fileEntity -> {
                    fileEntity.setUpdatedAt(Instant.now());
                    fileEntity.setStatus(EntityStatus.INACTIVE);
                    return fileRepository.save(fileEntity);
                });
    }

    public Mono<FileEntity> getFileById(Long fileId) {
        return fileRepository.findById(fileId);
    }

    public Mono<FileEntity> saveFile(FileEntity entity) {
        return fileRepository.save(entity);
    }

    public Mono<FileEntity> updateFile(Long fileId, FileUpdateRequestDto dto) {
        return fileRepository.findById(fileId)
                .flatMap(fileEntity -> {
                    FileEntity file = fileEntity.toBuilder()
                            .filename(dto.getFilename())
                            .location(dto.getLocation())
                            .status(dto.getStatus())
                            .build();
                    return fileRepository.save(file);
                });
    }

    public Flux<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }
}
