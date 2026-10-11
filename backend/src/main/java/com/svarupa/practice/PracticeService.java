package com.svarupa.practice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svarupa.library.JointAngles;
import com.svarupa.library.ReferenceService;
import com.svarupa.library.ReferenceStepRepository;
import com.svarupa.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Practice sessions: the tracker posts the student's final joint angles;
 * the backend scores them against the reference, stores per-joint scores
 * and corrections, and bumps the reference's practice count.
 */
@Service
public class PracticeService {

  private final PracticeSessionRepository sessions;
  private final ReferenceStepRepository references;
  private final UserRepository users;
  private final ReferenceService referenceService;
  private final ScoringService scoringService;
  private final ObjectMapper objectMapper;

  public PracticeService(
      PracticeSessionRepository sessions,
      ReferenceStepRepository references,
      UserRepository users,
      ReferenceService referenceService,
      ScoringService scoringService,
      ObjectMapper objectMapper) {
    this.sessions = sessions;
    this.references = references;
    this.users = users;
    this.referenceService = referenceService;
    this.scoringService = scoringService;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public SessionDto.View logSession(UUID studentId, @Valid SessionDto.LogRequest req) {
    var student =
        users
            .findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    var reference =
        references
            .findById(req.referenceStepId())
            .filter(r -> r.isActive())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reference not found"));

    var target = referenceService.parseAngles(reference);
    var score = scoringService.score(req.finalAngles(), target);
    var corrections = scoringService.correctionsFor(req.finalAngles(), target);

    var session = new PracticeSession();
    session.setStudent(student);
    session.setReferenceStep(reference);
    session.setStartedAt(req.startedAt() == null ? Instant.now() : req.startedAt());
    session.setEndedAt(Instant.now());
    int duration = Math.max(0, req.durationSeconds());
    session.setDurationSeconds(duration);
    session.setOverallMatchPct(
        BigDecimal.valueOf(score.overallPct()).setScale(2, RoundingMode.HALF_UP));
    session.setJointScores(toJson(score.perJoint()));
    session.setCorrections(toJson(corrections));
    session.setFrameCount(Math.max(0, req.frameCount()));

    reference.setPracticeCount(reference.getPracticeCount() + 1);
    var saved = sessions.save(session);
    return toView(saved, score, corrections, reference.getTitle());
  }

  @Transactional(readOnly = true)
  public Page<SessionDto.View> mySessions(UUID studentId, Pageable pageable) {
    return sessions
        .findByStudentIdOrderByStartedAtDesc(studentId, pageable)
        .map(s -> toView(s, null, null, s.getReferenceStep() == null ? null : s.getReferenceStep().getTitle()));
  }

  private SessionDto.View toView(
      PracticeSession s, ScoringService.JointScore score, List<String> corrections, String refTitle) {
    return new SessionDto.View(
        s.getId(),
        s.getReferenceStep() == null ? null : s.getReferenceStep().getId(),
        refTitle,
        s.getStartedAt(),
        s.getEndedAt(),
        s.getDurationSeconds(),
        s.getOverallMatchPct(),
        parseScores(s.getJointScores()),
        parseCorrections(s.getCorrections()),
        s.getFrameCount(),
        s.getCreatedAt());
  }

  @SuppressWarnings("unchecked")
  private java.util.Map<String, Double> parseScores(String json) {
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(json, java.util.Map.class);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private List<String> parseCorrections(String json) {
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(
          json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "serialization failed");
    }
  }

  // ---- DTOs ----

  public static final class SessionDto {
    private SessionDto() {}

    public record LogRequest(
        @NotNull UUID referenceStepId,
        @NotNull @Valid JointAngles finalAngles,
        Instant startedAt,
        @Min(0) int durationSeconds,
        @Min(0) int frameCount) {}

    public record View(
        UUID id,
        UUID referenceStepId,
        String referenceTitle,
        Instant startedAt,
        Instant endedAt,
        int durationSeconds,
        BigDecimal overallMatchPct,
        java.util.Map<String, Double> jointScores,
        List<String> corrections,
        int frameCount,
        Instant createdAt) {}
  }
}
