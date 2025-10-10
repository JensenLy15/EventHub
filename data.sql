INSERT IGNORE INTO categories (name) VALUES
('Social'),
('Career'),
('Hackathon'),
('Meetup');

INSERT IGNORE INTO users (name, email, password, role, status)
VALUES
('Dummy',  'dummy@example.com',  'password123', 'student',  'active'),
('Dummy2', 'dummy2@example.com', 'password123', 'student',  'active'),
('Dummy3', 'dummy3@example.com', 'password123', 'student',  'active'),
('Dummy4', 'dummy4@example.com', 'password123', 'student',  'active'),
('Dummy5', 'dummy5@example.com', 'password123', 'organiser','active'),
('Dummy7', 'dummy7@example.com', 'password123', 'admin',    'active');


INSERT IGNORE INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Welcome Back BBQ',
  'Kick off social',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL -10 DAY),
  'Alumni Courtyard',
  200,
  0.00,
  'Celebrate the start of semester with food, music, and games. Meet new friends and reconnect.',
  '17:30 - Doors open\n18:00 - Welcome\n18:15 - BBQ\n19:30 - Games & prizes',
  'Host: Student Union',
  'Casual / comfy'
FROM users u
WHERE u.email = 'dummy5@example.com';

INSERT IGNORE INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Cloud Career Panel',
  'Industry speakers',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL 3 HOUR),
  'Building 80, Room 10-12',
  150,
  0.00,
  'Hear from engineers and recruiters about pathways into cloud roles, internships and graduate tips.',
  '18:00 - Check-in\n18:15 - Panel discussion\n19:00 - Q&A\n19:20 - Networking',
  'A. Chen (AWS), J. Patel (Azure), R. Nguyen (GCP)',
  'Smart casual'
FROM users u
WHERE u.email = 'dummy5@example.com';

INSERT IGNORE INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Hack Night',
  'Bring your laptop',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL 1 DAY),
  'Fab Lab',
  80,
  0.00,
  'Bring a project or join a team. Mentors on-site, food provided.',
  '18:00 - Hallo\n19:00 - Test',
  'Community mentors',
  'Casual / hoodie-friendly'
FROM users u
WHERE u.email = 'dummy5@example.com';

INSERT IGNORE INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Data Science Meetup',
  'Lightning talks',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL 5 DAY),
  'Online (Zoom)',
  500,
  0.00,
  'Lightning talks on ML ops, feature stores, and practical tips.',
  '18:00 - Doors\n18:10 - Talk 1\n18:30 - Talk 2\n18:50 - Talk 3\n19:15 - Networking',
  'Rotating community speakers',
  'Smart casual'
FROM users u
WHERE u.email = 'dummy5@example.com';

INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Social'
WHERE e.name = 'Welcome Back BBQ';

INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Career'
WHERE e.name = 'Cloud Career Panel';

INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Hackathon'
WHERE e.name = 'Hack Night';

INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Meetup'
WHERE e.name = 'Data Science Meetup';
