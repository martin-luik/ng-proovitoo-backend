package ee.ng.events.event.service;

import ee.ng.events.event.model.dto.EventDto;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.model.mapper.EventMapper;
import ee.ng.events.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Captor
    ArgumentCaptor<EventEntity> eventEntityCaptor;

    @Test
    void save() {
        EventDto eventDto = new EventDto();
        eventDto.setName("Sample Event");
        eventDto.setStartsAt(LocalDateTime.parse("2024-12-31T23:59:59"));
        eventDto.setCapacity(100);

        EventEntity eventEntity = EventMapper.INSTANCE.toEventEntity(eventDto);

        doReturn(eventEntity).when(eventRepository).save(eventEntityCaptor.capture());

        EventEntity savedEventEntity = eventService.save(eventDto);

        assertEquals(eventEntity, savedEventEntity);
    }
}