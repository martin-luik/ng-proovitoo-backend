package ee.ng.events.event.repository;

import ee.ng.events.BaseRepositoryTest;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import ee.ng.events.registration.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;


    private EventEntity eventEntity;

    @BeforeEach
    void setUp() {
        eventEntity = new EventEntity();
        eventEntity.setCapacity(2);
        eventEntity.setName("Test Event");
        eventEntity.setStartsAt(LocalDateTime.now());
        eventEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void save() {
        EventEntity savedEvent = eventRepository.save(eventEntity);
        assertNotNull(savedEvent.getId());
        assertEquals(eventEntity.getName(), savedEvent.getName());
    }

    @Test
    void findAllSummaries_withoutRegistrations() {
        var insertedEvent = eventRepository.save(eventEntity);
        var eventSummary = eventRepository.findAllSummaries().stream()
                .filter(summary -> summary.getId().equals(insertedEvent.getId()))
                .findFirst()
                .orElse(null);

        assertEquals(eventEntity.getName(), eventSummary.getName());
        assertEquals(eventEntity.getCapacity(), eventSummary.getCapacity());
        assertEquals(0, eventSummary.getRegistrationsCount());
        assertEquals(eventEntity.getCapacity(), eventSummary.getAvailableSeats());
    }

    @Test
    void findAllSummaries_withRegistrations() {
        var insertedEvent = eventRepository.save(eventEntity);

        var registration1 = new RegistrationEntity();
        registration1.setEventEntity(insertedEvent);
        registration1.setFirstName("First Name 1");
        registration1.setLastName("Last Name 1");
        registration1.setPersonalCode("11111111111");
        registration1.setEventEntity(insertedEvent);

        var registration2 = new RegistrationEntity();
        registration2.setEventEntity(insertedEvent);
        registration2.setFirstName("First Name 2");
        registration2.setLastName("Last Name 2");
        registration2.setPersonalCode("22222222222");

        registrationRepository.saveAll(List.of(registration1, registration2));

        var eventSummary = eventRepository.findAllSummaries().stream()
                .filter(summary -> summary.getId().equals(insertedEvent.getId()))
                .findFirst()
                .orElse(null);

        assertEquals(insertedEvent.getName(), eventSummary.getName());
        assertEquals(insertedEvent.getCapacity(), eventSummary.getCapacity());
        assertEquals(2, eventSummary.getRegistrationsCount());
        assertEquals(0, eventSummary.getAvailableSeats());
    }
}