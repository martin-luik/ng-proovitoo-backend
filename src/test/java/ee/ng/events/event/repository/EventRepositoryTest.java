package ee.ng.events.event.repository;

import ee.ng.events.BaseRepositoryTest;
import ee.ng.events.event.model.entity.EventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    private EventEntity eventEntity;

    @BeforeEach
    void setUp() {
        eventEntity = new EventEntity();
        eventEntity.setCapacity(2);
        eventEntity.setTitle("Test Event");
        eventEntity.setStartAt(LocalDateTime.now());
        eventEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void save() {
        EventEntity savedEvent = eventRepository.save(eventEntity);
        assertNotNull(savedEvent.getId());
        assertEquals(eventEntity.getTitle(), savedEvent.getTitle());
    }

}