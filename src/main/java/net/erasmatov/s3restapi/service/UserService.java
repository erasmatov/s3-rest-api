package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.UserEntity;
import net.erasmatov.s3restapi.entity.UserRole;
import net.erasmatov.s3restapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserEntity> registerUser(UserEntity user) {
        return userRepository.save(
                user.toBuilder()
                        .username(user.getUsername())
                        .password(passwordEncoder.encode(user.getPassword()))
                        .role(user.getRole())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .status(EntityStatus.ACTIVE)
                        .build()
        ).doOnSuccess(u -> {
            log.info("IN registerUser - user: {} created", u);
        });
    }

    public Mono<UserEntity> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Mono<UserEntity> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Flux<UserEntity> getUsersByRole(UserRole role) {
        return userRepository.findAllByRole(role);
    }

    public Flux<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<UserEntity> saveUser(UserEntity entity) {
        return userRepository.save(entity);
    }

    public Mono<UserEntity> deleteUserById(Long userId) {
        return userRepository.findById(userId)
                .flatMap(userEntity -> {
                    userEntity.setUpdatedAt(Instant.now());
                    userEntity.setStatus(EntityStatus.INACTIVE);
                    return userRepository.save(userEntity);
                });
    }
}
