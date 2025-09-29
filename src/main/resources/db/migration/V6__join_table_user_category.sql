CREATE TABLE IF NOT EXISTS user_preferred_category (
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, category_id),
  CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
);