package net.erasmatov.s3restapi.repository;

import net.erasmatov.s3restapi.entity.EventEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface EventRepository extends R2dbcRepository<EventEntity, Long> {
    Flux<EventEntity> findAllByUserId(Long userId);
}
