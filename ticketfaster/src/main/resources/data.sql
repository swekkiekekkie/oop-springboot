INSERT INTO concerts (id, artist, location, concert_year, total_seats, cancelled) VALUES
(1, 'Aurora Lights', 'Arnhem Gelredome', 2027, 200, FALSE),
(2, 'Neon Pulse', 'Ziggo Dome', 2027, 150, FALSE),
(3, 'Retro Wave', '013 Tilburg', 2026, 120, FALSE);

INSERT INTO visitors (id, name, vip, wishes) VALUES
(1, 'Alice', TRUE, 'Glutenvrij eten'),
(2, 'Bob', FALSE, NULL),
(3, 'Chloe', TRUE, 'Rolstoeltoegankelijke plek');

INSERT INTO ticket_purchases (visitor_id, concert_id, quantity) VALUES
(1, 1, 2),
(2, 1, 3),
(3, 2, 1);
