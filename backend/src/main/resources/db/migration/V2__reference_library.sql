-- V2: teacher reference library.
-- A reference step is a teacher-recorded pose: 8 canonical joint angles
-- (lKnee, rKnee, lElbow, rElbow, lShoulder, rShoulder, lHip, rHip, in degrees)
-- stored as JSON text, matching the client pose-tracker's contract.
-- Portable DDL: runs on PostgreSQL 18 and H2 (tests).

CREATE TABLE reference_steps (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    dance_form VARCHAR(64) NOT NULL DEFAULT 'Kuchipudi',
    description TEXT,
    joint_angles TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    practice_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reference_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE INDEX idx_reference_teacher ON reference_steps (teacher_id);
CREATE INDEX idx_reference_form ON reference_steps (dance_form);
CREATE INDEX idx_reference_active ON reference_steps (active);
