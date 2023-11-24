package net.erasmatov.s3restapi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {
    @Id
    private Long id;
    private String username;
    private String password;
    private UserRole role;
    private List<EventEntity> events;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EntityStatus status;

    @ToString.Include(name = "password")
    private String maskPassword() {
        return "********";
    }
}
