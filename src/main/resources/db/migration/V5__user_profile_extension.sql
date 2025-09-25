-- Extend users with profile fields (H2/Postgres/MySQL friendly)

ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url   VARCHAR(400);
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio          VARCHAR(1000);

-- Gender with sane default so existing rows pass NOT NULL
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(32)
  DEFAULT 'prefer_not_to_say' NOT NULL;

-- last-updated timestamp
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP
  DEFAULT CURRENT_TIMESTAMP;

--  keep allowed values tidy
ALTER TABLE users
  ADD CONSTRAINT users_gender_chk
  CHECK (gender IN ('male','female','nonbinary','other','prefer_not_to_say'));