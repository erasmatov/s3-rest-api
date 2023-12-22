package net.erasmatov.s3restapi.mapper;

import net.erasmatov.s3restapi.dto.FileDto;
import net.erasmatov.s3restapi.entity.FileEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileDto map(FileEntity fileEntity);

    @InheritInverseConfiguration
    FileEntity map(FileDto fileDto);
}
