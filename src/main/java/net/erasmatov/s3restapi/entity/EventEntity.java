package net.erasmatov.s3restapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("events")
public class EventEntity {
    @Id
    private Long id;

    private Long userId;
    private Long fileId;

    @Transient
    private UserEntity user;

    @Transient
    private FileEntity file;

    private LocalDateTime createdAt;
    private EntityStatus status;
}
