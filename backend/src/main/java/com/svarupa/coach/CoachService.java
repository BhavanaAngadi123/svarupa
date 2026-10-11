package com.svarupa.coach;

import com.svarupa.config.SvarupaProperties;
import com.svarupa.library.JointAngles;
import com.svarupa.practice.ScoringService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AI practice coach. When Ollama was reachable at startup, a small local model
 * turns joint deviations into warm coaching cues. Otherwise (e.g. Render free
 * tier, where Ollama can't run) it degrades gracefully to deterministic
 * rule-based corrections — the endpoint never fails and never blocks.
 */
@Service
public class CoachService {

  private static final Logger log = LoggerFactory.getLogger(CoachService.class);

  private final Optional<ChatClient> chatClient;
  private final ScoringService scoringService;
  private final SvarupaProperties props;

  public CoachService(
      Optional<ChatClient> chatClient, ScoringService scoringService, SvarupaProperties props) {
    this.chatClient = chatClient;
    this.scoringService = scoringService;
    this.props = props;
  }

  public StatusResponse status() {
    return new StatusResponse(props.getCoach().isEnabled(), aiAvailable());
  }

  /** True only when the AI model bean was created (Ollama reachable at startup). */
  public boolean aiAvailable() {
    return props.getCoach().isEnabled() && chatClient.isPresent();
  }

  public FeedbackResponse feedback(@Valid FeedbackRequest req) {
    var corrections = scoringService.correctionsFor(req.currentAngles(), req.targetAngles());
    var score = scoringService.score(req.currentAngles(), req.targetAngles());
    String aiNote = null;
    boolean aiUsed = false;
    if (aiAvailable()) {
      try {
        aiNote = askModel(req, score, corrections);
        aiUsed = aiNote != null && !aiNote.isBlank();
      } catch (Exception e) {
        log.debug("Coach model call failed, using fallback: {}", e.getMessage());
      }
    }
    return new FeedbackResponse(score.overallPct(), corrections, aiNote, aiUsed);
  }

  private String askModel(
      FeedbackRequest req, ScoringService.JointScore score, List<String> corrections) {
    var prompt =
        """
        You are Svarupa, a warm and precise dance teacher. A student just practiced a %s step
        ("%s") and scored %.1f%% against the teacher's reference pose.
        The three worst joints (deviation in degrees): %s.
        Rule-based corrections: %s.
        In 2-3 short sentences, give encouraging, specific coaching: name ONE body part to fix,
        describe the physical feeling to aim for, and end with encouragement. No lists, no jargon.
        """
            .formatted(
                req.danceForm(),
                req.stepTitle(),
                score.overallPct(),
                topDeviations(score.deviationsDeg()),
                String.join(" ", corrections));
    return chatClient.orElseThrow().prompt().user(prompt).call().content();
  }

  private static String topDeviations(Map<String, Double> deviations) {
    return deviations.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .limit(3)
        .map(e -> e.getKey() + " " + Math.round(e.getValue()) + "°")
        .reduce((a, b) -> a + ", " + b)
        .orElse("none");
  }

  public record FeedbackRequest(
      @NotNull @Valid JointAngles currentAngles,
      @NotNull @Valid JointAngles targetAngles,
      String danceForm,
      String stepTitle) {
    public FeedbackRequest {
      if (danceForm == null || danceForm.isBlank()) {
        danceForm = "Kuchipudi";
      }
      if (stepTitle == null) {
        stepTitle = "practice pose";
      }
    }
  }

  public record FeedbackResponse(
      double overallMatchPct, List<String> corrections, String aiCoaching, boolean aiUsed) {}

  public record StatusResponse(boolean enabled, boolean aiAvailable) {}
}
