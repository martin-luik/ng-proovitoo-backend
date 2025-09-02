CREATE SCHEMA event_mgmt;
CREATE SCHEMA liquibase;

CREATE ROLE events_adm_role;
CREATE USER events_adm_user WITH PASSWORD 'events_admin';
GRANT events_adm_role TO events_adm_user;

GRANT USAGE ON SCHEMA event_mgmt TO events_adm_role;
GRANT USAGE ON SCHEMA liquibase TO events_adm_role;

grant all privileges on schema event_mgmt to events_adm_role;
grant all privileges on schema liquibase to events_adm_role;
