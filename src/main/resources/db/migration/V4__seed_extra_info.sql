
-- Ensure categories exist

INSERT INTO categories (name)
SELECT 'Social'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Social');

INSERT INTO categories (name)
SELECT 'Career'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Career');

INSERT INTO categories (name)
SELECT 'Hackathon'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hackathon');

INSERT INTO categories (name)
SELECT 'Meetup'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Meetup');


-- Ensure organiser exists

INSERT INTO users (name, email, password, role, status)
SELECT 'Dummy5', 'dummy5@example.com', 'password123', 'organiser', 'active'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'dummy5@example.com');


-- Update existing events from V2 with extra fields


-- Welcome Back BBQ (past)
UPDATE events
   SET detailed_description = 'Celebrate the start of semester with food, music, and games. Meet new friends and reconnect.',
       agenda               = '17:30 – Doors open
18:00 – Welcome
18:15 – BBQ
19:30 – Games & prizes',
       speakers             = 'Host: Student Union',
       dress_code           = 'Casual / comfy'
 WHERE name = 'Welcome Back BBQ';

-- Cloud Career Panel (upcoming)
UPDATE events
   SET detailed_description = 'Hear from engineers and recruiters about pathways into cloud roles, internships and graduate tips.',
       agenda               = '18:00 – Check-in
18:15 – Panel discussion
19:00 – Q&A
19:20 – Networking',
       speakers             = 'A. Chen (AWS), J. Patel (Azure), R. Nguyen (GCP)',
       dress_code           = 'Smart casual'
 WHERE name = 'Cloud Career Panel';

-- Hack Night (upcoming)
UPDATE events
   SET detailed_description = 'Bring a project or join a team. Mentors on-site, food provided.',
       agenda               = '18:00 – Hallo
19:00 – Test',
       speakers             = 'Community mentors',
       dress_code           = 'Casual / hoodie-friendly'
 WHERE name = 'Hack Night';

-- Data Science Meetup (upcoming)
UPDATE events
   SET detailed_description = 'Lightning talks on ML ops, feature stores, and practical tips.',
       agenda               = '18:00 – Doors
18:10 – Talk 1
18:30 – Talk 2
18:50 – Talk 3
19:15 – Networking',
       speakers             = 'Rotating community speakers',
       dress_code           = 'Smart casual'
 WHERE name = 'Data Science Meetup';


-- Insert events if they didn’t exist yet

-- Welcome Back BBQ (past)
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT 'Welcome Back BBQ', 'Kick off social',
       (SELECT user_id FROM users WHERE email='dummy5@example.com'),
       DATEADD('DAY', -10, CURRENT_TIMESTAMP),
       'Alumni Courtyard', 200, 0.00,
       'Celebrate the start of semester with food, music, and games. Meet new friends and reconnect.',
       '17:30 – Doors open
18:00 – Welcome
18:15 – BBQ
19:30 – Games & prizes',
       'Host: Student Union',
       'Casual / comfy'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE name = 'Welcome Back BBQ');

-- Cloud Career Panel
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT 'Cloud Career Panel', 'Industry speakers',
       (SELECT user_id FROM users WHERE email='dummy5@example.com'),
       DATEADD('HOUR', 3, CURRENT_TIMESTAMP),
       'Building 80, Room 10-12', 150, 0.00,
       'Hear from engineers and recruiters about pathways into cloud roles, internships and graduate tips.',
       '18:00 – Check-in
18:15 – Panel discussion
19:00 – Q&A
19:20 – Networking',
       'A. Chen (AWS), J. Patel (Azure), R. Nguyen (GCP)',
       'Smart casual'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE name = 'Cloud Career Panel');

-- Hack Night
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT 'Hack Night', 'Bring your laptop',
       (SELECT user_id FROM users WHERE email='dummy5@example.com'),
       DATEADD('DAY', 1, CURRENT_TIMESTAMP),
       'Fab Lab', 80, 0.00,
       'Bring a project or join a team. Mentors on-site, food provided.',
       '18:00 – Hallo
19:00 – Test',
       'Community mentors',
       'Casual / hoodie-friendly'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE name = 'Hack Night');

-- Data Science Meetup
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT 'Data Science Meetup', 'Lightning talks',
       (SELECT user_id FROM users WHERE email='dummy5@example.com'),
       DATEADD('DAY', 5, CURRENT_TIMESTAMP),
       'Online (Zoom)', 500, 0.00,
       'Lightning talks on ML ops, feature stores, and practical tips.',
       '18:00 – Doors
18:10 – Talk 1
18:30 – Talk 2
18:50 – Talk 3
19:15 – Networking',
       'Rotating community speakers',
       'Smart casual'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE name = 'Data Science Meetup');

-- Ensure event_categories mappings exist

-- BBQ → Social
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM   events e
JOIN   categories c ON c.name = 'Social'
WHERE  e.name = 'Welcome Back BBQ'
AND    NOT EXISTS (
  SELECT 1 FROM event_categories ec
  WHERE ec.event_id = e.event_id AND ec.category_id = c.category_id
);

-- Cloud Career Panel → Career
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM   events e
JOIN   categories c ON c.name = 'Career'
WHERE  e.name = 'Cloud Career Panel'
AND    NOT EXISTS (
  SELECT 1 FROM event_categories ec
  WHERE ec.event_id = e.event_id AND ec.category_id = c.category_id
);

-- Hack Night → Hackathon
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM   events e
JOIN   categories c ON c.name = 'Hackathon'
WHERE  e.name = 'Hack Night'
AND    NOT EXISTS (
  SELECT 1 FROM event_categories ec
  WHERE ec.event_id = e.event_id AND ec.category_id = c.category_id
);

-- Data Science Meetup → Meetup
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM   events e
JOIN   categories c ON c.name = 'Meetup'
WHERE  e.name = 'Data Science Meetup'
AND    NOT EXISTS (
  SELECT 1 FROM event_categories ec
  WHERE ec.event_id = e.event_id AND ec.category_id = c.category_id
);