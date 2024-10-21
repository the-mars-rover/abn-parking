create table parking_rate
(
    id        bigserial primary key,
    fine_rate integer      not null,
    rate      integer      not null,
    street    varchar(255) not null unique
);

create table parking_session
(
    id            bigserial primary key,
    end_instant   timestamp(6) with time zone,
    start_instant timestamp(6) with time zone not null,
    license       varchar(255)                not null,
    street        varchar(255)                not null
);

create table vehicle_observation
(
    id                  bigserial primary key,
    verified            boolean                     not null,
    observation_instant timestamp(6) with time zone not null,
    license             varchar(255)                not null,
    street              varchar(255)                not null
);

create table parking_invoice
(
    id              bigserial primary key,
    paid            boolean                     not null,
    amount          bigint                      not null,
    invoice_instant timestamp(6) with time zone not null,
    observation_id  bigint
        constraint vehicle_observation_fk references vehicle_observation,
    session_id      bigint
        constraint parking_session_fk references parking_session
);

