package ee.ng.events.event.service;

import ee.ng.events.event.model.dto.EventDto;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.model.mapper.EventMapper;
import ee.ng.events.event.model.projection.EventSummaryProjection;
import ee.ng.events.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventEntity save(EventDto eventDto) {
        EventEntity eventEntity = EventMapper.INSTANCE.toEventEntity(eventDto);
        return eventRepository.save(eventEntity);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryProjection> getAllSummaries() {
        return eventRepository.findAllSummaries();
    }
}
