package net.erasmatov.s3restapi.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
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

    @Column("username")
    private String username;

    @Column("password")
    private String password;

    @Column("role")
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Transient
    private List<EventEntity> events;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("status")
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;


    @ToString.Include(name = "password")
    private String maskPassword() {
        return "********";
    }
}
