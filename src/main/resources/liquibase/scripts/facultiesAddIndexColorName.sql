-- liquibase formatted sql

-- changeset anesterov:1
CREATE INDEX idx_faculties_name_color ON faculties(name, color);