package net.erasmatov.s3restapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import net.erasmatov.s3restapi.entity.EntityStatus;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FileDto {
    private Long id;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EntityStatus status;
}
