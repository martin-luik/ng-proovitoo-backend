package ee.ng.events.registration.model.mapper;

import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.registration.model.dto.PostRegistrationRequest;
import ee.ng.events.registration.model.dto.PostRegistrationResponse;
import ee.ng.events.registration.model.dto.RegistrationDto;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RegistrationMapper {
    RegistrationMapper INSTANCE = Mappers.getMapper(RegistrationMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "firstName", source = "postRegistrationRequest.firstName")
    @Mapping(target = "lastName", source = "postRegistrationRequest.lastName")
    @Mapping(target = "personalCode", source = "postRegistrationRequest.personalCode")
    RegistrationDto toRegistrationDto(Long eventId, PostRegistrationRequest postRegistrationRequest);

    @Mapping(target = "eventId", source = "eventEntity.id")
    PostRegistrationResponse toPostRegistrationResponse(RegistrationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "eventEntity", source = "eventEntity")
    RegistrationEntity toRegistrationEntity(RegistrationDto dto, EventEntity eventEntity);
}
