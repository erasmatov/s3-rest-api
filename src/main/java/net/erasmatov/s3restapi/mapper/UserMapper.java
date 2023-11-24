package net.erasmatov.s3restapi.mapper;

import net.erasmatov.s3restapi.dto.UserDto;
import net.erasmatov.s3restapi.entity.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto map(UserEntity userEntity);

    @InheritInverseConfiguration
    UserEntity map(UserDto userDto);
}
