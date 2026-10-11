package com.svarupa.progress;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Student progress endpoints. */
@RestController
@RequestMapping("/api/progress")
@Tag(name = "Progress")
public class ProgressController {

  private final ProgressService progressService;

  public ProgressController(ProgressService progressService) {
    this.progressService = progressService;
  }

  @GetMapping("/summary")
  @Operation(summary = "My progress summary: totals, average, streak, trend")
  public ProgressService.Summary summary(@AuthenticationPrincipal String userId) {
    return progressService.summary(UUID.fromString(userId));
  }

  @GetMapping("/by-reference")
  @Operation(summary = "My progress per reference step, with improvement")
  public List<ProgressService.ReferenceProgress> byReference(
      @AuthenticationPrincipal String userId) {
    return progressService.byReference(UUID.fromString(userId));
  }
}
