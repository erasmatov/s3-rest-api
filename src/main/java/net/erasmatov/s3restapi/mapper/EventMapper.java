package net.erasmatov.s3restapi.mapper;

import net.erasmatov.s3restapi.dto.EventDto;
import net.erasmatov.s3restapi.entity.EventEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventDto map(EventEntity eventEntity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "file", ignore = true)
    @InheritInverseConfiguration
    EventEntity map(EventDto eventDto);
}
