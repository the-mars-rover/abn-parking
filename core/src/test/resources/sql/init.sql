-- Insert data into parking_rate table
insert into parking_rate(fine_rate, rate, street)
values (10000, 100, 'Europaplein');

insert into parking_session(license, street, start_instant)
values ('ALREADY_PARKING', 'Europaplein', '2024-01-06 20:00:00.000000 +00:00'),
       ('PARKING_FREELY', 'Free Street', '2024-01-06 20:00:00.000000 +00:00'),
       ('PARKING_LONG', 'Europaplein', '2023-12-30 21:00:00.000000 +00:00');