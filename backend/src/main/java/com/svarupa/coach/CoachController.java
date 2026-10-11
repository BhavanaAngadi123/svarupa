package com.svarupa.coach;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** AI practice-coach endpoints. Never fails: falls back when the model is down. */
@RestController
@RequestMapping("/api/coach")
@Tag(name = "Coach")
public class CoachController {

  private final CoachService coachService;

  public CoachController(CoachService coachService) {
    this.coachService = coachService;
  }

  @GetMapping("/status")
  @Operation(summary = "Coach availability (public)")
  public CoachService.StatusResponse status() {
    return coachService.status();
  }

  @PostMapping("/feedback")
  @Operation(summary = "Get coaching feedback for a practice attempt")
  public CoachService.FeedbackResponse feedback(@Valid @RequestBody CoachService.FeedbackRequest req) {
    return coachService.feedback(req);
  }
}
