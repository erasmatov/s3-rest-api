package net.erasmatov.s3restapi.repository;

import net.erasmatov.s3restapi.entity.EventEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface EventRepository extends R2dbcRepository<EventEntity, Long> {
}
