CREATE INDEX idx_app_created_at ON applications (created_at DESC);
CREATE INDEX idx_app_status_created ON applications (status, created_at DESC);
CREATE INDEX idx_app_national_id_hash ON applications (national_id_hash);
CREATE INDEX idx_doc_app ON documents (application_id);
CREATE INDEX idx_decision_app_time ON decisions (application_id, decided_at DESC);
CREATE INDEX idx_outbox_pub ON outbox_events (published_at NULLS FIRST, occurred_at);