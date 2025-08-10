CREATE INDEX idx_app_created_at ON mortgage.applications (created_at);
CREATE INDEX idx_app_status_created ON mortgage.applications (status, created_at);
CREATE INDEX idx_app_national_id_hash ON mortgage.applications (national_id_hash);
CREATE INDEX idx_doc_user_id ON mortgage.documents (user_id);
CREATE INDEX idx_decision_user_time ON mortgage.decisions (user_id, decided_at);
CREATE INDEX idx_outbox_pub ON mortgage.outbox_events (published_at NULLS FIRST, occurred_at);