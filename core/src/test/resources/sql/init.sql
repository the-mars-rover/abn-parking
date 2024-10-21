-- Insert data into parking_rate table
insert into parking_rate(fine_rate, rate, street)
values (10000, 100, 'Europaplein');

insert into parking_session(license, street, start_instant)
values ('ALREADY_PARKING', 'Europaplein', DATEADD(MINUTE, -1, CURRENT_TIMESTAMP));