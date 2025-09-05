-- Seed categories
INSERT INTO categories (name) VALUES ('Social');
INSERT INTO categories (name) VALUES ('Career');
INSERT INTO categories (name) VALUES ('Hackathon');
INSERT INTO categories (name) VALUES ('Meetup');


-- Past event (won't show in "upcoming")
INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price, category_fk_id)
VALUES ('Welcome Back BBQ', 'Kick off social', 1, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'Alumni Courtyard', 200, 0.00,
        (SELECT category_id FROM categories WHERE name='Social'));

-- Upcoming events (should show, sorted by date_time)
INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price, category_fk_id)
VALUES ('Cloud Career Panel', 'Industry speakers', 2, DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 'Building 80, Room 10-12', 150, 0.00,
        (SELECT category_id FROM categories WHERE name='Career'));

INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price, category_fk_id)
VALUES ('Hack Night', 'Bring your laptop', 2, DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'Fab Lab', 80, 0.00,
        (SELECT category_id FROM categories WHERE name='Hackathon'));

INSERT INTO events (name, description, created_by_user_id, date_time, location, capacity, price, category_fk_id)
VALUES ('Data Science Meetup', 'Lightning talks', 3, DATEADD('DAY', 2, CURRENT_TIMESTAMP), 'Online (Zoom)', 500, 0.00,
        (SELECT category_id FROM categories WHERE name='Meetup'));