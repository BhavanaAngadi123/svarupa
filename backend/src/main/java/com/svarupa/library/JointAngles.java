package com.svarupa.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * The canonical joint-angle contract shared with the client pose tracker
 * (mirrors the prototype's ANGLE_KEYS): 8 joint angles in degrees, 0..180.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JointAngles(
    @Min(0) @Max(180) double lKnee,
    @Min(0) @Max(180) double rKnee,
    @Min(0) @Max(180) double lElbow,
    @Min(0) @Max(180) double rElbow,
    @Min(0) @Max(180) double lShoulder,
    @Min(0) @Max(180) double rShoulder,
    @Min(0) @Max(180) double lHip,
    @Min(0) @Max(180) double rHip) {

  /** Deviation per joint in degrees, used for scoring and corrections. */
  public java.util.Map<String, Double> deviationsFrom(JointAngles target) {
    var map = new java.util.LinkedHashMap<String, Double>();
    map.put("lKnee", Math.abs(lKnee - target.lKnee));
    map.put("rKnee", Math.abs(rKnee - target.rKnee));
    map.put("lElbow", Math.abs(lElbow - target.lElbow));
    map.put("rElbow", Math.abs(rElbow - target.rElbow));
    map.put("lShoulder", Math.abs(lShoulder - target.lShoulder));
    map.put("rShoulder", Math.abs(rShoulder - target.rShoulder));
    map.put("lHip", Math.abs(lHip - target.lHip));
    map.put("rHip", Math.abs(rHip - target.rHip));
    return map;
  }
}
