package net.erasmatov.s3restapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
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

    @Column("user_id")
    private Long userId;

    @Column("file_id")
    private Long fileId;

    @Transient
    private UserEntity user;

    @Transient
    private FileEntity file;

    @Column("created_At")
    private LocalDateTime createdAt;

    @Column("status")
    private EntityStatus status;
}
