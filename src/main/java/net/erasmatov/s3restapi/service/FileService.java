package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import net.erasmatov.s3restapi.entity.*;
import net.erasmatov.s3restapi.mapper.FileMapper;
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
    private final UserService userService;
    private final EventService eventService;
    private final AwsS3ObjectStorageService objectStorageService;
    private final FileMapper fileMapper;

    public Mono<FileEntity> uploadFile(FilePart filePart, String username) {
        return Mono.zip(objectStorageService.uploadObject(filePart),
                        userService.findUserByUsername(username))
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
                                return eventService.saveEvent(eventEntity)
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

    public Flux<FileEntity> getFilesByUserId(Long userId) {
        return eventRepository.findAllByUserId(userId)
                .map(EventEntity::getFileId)
                .flatMap(fileRepository::findById);
    }

    public Flux<FileEntity> getUserFiles() {
        return userRepository.findAllByRole(UserRole.USER)
                .map(UserEntity::getId)
                .flatMap(eventRepository::findAllByUserId)
                .map(EventEntity::getFileId)
                .flatMap(fileRepository::findById);
    }
}
