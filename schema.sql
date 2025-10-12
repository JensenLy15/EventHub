DROP TABLE IF EXISTS rsvp;
DROP TABLE IF EXISTS event_categories;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS user_preferred_category;

CREATE TABLE categories (
  category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE users (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('student', 'organiser', 'admin') NOT NULL,
  status ENUM('active', 'banned', 'suspended') NOT NULL,
  display_name VARCHAR(100),
  avatar_url VARCHAR(400),
  bio VARCHAR(1000),
  gender VARCHAR(32) DEFAULT 'prefer_not_to_say' NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT users_gender_chk
    CHECK (gender IN ('male','female','nonbinary','other','prefer_not_to_say'))
);

CREATE TABLE events (
  event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(2000),
  detailed_description TEXT,
  created_by_user_id BIGINT,
  date_time TIMESTAMP NOT NULL,
  location VARCHAR(255) NOT NULL,
  capacity INT,
  price DECIMAL(10,2),
  agenda TEXT,
  speakers VARCHAR(255),
  dress_code VARCHAR(100),
  event_status BOOLEAN NOT NULL DEFAULT TRUE,
  deactivated_by_admin_id BIGINT NULL,
  deactivation_reason VARCHAR(1000) NULL,
  deactivation_at DATETIME NULL,
  CONSTRAINT fk_event_user FOREIGN KEY (created_by_user_id)
    REFERENCES users(user_id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT fk_event_deactivated_by FOREIGN KEY (deactivated_by_admin_id)
    REFERENCES users(user_id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
);

CREATE INDEX idx_event_date_time ON events(date_time);


CREATE TABLE event_categories (
  event_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY (event_id, category_id),
  CONSTRAINT fk_event FOREIGN KEY (event_id)
    REFERENCES events(event_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_category FOREIGN KEY (category_id)
    REFERENCES categories(category_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);


CREATE TABLE rsvp (
  rsvp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rsvp_user FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_rsvp_event FOREIGN KEY (event_id)
    REFERENCES events(event_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT uc_user_event UNIQUE (user_id, event_id)
);

CREATE TABLE reports (
  report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  note VARCHAR(1000),
  reportStatus VARCHAR(50) NOT NULL CHECK (reportStatus IN ('open', 'under_review', 'resolved')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_report_user FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_report_event FOREIGN KEY (event_id)
    REFERENCES events(event_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

  -- CONSTRAINT uc_report_event UNIQUE (user_id, event_id)  -- prevent duplicate report
CREATE TABLE user_preferred_category (
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, category_id),
  CONSTRAINT fk_upc_user FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_upc_category FOREIGN KEY (category_id)
    REFERENCES categories(category_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);
