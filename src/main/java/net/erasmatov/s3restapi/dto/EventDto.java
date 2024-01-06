package net.erasmatov.s3restapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.FileEntity;
import net.erasmatov.s3restapi.entity.UserEntity;

import java.time.Instant;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EventDto {
    private Long id;
    private UserEntity user;
    private FileEntity file;
    private Instant createdAt;
    private EntityStatus status;
}
