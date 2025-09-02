package ee.ng.events.registration.repository;

import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {
    List<RegistrationEntity> findByEventEntity(EventEntity eventEntity);
    boolean existsByEventEntityAndPersonalCode(EventEntity eventEntity, String personalCode);
}
