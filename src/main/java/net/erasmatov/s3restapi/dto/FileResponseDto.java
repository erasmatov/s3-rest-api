package net.erasmatov.s3restapi.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@ToString
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
public class FileResponseDto {
    private final String name;
    private final String path;
    private final Instant createdAt;
    private final Instant updatedAt;
}
