package ee.ng.events.registration.model.mapper;

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

    @Mapping(target = "eventId", source = "eventId")
    RegistrationDto toRegistrationDto(Long eventId, PostRegistrationRequest postRegistrationRequest);

    PostRegistrationResponse toPostRegistrationResponse(RegistrationEntity registrationEntity);
}
