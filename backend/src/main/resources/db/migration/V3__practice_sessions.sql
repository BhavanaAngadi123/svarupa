-- V3: practice sessions.
-- Each session stores the student's per-joint match scores (0..1) and the
-- corrections given as JSON text, plus the overall % match.
-- Portable DDL: runs on PostgreSQL 18 and H2 (tests).

CREATE TABLE practice_sessions (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reference_step_id UUID REFERENCES reference_steps (id) ON DELETE SET NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    duration_seconds INT NOT NULL DEFAULT 0,
    overall_match_pct NUMERIC(5, 2),
    joint_scores TEXT,
    corrections TEXT,
    frame_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_student ON practice_sessions (student_id);
CREATE INDEX idx_sessions_reference ON practice_sessions (reference_step_id);
CREATE INDEX idx_sessions_started ON practice_sessions (started_at);
