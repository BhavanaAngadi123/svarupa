package com.svarupa.library;

import com.svarupa.library.ReferenceService.ReferenceDto;
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

/** Reference library endpoints. Browsing is public; recording requires TEACHER/ADMIN. */
@RestController
@RequestMapping("/api/references")
@Tag(name = "References")
public class ReferenceController {

  private final ReferenceService referenceService;

  public ReferenceController(ReferenceService referenceService) {
    this.referenceService = referenceService;
  }

  @GetMapping
  @Operation(summary = "Browse active reference steps")
  public Page<ReferenceDto.View> search(
      @RequestParam(required = false) String form,
      @RequestParam(required = false) ReferenceStep.Difficulty difficulty,
      @RequestParam(required = false) String q,
      @PageableDefault(size = 24) Pageable pageable) {
    return referenceService.search(form, difficulty, q, pageable);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get one reference step with its joint angles")
  public ReferenceDto.View get(@PathVariable UUID id) {
    return referenceService.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Record a new reference step (teacher/admin)")
  public ReferenceDto.View create(
      @AuthenticationPrincipal String userId, @Valid @RequestBody ReferenceDto.Upsert upsert) {
    return referenceService.create(UUID.fromString(userId), upsert);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update your reference step (teacher/admin)")
  public ReferenceDto.View update(
      @AuthenticationPrincipal String userId,
      @PathVariable UUID id,
      @Valid @RequestBody ReferenceDto.Upsert upsert) {
    return referenceService.update(UUID.fromString(userId), id, upsert);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Deactivate your reference step (teacher/admin)")
  public void deactivate(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
    referenceService.deactivate(UUID.fromString(userId), id);
  }
}
