package ee.ng.events.registration.service;

import ee.ng.events.common.exception.AlreadyRegisteredException;
import ee.ng.events.common.exception.EventCapacityExceededException;
import ee.ng.events.common.exception.EventNotFoundException;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.repository.EventRepository;
import ee.ng.events.registration.model.dto.RegistrationDto;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import ee.ng.events.registration.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Captor
    ArgumentCaptor<RegistrationEntity> registrationEntityArgumentCaptor;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void save_throwEventNotFoundException() {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEventId(1L);

        doReturn(Optional.empty()).when(eventRepository).findById(registrationDto.getEventId());

        assertThatThrownBy(() -> registrationService.save(registrationDto))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event with id " + registrationDto.getEventId() + " was not found.");
    }

    @Test
    void save_throwEventCapacityExceededException() {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEventId(1L);
        registrationDto.setPersonalCode("11111111111");

        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(1L);
        eventEntity.setName("Sample Event");
        eventEntity.setCapacity(1);

        RegistrationEntity registrationEntity = new RegistrationEntity();

        doReturn(Optional.of(eventEntity)).when(eventRepository).findById(registrationDto.getEventId());
        doReturn(List.of(registrationEntity)).when(registrationRepository).findByEventEntity(eventEntity);

        assertThatThrownBy(() -> registrationService.save(registrationDto))
                .isInstanceOf(EventCapacityExceededException.class)
                .hasMessage("Event with id " + registrationDto.getEventId() + " is full.");
    }

    @Test
    void save_throwAlreadyRegisteredException() {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEventId(1L);
        registrationDto.setPersonalCode("11111111111");

        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(1L);
        eventEntity.setName("Sample Event");
        eventEntity.setCapacity(2);

        RegistrationEntity registrationEntity = new RegistrationEntity();

        doReturn(Optional.of(eventEntity)).when(eventRepository).findById(registrationDto.getEventId());
        doReturn(List.of(registrationEntity)).when(registrationRepository).findByEventEntity(eventEntity);
        doReturn(true).when(registrationRepository).existsByEventEntityAndPersonalCode(eventEntity, registrationDto.getPersonalCode());

        assertThatThrownBy(() -> registrationService.save(registrationDto))
                .isInstanceOf(AlreadyRegisteredException.class)
                .hasMessage("Registration already exists for event with id " +  registrationDto.getEventId());
    }

    @Test
    void save_success() {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEventId(1L);
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setPersonalCode("11111111111");

        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(1L);
        eventEntity.setName("Sample Event");
        eventEntity.setCapacity(2);

        RegistrationEntity registrationEntity = new RegistrationEntity();
        registrationEntity.setId(1L);
        registrationEntity.setEventEntity(eventEntity);
        registrationEntity.setFirstName("John");
        registrationEntity.setLastName("Doe");
        registrationEntity.setPersonalCode("11111111111");

        doReturn(Optional.of(eventEntity)).when(eventRepository).findById(registrationDto.getEventId());
        doReturn(List.of(new RegistrationEntity())).when(registrationRepository).findByEventEntity(eventEntity);
        doReturn(false).when(registrationRepository).existsByEventEntityAndPersonalCode(eventEntity, registrationDto.getPersonalCode());
        doReturn(registrationEntity).when(registrationRepository).save(registrationEntityArgumentCaptor.capture());

        RegistrationEntity savedRegistration = registrationService.save(registrationDto);

        assertNotNull(savedRegistration);
        assertEquals(registrationEntity, savedRegistration);
    }
}