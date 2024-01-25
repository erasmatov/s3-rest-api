package net.erasmatov.s3restapi.repository;

import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.entity.UserRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<UserEntity, Long> {
    Mono<UserEntity> findByUsername(String username);

    Flux<UserEntity> findAllByRole(UserRole role);
}
