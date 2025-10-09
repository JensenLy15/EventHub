DROP TABLE IF EXISTS reports;

CREATE TABLE reports (
  report_id IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  note VARCHAR(1000),
  reportStatus VARCHAR(50) NOT NULL CHECK (reportStatus IN ('open', 'under_review', 'resolved')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_report_event FOREIGN KEY (event_id) REFERENCES events(event_id)
  -- CONSTRAINT uc_report_event UNIQUE (user_id, event_id)  -- prevent duplicate report
);