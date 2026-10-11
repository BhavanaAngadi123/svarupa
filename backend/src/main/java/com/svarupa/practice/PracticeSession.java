package com.svarupa.practice;

import com.svarupa.library.ReferenceStep;
import com.svarupa.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** One student practice session against a reference step. */
@Entity
@Table(name = "practice_sessions")
public class PracticeSession {

  @Id private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reference_step_id")
  private ReferenceStep referenceStep;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt = Instant.now();

  @Column(name = "ended_at")
  private Instant endedAt;

  @Column(name = "duration_seconds", nullable = false)
  private int durationSeconds = 0;

  @Column(name = "overall_match_pct", precision = 5, scale = 2)
  private BigDecimal overallMatchPct;

  /** JSON: per-joint 0..1 scores. */
  @Column(name = "joint_scores", columnDefinition = "TEXT")
  private String jointScores;

  /** JSON: list of correction strings given during the session. */
  @Column(columnDefinition = "TEXT")
  private String corrections;

  @Column(name = "frame_count", nullable = false)
  private int frameCount = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected PracticeSession() {}

  public UUID getId() {
    return id;
  }

  public User getStudent() {
    return student;
  }

  public void setStudent(User student) {
    this.student = student;
  }

  public ReferenceStep getReferenceStep() {
    return referenceStep;
  }

  public void setReferenceStep(ReferenceStep referenceStep) {
    this.referenceStep = referenceStep;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Instant endedAt) {
    this.endedAt = endedAt;
  }

  public int getDurationSeconds() {
    return durationSeconds;
  }

  public void setDurationSeconds(int durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public BigDecimal getOverallMatchPct() {
    return overallMatchPct;
  }

  public void setOverallMatchPct(BigDecimal overallMatchPct) {
    this.overallMatchPct = overallMatchPct;
  }

  public String getJointScores() {
    return jointScores;
  }

  public void setJointScores(String jointScores) {
    this.jointScores = jointScores;
  }

  public String getCorrections() {
    return corrections;
  }

  public void setCorrections(String corrections) {
    this.corrections = corrections;
  }

  public int getFrameCount() {
    return frameCount;
  }

  public void setFrameCount(int frameCount) {
    this.frameCount = frameCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
