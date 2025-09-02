CREATE TABLE event_mgmt.events
(
    id         BIGSERIAL PRIMARY KEY,
    name       TEXT                     NOT NULL,
    starts_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity   INTEGER                  NOT NULL CHECK (capacity > 0),
    created_at timestamp with time zone default CURRENT_TIMESTAMP
);

ALTER TABLE event_mgmt.events
    OWNER TO events_adm_user;

CREATE TABLE event_mgmt.registrations
(
    id            BIGSERIAL PRIMARY KEY,
    event_id      BIGINT NOT NULL REFERENCES event_mgmt.events (id) ON DELETE CASCADE,
    first_name    TEXT   NOT NULL,
    last_name     TEXT   NOT NULL,
    personal_code TEXT   NOT NULL,
    created_at    timestamp with time zone default CURRENT_TIMESTAMP,
    CONSTRAINT uniq_reg_per_event UNIQUE (event_id, personal_code)
);

CREATE INDEX idx_reg_event ON event_mgmt.registrations (event_id);

ALTER TABLE event_mgmt.registrations
    OWNER TO events_adm_user;

