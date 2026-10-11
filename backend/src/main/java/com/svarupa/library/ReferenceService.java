package com.svarupa.library;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svarupa.user.User;
import com.svarupa.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Teacher reference library: record, browse, and manage reference steps. */
@Service
public class ReferenceService {

  private final ReferenceStepRepository references;
  private final UserRepository users;
  private final ObjectMapper objectMapper;

  public ReferenceService(
      ReferenceStepRepository references, UserRepository users, ObjectMapper objectMapper) {
    this.references = references;
    this.users = users;
    this.objectMapper = objectMapper;
  }

  @Transactional
  @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
  public ReferenceDto.View create(UUID teacherId, @Valid ReferenceDto.Upsert upsert) {
    var teacher =
        users
            .findById(teacherId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    var step = new ReferenceStep();
    step.setTeacher(teacher);
    apply(upsert, step);
    var saved = references.save(step);
    return ReferenceDto.View.of(saved, teacher.getDisplayName(), parseAngles(saved));
  }

  @Transactional(readOnly = true)
  public Page<ReferenceDto.View> search(
      String form, ReferenceStep.Difficulty difficulty, String q, Pageable pageable) {
    String qPattern =
        q == null || q.isBlank() ? null : "%" + q.toLowerCase(java.util.Locale.ROOT) + "%";
    return references.search(form, difficulty, q, qPattern, pageable).map(this::toView);
  }

  @Transactional(readOnly = true)
  public ReferenceDto.View get(UUID id) {
    var step =
        references
            .findById(id)
            .filter(ReferenceStep::isActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reference not found"));
    return toView(step);
  }

  @Transactional
  @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
  public ReferenceDto.View update(UUID teacherId, UUID id, @Valid ReferenceDto.Upsert upsert) {
    var step = ownedStep(teacherId, id);
    apply(upsert, step);
    return toView(step);
  }

  @Transactional
  @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
  public void deactivate(UUID teacherId, UUID id) {
    var step = ownedStep(teacherId, id);
    step.setActive(false);
  }

  private ReferenceStep ownedStep(UUID teacherId, UUID id) {
    var step =
        references
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "reference not found"));
    boolean isAdmin =
        users.findById(teacherId).map(u -> u.getRole() == com.svarupa.common.Role.ADMIN).orElse(false);
    if (!step.getTeacher().getId().equals(teacherId) && !isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your reference step");
    }
    return step;
  }

  private void apply(ReferenceDto.Upsert upsert, ReferenceStep step) {
    step.setTitle(upsert.title().trim());
    step.setDanceForm(upsert.danceForm() == null || upsert.danceForm().isBlank()
        ? "Kuchipudi" : upsert.danceForm().trim());
    step.setDescription(upsert.description());
    step.setDifficulty(upsert.difficulty() == null ? ReferenceStep.Difficulty.BEGINNER : upsert.difficulty());
    step.setJointAngles(toJson(upsert.jointAngles()));
  }

  private ReferenceDto.View toView(ReferenceStep step) {
    return ReferenceDto.View.of(step, step.getTeacher().getDisplayName(), parseAngles(step));
  }

  public JointAngles parseAngles(ReferenceStep step) {
    try {
      return objectMapper.readValue(step.getJointAngles(), JointAngles.class);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "corrupt reference angles");
    }
  }

  private String toJson(JointAngles angles) {
    try {
      return objectMapper.writeValueAsString(angles);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid joint angles");
    }
  }

  // ---- DTOs ----

  public static final class ReferenceDto {
    private ReferenceDto() {}

    public record Upsert(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 64) String danceForm,
        String description,
        @NotNull @Valid JointAngles jointAngles,
        ReferenceStep.Difficulty difficulty) {}

    public record View(
        UUID id,
        String title,
        String danceForm,
        String description,
        JointAngles jointAngles,
        ReferenceStep.Difficulty difficulty,
        String teacherName,
        UUID teacherId,
        int practiceCount,
        Instant createdAt) {
      static View of(ReferenceStep step, String teacherName, JointAngles angles) {
        return new View(
            step.getId(),
            step.getTitle(),
            step.getDanceForm(),
            step.getDescription(),
            angles,
            step.getDifficulty(),
            teacherName,
            step.getTeacher().getId(),
            step.getPracticeCount(),
            step.getCreatedAt());
      }
    }
  }
}
