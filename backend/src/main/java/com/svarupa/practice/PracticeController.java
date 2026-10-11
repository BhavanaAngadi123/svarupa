package com.svarupa.practice;

import com.svarupa.practice.PracticeService.SessionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Practice session endpoints (authenticated students). */
@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions")
public class PracticeController {

  private final PracticeService practiceService;

  public PracticeController(PracticeService practiceService) {
    this.practiceService = practiceService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Log a completed practice session (scored server-side)")
  public SessionDto.View logSession(
      @AuthenticationPrincipal String userId, @Valid @RequestBody SessionDto.LogRequest req) {
    return practiceService.logSession(UUID.fromString(userId), req);
  }

  @GetMapping("/mine")
  @Operation(summary = "List my practice sessions, newest first")
  public Page<SessionDto.View> mySessions(
      @AuthenticationPrincipal String userId, @PageableDefault(size = 20) Pageable pageable) {
    return practiceService.mySessions(UUID.fromString(userId), pageable);
  }
}
