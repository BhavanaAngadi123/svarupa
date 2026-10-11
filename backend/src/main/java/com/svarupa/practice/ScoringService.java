package com.svarupa.practice;

import com.svarupa.library.JointAngles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Posture scoring. Mirrors the client prototype's {@code scoreAgainst} exactly:
 * per joint, {@code clamp(1 - max(0, deviation - 8) / 55, 0, 1)} — an 8-degree
 * tolerance band, full penalty at 63 degrees. Overall = mean × 100.
 */
@Service
public class ScoringService {

  private static final double TOLERANCE_DEG = 8.0;
  private static final double SCALE_DEG = 55.0;

  private static final Map<String, String> JOINT_LABELS =
      Map.of(
          "lKnee", "left knee",
          "rKnee", "right knee",
          "lElbow", "left elbow",
          "rElbow", "right elbow",
          "lShoulder", "left shoulder",
          "rShoulder", "right shoulder",
          "lHip", "left hip",
          "rHip", "right hip");

  public JointScore score(JointAngles current, JointAngles target) {
    var deviations = current.deviationsFrom(target);
    var perJoint = new LinkedHashMap<String, Double>();
    double sum = 0;
    for (var e : deviations.entrySet()) {
      double s = Math.max(0, Math.min(1, 1 - Math.max(0, e.getValue() - TOLERANCE_DEG) / SCALE_DEG));
      perJoint.put(e.getKey(), round2(s));
      sum += s;
    }
    double overall = perJoint.isEmpty() ? 0 : sum / perJoint.size() * 100;
    return new JointScore(perJoint, round2(overall), deviations);
  }

  /**
   * Human-readable corrections for the worst joints. Deterministic fallback
   * used when the AI coach is unavailable.
   */
  public List<String> correctionsFor(JointAngles current, JointAngles target) {
    var deviations = current.deviationsFrom(target);
    var corrections = new ArrayList<String>();
    deviations.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .limit(3)
        .filter(e -> e.getValue() > 12)
        .forEach(
            e -> {
              String label = JOINT_LABELS.getOrDefault(e.getKey(), e.getKey());
              String direction = currentValue(current, e.getKey()) > targetValue(target, e.getKey())
                  ? "too open — close it a little"
                  : "too closed — open it a little";
              corrections.add(
                  "Your " + label + " is off by " + Math.round(e.getValue()) + "°. " + direction + ".");
            });
    if (corrections.isEmpty()) {
      corrections.add("Beautiful — every joint is within the tolerance band. Hold this form.");
    }
    return corrections;
  }

  private static double currentValue(JointAngles a, String key) {
    return value(a, key);
  }

  private static double targetValue(JointAngles a, String key) {
    return value(a, key);
  }

  private static double value(JointAngles a, String key) {
    return switch (key) {
      case "lKnee" -> a.lKnee();
      case "rKnee" -> a.rKnee();
      case "lElbow" -> a.lElbow();
      case "rElbow" -> a.rElbow();
      case "lShoulder" -> a.lShoulder();
      case "rShoulder" -> a.rShoulder();
      case "lHip" -> a.lHip();
      case "rHip" -> a.rHip();
      default -> 0;
    };
  }

  private static double round2(double v) {
    return Math.round(v * 100.0) / 100.0;
  }

  public record JointScore(
      Map<String, Double> perJoint, double overallPct, Map<String, Double> deviationsDeg) {}
}
