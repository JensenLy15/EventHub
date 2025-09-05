DROP TABLE IF EXISTS event_categories;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
  category_id IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
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
  category_fk_id BIGINT NOT NULL,
  CONSTRAINT category_fk_id FOREIGN KEY (category_fk_id) REFERENCES categories(category_id)
);

-- Create index for efficient upcoming event queries
CREATE INDEX idx_event_date_time ON events(date_time);
CREATE INDEX idx_event_category_fk_id ON events(category_fk_id);