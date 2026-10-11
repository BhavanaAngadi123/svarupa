package com.svarupa.follow;

import com.svarupa.common.Role;
import com.svarupa.progress.ProgressService;
import com.svarupa.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Student-teacher follows + the teacher dashboard view of student progress. */
@Service
public class FollowService {

  private final FollowRepository follows;
  private final UserRepository users;
  private final ProgressService progressService;

  public FollowService(
      FollowRepository follows, UserRepository users, ProgressService progressService) {
    this.follows = follows;
    this.users = users;
    this.progressService = progressService;
  }

  @Transactional
  public void follow(UUID studentId, UUID teacherId) {
    if (studentId.equals(teacherId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot follow yourself");
    }
    var student =
        users
            .findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    var teacher =
        users
            .findById(teacherId)
            .filter(u -> u.getRole() == Role.TEACHER && u.isEnabled())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "teacher not found"));
    if (!follows.existsByStudentIdAndTeacherId(studentId, teacherId)) {
      follows.save(new Follow(student, teacher));
    }
  }

  @Transactional
  public void unfollow(UUID studentId, UUID teacherId) {
    follows
        .findByStudentIdAndTeacherId(studentId, teacherId)
        .ifPresent(follows::delete);
  }

  @Transactional(readOnly = true)
  public List<UUID> myTeachers(UUID studentId) {
    return follows.findTeacherIdsByStudent(studentId);
  }

  /** Teacher dashboard: every follower with their progress summary. */
  @Transactional(readOnly = true)
  public TeacherDashboard dashboard(UUID teacherId) {
    var students =
        follows.findByTeacherWithStudent(teacherId).stream()
            .map(
                f -> {
                  var s = f.getStudent();
                  var summary = progressService.summary(s.getId());
                  return new StudentProgress(
                      s.getId(),
                      s.getDisplayName(),
                      summary.totalSessions(),
                      summary.avgMatchPct(),
                      summary.streakDays(),
                      f.getCreatedAt());
                })
            .toList();
    return new TeacherDashboard(
        teacherId, students.size(), follows.countByTeacher(teacherId), students);
  }

  public record StudentProgress(
      UUID studentId,
      String displayName,
      long totalSessions,
      BigDecimal avgMatchPct,
      int streakDays,
      java.time.Instant followedAt) {}

  public record TeacherDashboard(
      UUID teacherId, long studentCount, long followerCount, List<StudentProgress> students) {}
}
