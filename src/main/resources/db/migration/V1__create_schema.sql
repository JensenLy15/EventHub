DROP TABLE IF EXISTS event_categories;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
  category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL
);
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS rsvp;

CREATE TABLE users (
  user_id IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL CHECK (role IN ('student', 'organiser', 'admin')),
  status VARCHAR(50) NOT NULL CHECK (status IN ('active', 'banned', 'suspended'))
);

CREATE TABLE events (
  event_id IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(2000),
  created_by_user_id BIGINT,
  date_time TIMESTAMP NOT NULL,
  location VARCHAR(255) NOT NULL,
  capacity INT,
  price DECIMAL(10,2),
  CONSTRAINT fk_event_user FOREIGN KEY (created_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE event_categories (
    event_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY(event_id, category_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);
-- Create index for efficient upcoming event queries
CREATE INDEX idx_event_date_time ON events(date_time);

CREATE TABLE rsvp (
  rsvp_id IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_rsvp_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_rsvp_event FOREIGN KEY (event_id) REFERENCES events(event_id),
  CONSTRAINT uc_user_event UNIQUE (user_id, event_id)  -- prevent duplicate RSVP
);