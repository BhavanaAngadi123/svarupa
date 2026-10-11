package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.library.JointAngles;
import com.svarupa.practice.PracticeService;
import com.svarupa.practice.PracticeService.SessionDto;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

class PracticeServiceTest extends IntegrationTestBase {

  @Autowired PracticeService practiceService;

  private SessionDto.LogRequest logRequest(JointAngles angles, int seconds) {
    return new SessionDto.LogRequest(reference.getId(), angles, Instant.now().minusSeconds(seconds),
        seconds, 120);
  }

  @Test
  void logSessionScoresAgainstReference() {
    // Slightly off on one joint (10° on lKnee): per-joint ≈ 0.9636.
    var angles = new JointAngles(105, 95, 160, 160, 45, 45, 100, 100);
    var view = practiceService.logSession(student.getId(), logRequest(angles, 300));
    assertThat(view.overallMatchPct()).isNotNull();
    assertThat(view.overallMatchPct().doubleValue()).isGreaterThan(95).isLessThan(100);
    assertThat(view.jointScores()).containsKey("lKnee");
    assertThat(view.corrections()).isNotEmpty();
    assertThat(view.durationSeconds()).isEqualTo(300);
    assertThat(view.frameCount()).isEqualTo(120);
  }

  @Test
  void perfectSessionScores100() {
    var view = practiceService.logSession(student.getId(), logRequest(POSE_PERFECT_COPY, 60));
    assertThat(view.overallMatchPct()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(view.corrections()).hasSize(1); // praise, not corrections
  }

  @Test
  void practiceCountBumpsOnReference() {
    int before = references.findById(reference.getId()).orElseThrow().getPracticeCount();
    practiceService.logSession(student.getId(), logRequest(POSE_A, 60));
    int after = references.findById(reference.getId()).orElseThrow().getPracticeCount();
    assertThat(after).isEqualTo(before + 1);
  }

  @Test
  void unknownReferenceRejected() {
    var req =
        new SessionDto.LogRequest(
            java.util.UUID.randomUUID(), POSE_A, Instant.now(), 60, 10);
    assertThatThrownBy(() -> practiceService.logSession(student.getId(), req))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("reference not found");
  }

  @Test
  void mySessionsListsNewestFirst() {
    practiceService.logSession(student.getId(), logRequest(POSE_A, 60));
    practiceService.logSession(student.getId(), logRequest(POSE_A, 120));
    var page = practiceService.mySessions(student.getId(), PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(2);
    // Ordered by startedAt desc: the session started 60s ago comes first.
    assertThat(page.getContent().get(0).startedAt())
        .isAfterOrEqualTo(page.getContent().get(1).startedAt());
  }
}
