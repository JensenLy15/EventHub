INSERT INTO categories (name) VALUES ('Social');      -- will get category_id = 1
INSERT INTO categories (name) VALUES ('Career');      -- category_id = 2
INSERT INTO categories (name) VALUES ('Hackathon');   -- category_id = 3
INSERT INTO categories (name) VALUES ('Meetup');      -- category_id = 4


-- Dummy users 
INSERT INTO users (name, email, password, role, status)
VALUES ('Dummy', 'dummy@example.com', 'password123', 'student', 'active');

INSERT INTO users (name, email, password, role, status)
VALUES ('Dummy2', 'dummy2@example.com', 'password123', 'student', 'active');

INSERT INTO users (name, email, password, role, status)
VALUES ('Dummy3', 'dummy3@example.com', 'password123', 'student', 'active');

INSERT INTO users (name, email, password, role, status)
VALUES ('Dummy4', 'dummy4@example.com', 'password123', 'student', 'active');

INSERT INTO users (name, email, password, role, status)
VALUES ('Dummy5', 'dummy5@example.com', 'password123', 'organiser', 'active');

-- Past event (won't show in "upcoming")
INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price)
VALUES ('Welcome Back BBQ', 'Kick off social', 5, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'Alumni Courtyard', 200, 0.00);
-- BBQ → Social
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e, categories c
WHERE e.name='Welcome Back BBQ' AND c.name='Social';

-- Upcoming events (should show, sorted by date_time)
INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price)
VALUES ('Cloud Career Panel', 'Industry speakers', 5, DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 'Building 80, Room 10-12', 150, 0.00);

-- Panel → Career
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e, categories c
WHERE e.name='Cloud Career Panel' AND c.name='Career';

INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price)
VALUES ('Hack Night', 'Bring your laptop', 5, DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'Fab Lab', 80, 0.00);

-- Hack Night → Hackathon
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e, categories c
WHERE e.name='Hack Night' AND c.name='Hackathon';

INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price)
VALUES ('Data Science Meetup', 'Lightning talks', 5, DATEADD('DAY', 5, CURRENT_TIMESTAMP), 'Online (Zoom)', 500, 0.00);

-- Meetup → Meetup
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e, categories c
WHERE e.name='Data Science Meetup' AND c.name='Meetup';