package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.library.JointAngles;
import com.svarupa.practice.ScoringService;
import org.junit.jupiter.api.Test;

/** Unit tests for the scoring engine — must mirror the client prototype exactly. */
class ScoringServiceTest {

  private final ScoringService scoring = new ScoringService();

  private static final JointAngles TARGET =
      new JointAngles(95, 95, 160, 160, 45, 45, 100, 100);

  @Test
  void perfectMatchScores100() {
    var result = scoring.score(TARGET, TARGET);
    assertThat(result.overallPct()).isEqualTo(100.0);
    assertThat(result.perJoint().values()).allMatch(v -> v == 1.0);
  }

  @Test
  void withinToleranceBandScores100() {
    // 8° deviation is fully forgiven (prototype: max(0, d-8)).
    var cur = new JointAngles(103, 87, 160, 160, 45, 45, 100, 100);
    var result = scoring.score(cur, TARGET);
    assertThat(result.overallPct()).isEqualTo(100.0);
  }

  @Test
  void justOutsideTolerancePenalizes() {
    // 9° deviation on one joint: 1 - 1/55 ≈ 0.9818; overall = (7 + 0.9818)/8*100.
    var cur = new JointAngles(104, 95, 160, 160, 45, 45, 100, 100);
    var result = scoring.score(cur, TARGET);
    assertThat(result.perJoint().get("lKnee")).isCloseTo(0.98, within(0.01));
    assertThat(result.overallPct()).isCloseTo(99.77, within(0.05));
  }

  @Test
  void fullDeviationScoresZeroForThatJoint() {
    // 63°+ deviation → 0 for that joint; others perfect → 7/8*100 = 87.5.
    var cur = new JointAngles(95 + 63, 95, 160, 160, 45, 45, 100, 100);
    var result = scoring.score(cur, TARGET);
    assertThat(result.perJoint().get("lKnee")).isEqualTo(0.0);
    assertThat(result.overallPct()).isEqualTo(87.5);
  }

  @Test
  void deviationsReportedInDegrees() {
    var cur = new JointAngles(110, 95, 160, 160, 45, 45, 100, 100);
    var result = scoring.score(cur, TARGET);
    assertThat(result.deviationsDeg().get("lKnee")).isEqualTo(15.0);
  }

  @Test
  void correctionsNameWorstJoints() {
    var cur = new JointAngles(130, 95, 160, 160, 45, 45, 100, 100); // lKnee off by 35°
    var corrections = scoring.correctionsFor(cur, TARGET);
    assertThat(corrections).isNotEmpty();
    assertThat(corrections.get(0)).containsIgnoringCase("left knee").contains("35°");
  }

  @Test
  void correctionsDirectionTooOpen() {
    // current > target → "too open".
    var cur = new JointAngles(130, 95, 160, 160, 45, 45, 100, 100);
    assertThat(scoring.correctionsFor(cur, TARGET).get(0)).contains("too open");
  }

  @Test
  void correctionsDirectionTooClosed() {
    // current < target → "too closed".
    var cur = new JointAngles(60, 95, 160, 160, 45, 45, 100, 100);
    assertThat(scoring.correctionsFor(cur, TARGET).get(0)).contains("too closed");
  }

  @Test
  void perfectFormGetsPraiseNotCorrections() {
    var corrections = scoring.correctionsFor(TARGET, TARGET);
    assertThat(corrections).hasSize(1);
    assertThat(corrections.get(0)).containsIgnoringCase("beautiful");
  }

  @Test
  void atMostThreeCorrections() {
    var cur = new JointAngles(130, 140, 100, 100, 100, 100, 150, 150);
    assertThat(scoring.correctionsFor(cur, TARGET)).hasSizeLessThanOrEqualTo(3);
  }
}
