package ee.ng.events.registration.service;

import ee.ng.events.common.exception.AlreadyRegisteredException;
import ee.ng.events.common.exception.EventCapacityExceededException;
import ee.ng.events.common.exception.EventNotFoundException;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.repository.EventRepository;
import ee.ng.events.registration.model.dto.RegistrationDto;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import ee.ng.events.registration.model.mapper.RegistrationMapper;
import ee.ng.events.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    @Transactional
    public RegistrationEntity save(RegistrationDto registrationDto) {
        EventEntity eventEntity = eventRepository
                .findById(registrationDto.getEventId())
                .orElseThrow(() -> new EventNotFoundException(registrationDto.getEventId()));

        if (isEventFull(eventEntity)) {
            throw new EventCapacityExceededException(registrationDto.getEventId());
        }

        if (registrationRepository.existsByEventEntityAndPersonalCode(eventEntity, registrationDto.getPersonalCode())) {
            throw new AlreadyRegisteredException(registrationDto.getEventId());
        }

        RegistrationEntity registrationEntity = RegistrationMapper.INSTANCE.toRegistrationEntity(registrationDto, eventEntity);

        return registrationRepository.save(registrationEntity);
    }

    private boolean isEventFull(EventEntity eventEntity) {
        long registrationCount = registrationRepository.findByEventEntity(eventEntity).size();
        return registrationCount >= eventEntity.getCapacity();
    }
}
