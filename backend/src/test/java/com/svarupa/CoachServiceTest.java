package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.coach.CoachService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CoachServiceTest extends IntegrationTestBase {

  @Autowired CoachService coachService;

  @Test
  void statusReportsFallbackWhenOllamaDown() {
    // No Ollama in tests → graceful fallback, never an exception.
    var status = coachService.status();
    assertThat(status.enabled()).isTrue();
    assertThat(status.aiAvailable()).isFalse();
  }

  @Test
  void feedbackFallsBackToRuleBasedCorrections() {
    var req =
        new CoachService.FeedbackRequest(
            new com.svarupa.library.JointAngles(130, 95, 160, 160, 45, 45, 100, 100),
            POSE_A,
            "Kuchipudi",
            "Araimandi");
    var feedback = coachService.feedback(req);
    assertThat(feedback.aiUsed()).isFalse();
    assertThat(feedback.aiCoaching()).isNull();
    assertThat(feedback.corrections()).isNotEmpty();
    assertThat(feedback.corrections().get(0)).containsIgnoringCase("left knee");
    assertThat(feedback.overallMatchPct()).isLessThan(100);
  }

  @Test
  void feedbackForPerfectPosePraises() {
    var req = new CoachService.FeedbackRequest(POSE_A, POSE_A, "Kuchipudi", "Araimandi");
    var feedback = coachService.feedback(req);
    assertThat(feedback.overallMatchPct()).isEqualTo(100.0);
    assertThat(feedback.corrections()).hasSize(1);
  }
}
