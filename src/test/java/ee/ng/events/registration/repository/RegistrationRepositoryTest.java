package ee.ng.events.registration.repository;

import ee.ng.events.BaseRepositoryTest;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.repository.EventRepository;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RegistrationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private EventEntity eventEntity;

    @BeforeEach
    void setup() {
        eventEntity = new EventEntity();
        eventEntity.setCapacity(2);
        eventEntity.setName("Test Event");
        eventEntity.setStartsAt(LocalDateTime.now());
        eventEntity.setCreatedAt(LocalDateTime.now());
        eventEntity = eventRepository.save(eventEntity);
    }

    @Test
    void save() {
        RegistrationEntity registrationEntity = new RegistrationEntity();
        registrationEntity.setEventEntity(eventEntity);
        registrationEntity.setFirstName("FirstName");
        registrationEntity.setLastName("LastName");
        registrationEntity.setPersonalCode("12345678901");
        registrationEntity.setCreatedAt(LocalDateTime.now());

        RegistrationEntity insertedRegistrationEntity = registrationRepository.save(registrationEntity);
        assertEquals(registrationEntity, insertedRegistrationEntity);
    }


}