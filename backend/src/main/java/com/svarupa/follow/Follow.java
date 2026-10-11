package com.svarupa.follow;

import com.svarupa.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** A student follows a teacher — powers the teacher dashboard. */
@Entity
@Table(name = "follows")
public class Follow {

  @Id private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "teacher_id", nullable = false)
  private User teacher;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  protected Follow() {}

  public Follow(User student, User teacher) {
    this.student = student;
    this.teacher = teacher;
  }

  public UUID getId() {
    return id;
  }

  public User getStudent() {
    return student;
  }

  public User getTeacher() {
    return teacher;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
