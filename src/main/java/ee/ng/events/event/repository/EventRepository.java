package ee.ng.events.event.repository;

import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.model.projection.EventSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    @Query("""
              SELECT e.id as id, e.name as name, e.startsAt as startsAt, e.capacity as capacity,
                     count(r.id) as registrationsCount
              FROM EventEntity e
              LEFT JOIN RegistrationEntity r on r.eventEntity.id = e.id
              GROUP BY e.id, e.name, e.startsAt, e.capacity ORDER BY e.id DESC
            """)
    List<EventSummaryProjection> findAllSummaries();

}
