-- ============================================
-- 1. CATEGORIES
INSERT INTO categories (name) VALUES
('Social'),
('Career'),
('Hackathon'),
('Meetup'),
('Sports'),
('Festival')
ON DUPLICATE KEY UPDATE name = VALUES(name);




-- ============================================
-- 2. USERS
-- Password for all: "password123"
INSERT INTO users (name, email, password, role, status)
VALUES
('Dummy',  'dummy@example.com',   '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'student',   'active'),
('Dummy2', 'dummy2@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'student',   'active'),
('Dummy3', 'dummy3@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'student',   'active'),
('Dummy4', 'dummy4@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'student',   'active'),
('Dummy5', 'dummy5@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'organiser', 'active'),
('Dummy6', 'dummy6@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'organiser', 'active'),
('Dummy7', 'dummy7@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'admin',     'active'),
('Dummy8', 'dummy8@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'student',   'banned'),
('Dummy9', 'dummy9@example.com',  '$2a$10$CG.8xKAlPySkRUPmMbkdOe40AQOfgMoTg2u7KvOMc8MTI/NTb3pAu', 'student',   'suspended')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================
-- 3. EVENTS (Created by Dummy5)

-- Event 1: Past event
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Welcome Back BBQ',
  'Kick off social',
  u.user_id,
  DATE_SUB(NOW(), INTERVAL 10 DAY),
  'Alumni Courtyard',
  200,
  0.00,
  'Celebrate the start of semester with food, music, and games. Meet new friends and reconnect.',
  '17:30 - Doors open\n18:00 - Welcome\n18:15 - BBQ\n19:30 - Games & prizes',
  'Host: Student Union',
  'Casual / comfy'
FROM users u
WHERE u.email = 'dummy5@example.com';

-- Event 2: Happening soon
INSERT INTO events (
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

-- Event 3: Tomorrow
INSERT INTO events (
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
  '18:00 - Hello\n19:00 - Test',
  'Community mentors',
  'Casual / hoodie-friendly'
FROM users u
WHERE u.email = 'dummy5@example.com';

-- Event 4: In 5 days
INSERT INTO events (
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

-- Event 5: In 7 days
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'AI Innovation Expo',
  'Showcase of cutting-edge AI projects by students and startups.',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL 7 DAY),
  'Building 12, Level 2 Atrium',
  300,
  10.00,
  'Explore booths, demos, and talks on the latest trends in AI, machine learning, and robotics.',
  '10:00 - Registration\n11:00 - Opening keynote\n12:00 - Booths open\n16:00 - Awards',
  'Various AI Researchers and Industry Guests',
  'Smart casual'
FROM users u
WHERE u.email = 'dummy5@example.com';

-- Event 6: In 14 days
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Summer Coding Bootcamp',
  'Intensive weekend workshop for web development beginners.',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL 14 DAY),
  'Building 80, Room 5-09',
  100,
  0.00,
  'Learn HTML, CSS, JavaScript, and Git basics in a hands-on workshop with mentors.',
  '09:00 - Setup\n09:30 - HTML & CSS\n11:30 - JS Basics\n13:00 - Lunch\n14:00 - Mini project',
  'Web Dev Club Mentors',
  'Casual'
FROM users u
WHERE u.email = 'dummy5@example.com';

-- ============================================
-- 4. EVENTS (Created by Dummy6)

-- Event 7: In 9 days
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Startup Pitch Night',
  'Watch student founders pitch their startups to investors.',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL 9 DAY),
  'Building 16, Lecture Theatre 8',
  120,
  5.00,
  'Join us for a night of pitches, feedback, and networking with VCs and mentors.',
  '17:30 - Registration\n18:00 - Pitches start\n19:30 - Networking',
  'Panel of Angel Investors',
  'Business casual'
FROM users u
WHERE u.email = 'dummy6@example.com';

-- Event 8: In 12 days
INSERT INTO events (
  name, description, created_by_user_id, date_time, location, capacity, price,
  detailed_description, agenda, speakers, dress_code
)
SELECT
  'Outdoor Sports Festival',
  'Fun day with team games and activities at Princes Park.',
  u.user_id,
  DATE_ADD(NOW(), INTERVAL 12 DAY),
  'Princes Park',
  250,
  0.00,
  'Participate in soccer, volleyball, and relay races. Free snacks and prizes!',
  '09:00 - Opening\n09:30 - Team formation\n10:00 - Games start\n15:00 - Awards',
  'Sports Committee',
  'Activewear'
FROM users u
WHERE u.email = 'dummy6@example.com';






-- ============================================
-- 5. EVENT CATEGORIES INSERTS


-- Welcome Back BBQ → Social
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Social'
WHERE e.name = 'Welcome Back BBQ';

-- Cloud Career Panel → Career
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Career'
WHERE e.name = 'Cloud Career Panel';

-- Hack Night → Hackathon
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Hackathon'
WHERE e.name = 'Hack Night';

-- Data Science Meetup → Meetup
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Meetup'
WHERE e.name = 'Data Science Meetup';

-- AI Innovation Expo → Career
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Career'
WHERE e.name = 'AI Innovation Expo';

-- Summer Coding Bootcamp → Hackathon
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Hackathon'
WHERE e.name = 'Summer Coding Bootcamp';

-- Startup Pitch Night → Career
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Career'
WHERE e.name = 'Startup Pitch Night';

-- Outdoor Sports Festival → Sports
INSERT INTO event_categories (event_id, category_id)
SELECT e.event_id, c.category_id
FROM events e
JOIN categories c ON c.name = 'Sports'
WHERE e.name = 'Outdoor Sports Festival';




-- ============================================
-- 6. RSVPs

-- RSVPs for Cloud Career Panel
INSERT INTO rsvp (user_id, event_id)
SELECT u.user_id, e.event_id
FROM users u
JOIN events e ON e.name = 'Cloud Career Panel'
WHERE u.email IN (
  'dummy2@example.com',
  'dummy3@example.com',
  'dummy4@example.com',
  'dummy8@example.com'
);

-- RSVPs for Hack Night
INSERT INTO rsvp (user_id, event_id)
SELECT u.user_id, e.event_id
FROM users u
JOIN events e ON e.name = 'Hack Night'
WHERE u.email IN (
  'dummy@example.com',
  'dummy2@example.com',
  'dummy5@example.com',
  'dummy6@example.com'
);

-- RSVPs for AI Innovation Expo
INSERT INTO rsvp (user_id, event_id)
SELECT u.user_id, e.event_id
FROM users u
JOIN events e ON e.name = 'AI Innovation Expo'
WHERE u.email IN (
  'dummy@example.com',
  'dummy3@example.com',
  'dummy4@example.com'
);




-- ============================================]
-- 7. USER PREFERRED CATEGORIES


-- Dummy likes Career and Hackathon

INSERT INTO user_preferred_category (user_id, category_id)
SELECT u.user_id, c.category_id
FROM users u
CROSS JOIN categories c
WHERE u.email = 'dummy@example.com'
AND c.name IN ('Career', 'Hackathon');

-- Dummy2 likes Social and Sports
INSERT INTO user_preferred_category (user_id, category_id)
SELECT u.user_id, c.category_id
FROM users u
CROSS JOIN categories c
WHERE u.email = 'dummy2@example.com'
AND c.name IN ('Social', 'Sports');

-- Dummy3 likes all tech events
INSERT INTO user_preferred_category (user_id, category_id)
SELECT u.user_id, c.category_id
FROM users u
CROSS JOIN categories c
WHERE u.email = 'dummy3@example.com'
AND c.name IN ('Career', 'Hackathon', 'Meetup');