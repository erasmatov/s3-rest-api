package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.dto.FileResponseDto;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.EventEntity;
import net.erasmatov.s3restapi.entity.FileEntity;
import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.repository.FileRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final AwsS3ObjectStorageService objectStorageService;
    private final UserService userService;
    private final EventService eventService;

    public Mono<FileEntity> uploadFile(FilePart filePart, String username) {

        return Mono.zip(objectStorageService.uploadObject(filePart),
                        userService.findUserByUsername(username))
                .flatMap(tuples -> {
                    FileResponseDto fileResponseDto = tuples.getT1();
                    UserEntity userEntity = tuples.getT2();

                    FileEntity fileEntity = FileEntity.builder()
                            .filename(fileResponseDto.getName())
                            .location(fileResponseDto.getPath())
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

    public Flux<FileEntity> findAllFiles() {
        return fileRepository.findAll();
    }

    public Mono<FileEntity> findFileById(Long id) {
        return fileRepository.findById(id);
    }

    public Mono<FileEntity> findFileByFilename(String filename) {
        return fileRepository.findByFilename(filename);
    }

    public Mono<FileEntity> saveFile(FileEntity entity) {
        return fileRepository.save(entity);
    }

}
