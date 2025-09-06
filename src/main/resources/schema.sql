DROP TABLE IF EXISTS event_categories;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
  category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL
);

CREATE TABLE events (
  event_id IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(2000),
  created_by_user_id BIGINT,    -- Foreign key to user table (not enforced yet)
  date_time TIMESTAMP NOT NULL,
  location VARCHAR(255) NOT NULL,
  capacity INT,
  price DECIMAL(10,2)
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