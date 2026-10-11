package com.svarupa.library;

import com.svarupa.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A teacher-recorded reference step: the "destination" the student's practice
 * is scored against. Joint angles stored as JSON text (portable across
 * PostgreSQL and H2).
 */
@Entity
@Table(name = "reference_steps")
public class ReferenceStep {

  public enum Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
  }

  @Id private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "teacher_id", nullable = false)
  private User teacher;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(name = "dance_form", nullable = false, length = 64)
  private String danceForm = "Kuchipudi";

  @Column(columnDefinition = "TEXT")
  private String description;

  /** JSON of {@link JointAngles}. */
  @Column(name = "joint_angles", nullable = false, columnDefinition = "TEXT")
  private String jointAngles;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Difficulty difficulty = Difficulty.BEGINNER;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "practice_count", nullable = false)
  private int practiceCount = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public ReferenceStep() {}

  public UUID getId() {
    return id;
  }

  public User getTeacher() {
    return teacher;
  }

  public void setTeacher(User teacher) {
    this.teacher = teacher;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDanceForm() {
    return danceForm;
  }

  public void setDanceForm(String danceForm) {
    this.danceForm = danceForm;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getJointAngles() {
    return jointAngles;
  }

  public void setJointAngles(String jointAngles) {
    this.jointAngles = jointAngles;
  }

  public Difficulty getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(Difficulty difficulty) {
    this.difficulty = difficulty;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getPracticeCount() {
    return practiceCount;
  }

  public void setPracticeCount(int practiceCount) {
    this.practiceCount = practiceCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
