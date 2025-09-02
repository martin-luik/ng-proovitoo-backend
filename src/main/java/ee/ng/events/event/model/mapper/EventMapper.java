package ee.ng.events.event.model.mapper;

import ee.ng.events.event.model.dto.EventDto;
import ee.ng.events.event.model.dto.GetEventResponse;
import ee.ng.events.event.model.dto.PostEventRequest;
import ee.ng.events.event.model.dto.PostEventResponse;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.model.projection.EventSummaryProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    EventDto toEventDto(PostEventRequest postEventRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    EventEntity toEventEntity(EventDto eventDto);

    PostEventResponse toPostEventResponse(EventEntity eventEntity);

    List<GetEventResponse> toGetEventResponses(List<EventSummaryProjection> eventSummaryProjections);
}
