MERGE INTO flights (flight_id, origin, destination, seat_class, price, total_seats, available_seats, approved)
KEY (flight_id, seat_class)
VALUES
    ('AI-101', 'Mumbai', 'Delhi', 'Economy', 5000.00, 100, 100, TRUE),
    ('AI-202', 'Delhi', 'Bengaluru', 'Business', 9200.00, 24, 24, TRUE),
    ('AI-303', 'Mumbai', 'Chennai', 'First', 15500.00, 8, 8, TRUE);
