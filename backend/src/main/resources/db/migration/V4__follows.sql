-- V4: student-teacher follows (powers the teacher dashboard).
-- Portable DDL: runs on PostgreSQL 18 and H2 (tests).

CREATE TABLE follows (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    teacher_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_follows_pair UNIQUE (student_id, teacher_id),
    CONSTRAINT chk_follows_not_self CHECK (student_id <> teacher_id)
);

CREATE INDEX idx_follows_student ON follows (student_id);
CREATE INDEX idx_follows_teacher ON follows (teacher_id);
