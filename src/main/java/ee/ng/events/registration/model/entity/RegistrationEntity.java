package ee.ng.events.registration.model.entity;

import ee.ng.events.event.model.entity.EventEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", schema = "event_mgmt")
@Getter
@Setter
public class RegistrationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "personal_code", nullable = false)
    private String personalCode;
    @Column(
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
   private EventEntity eventEntity;
}
