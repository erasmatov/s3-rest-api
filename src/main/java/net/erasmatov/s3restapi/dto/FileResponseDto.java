package net.erasmatov.s3restapi.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
public class FileResponseDto {

    private final String name;
    private final String uploadId;
    private final String path;
    private final String type;
    private final String eTag;
}
