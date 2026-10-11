package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.library.JointAngles;
import com.svarupa.practice.PracticeService;
import com.svarupa.practice.PracticeService.SessionDto;
import com.svarupa.progress.ProgressService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProgressServiceTest extends IntegrationTestBase {

  @Autowired PracticeService practiceService;
  @Autowired ProgressService progressService;

  private void logSession(JointAngles angles, int secondsAgo, int duration) {
    practiceService.logSession(
        student.getId(),
        new SessionDto.LogRequest(
            reference.getId(), angles, Instant.now().minusSeconds(secondsAgo), duration, 60));
  }

  @Test
  void emptySummaryForNewStudent() {
    var summary = progressService.summary(student.getId());
    assertThat(summary.totalSessions()).isEqualTo(0);
    assertThat(summary.avgMatchPct()).isNull();
    assertThat(summary.streakDays()).isEqualTo(0);
    assertThat(summary.trend()).isEmpty();
  }

  @Test
  void summaryAggregatesSessions() {
    logSession(new JointAngles(105, 95, 160, 160, 45, 45, 100, 100), 3600, 300);
    logSession(POSE_PERFECT_COPY, 60, 120);
    var summary = progressService.summary(student.getId());
    assertThat(summary.totalSessions()).isEqualTo(2);
    assertThat(summary.avgMatchPct().doubleValue()).isGreaterThan(95);
    assertThat(summary.totalPracticeSeconds()).isEqualTo(420);
    assertThat(summary.streakDays()).isEqualTo(1);
    assertThat(summary.trend()).hasSize(2);
  }

  @Test
  void byReferenceShowsImprovement() {
    // Worse first, perfect later → positive improvement.
    logSession(new JointAngles(130, 95, 160, 160, 45, 45, 100, 100), 7200, 300);
    logSession(POSE_PERFECT_COPY, 60, 300);
    var list = progressService.byReference(student.getId());
    assertThat(list).hasSize(1);
    var rp = list.get(0);
    assertThat(rp.sessions()).isEqualTo(2);
    assertThat(rp.improvementPct().doubleValue()).isPositive();
    assertThat(rp.bestMatchPct().doubleValue())
        .isGreaterThanOrEqualTo(rp.firstMatchPct().doubleValue());
  }

  @Test
  void streakCountsConsecutiveDays() {
    // Sessions on two consecutive days → streak 2.
    logSession(POSE_A, 25 * 3600, 300); // yesterday
    logSession(POSE_A, 60, 300); // today
    assertThat(progressService.summary(student.getId()).streakDays()).isEqualTo(2);
  }

  @Test
  void streakBreaksOnGap() {
    logSession(POSE_A, 3 * 24 * 3600, 300); // 3 days ago only
    assertThat(progressService.summary(student.getId()).streakDays()).isEqualTo(0);
  }
}
