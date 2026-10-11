package com.svarupa.follow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Follow + teacher dashboard endpoints. */
@RestController
@RequestMapping("/api/teachers")
@Tag(name = "Teachers")
public class FollowController {

  private final FollowService followService;

  public FollowController(FollowService followService) {
    this.followService = followService;
  }

  @PostMapping("/{teacherId}/follow")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Follow a teacher")
  public void follow(
      @AuthenticationPrincipal String userId, @PathVariable UUID teacherId) {
    followService.follow(UUID.fromString(userId), teacherId);
  }

  @DeleteMapping("/{teacherId}/follow")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Unfollow a teacher")
  public void unfollow(
      @AuthenticationPrincipal String userId, @PathVariable UUID teacherId) {
    followService.unfollow(UUID.fromString(userId), teacherId);
  }

  @GetMapping("/mine")
  @Operation(summary = "Teachers I follow")
  public List<UUID> myTeachers(@AuthenticationPrincipal String userId) {
    return followService.myTeachers(UUID.fromString(userId));
  }

  @GetMapping("/dashboard")
  @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
  @Operation(summary = "Teacher dashboard: followers with progress summaries")
  public FollowService.TeacherDashboard dashboard(@AuthenticationPrincipal String userId) {
    return followService.dashboard(UUID.fromString(userId));
  }
}
