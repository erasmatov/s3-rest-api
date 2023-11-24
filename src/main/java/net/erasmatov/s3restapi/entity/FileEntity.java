package net.erasmatov.s3restapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("files")
public class FileEntity {
    @Id
    private Long id;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EntityStatus status;
}
