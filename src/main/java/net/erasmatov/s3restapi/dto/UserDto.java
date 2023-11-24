package net.erasmatov.s3restapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.EventEntity;
import net.erasmatov.s3restapi.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDto {
    private Long id;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private UserRole role;
    private List<EventEntity> events;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EntityStatus status;
}
