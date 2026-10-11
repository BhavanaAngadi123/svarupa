package com.svarupa.progress;

import com.svarupa.library.ReferenceStepRepository;
import com.svarupa.practice.PracticeSessionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Progress analytics: score trends, streaks, practice time — computed from
 * practice sessions, nothing pre-aggregated.
 */
@Service
public class ProgressService {

  private final PracticeSessionRepository sessions;
  private final ReferenceStepRepository references;

  public ProgressService(
      PracticeSessionRepository sessions, ReferenceStepRepository references) {
    this.sessions = sessions;
    this.references = references;
  }

  @Transactional(readOnly = true)
  public Summary summary(UUID studentId) {
    long totalSessions = sessions.countScoredByStudent(studentId);
    Double avg = sessions.avgMatchByStudent(studentId);
    long totalSeconds = sessions.totalSecondsByStudent(studentId);
    int streakDays = currentStreakDays(studentId);
    var recent =
        sessions.findByStudentIdOrderByStartedAtDesc(studentId, PageRequest.of(0, 30)).getContent();
    var trend = new ArrayList<TrendPoint>();
    for (var s : recent) {
      if (s.getOverallMatchPct() != null) {
        trend.add(
            new TrendPoint(
                s.getStartedAt().truncatedTo(ChronoUnit.DAYS),
                s.getOverallMatchPct().setScale(1, RoundingMode.HALF_UP)));
      }
    }
    trend.sort((a, b) -> a.day().compareTo(b.day()));
    return new Summary(
        totalSessions,
        avg == null ? null : BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP),
        totalSeconds,
        streakDays,
        trend);
  }

  @Transactional(readOnly = true)
  public List<ReferenceProgress> byReference(UUID studentId) {
    var all =
        sessions
            .findByStudentIdAndStartedAtAfterOrderByStartedAtAsc(studentId, Instant.EPOCH);
    var byRef = new java.util.LinkedHashMap<UUID, List<com.svarupa.practice.PracticeSession>>();
    for (var s : all) {
      if (s.getReferenceStep() != null && s.getOverallMatchPct() != null) {
        byRef.computeIfAbsent(s.getReferenceStep().getId(), k -> new ArrayList<>()).add(s);
      }
    }
    var result = new ArrayList<ReferenceProgress>();
    for (var e : byRef.entrySet()) {
      var list = e.getValue();
      var first = list.get(0).getOverallMatchPct();
      var last = list.get(list.size() - 1).getOverallMatchPct();
      var avg =
          list.stream()
              .map(com.svarupa.practice.PracticeSession::getOverallMatchPct)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .divide(BigDecimal.valueOf(list.size()), 1, RoundingMode.HALF_UP);
      String title =
          references
              .findById(e.getKey())
              .map(r -> r.getTitle())
              .orElse("Reference");
      result.add(
          new ReferenceProgress(
              e.getKey(), title, list.size(), avg, first, last,
              last.subtract(first).setScale(1, RoundingMode.HALF_UP)));
    }
    result.sort((a, b) -> Integer.compare(b.sessions(), a.sessions()));
    return result;
  }

  /** Consecutive days (UTC) with at least one session, ending today or yesterday. */
  int currentStreakDays(UUID studentId) {
    var all =
        sessions.findByStudentIdAndStartedAtAfterOrderByStartedAtAsc(
            studentId, Instant.now().minus(365, ChronoUnit.DAYS));
    var days = new java.util.HashSet<LocalDate>();
    for (var s : all) {
      days.add(s.getStartedAt().atZone(ZoneOffset.UTC).toLocalDate());
    }
    if (days.isEmpty()) {
      return 0;
    }
    LocalDate cursor = LocalDate.now(ZoneOffset.UTC);
    if (!days.contains(cursor)) {
      cursor = cursor.minusDays(1); // yesterday still counts as "alive" streak
      if (!days.contains(cursor)) {
        return 0;
      }
    }
    int streak = 0;
    while (days.contains(cursor)) {
      streak++;
      cursor = cursor.minusDays(1);
    }
    return streak;
  }

  public record Summary(
      long totalSessions,
      BigDecimal avgMatchPct,
      long totalPracticeSeconds,
      int streakDays,
      List<TrendPoint> trend) {}

  public record TrendPoint(Instant day, BigDecimal matchPct) {}

  public record ReferenceProgress(
      UUID referenceStepId,
      String title,
      int sessions,
      BigDecimal avgMatchPct,
      BigDecimal firstMatchPct,
      BigDecimal bestMatchPct,
      BigDecimal improvementPct) {}
}
