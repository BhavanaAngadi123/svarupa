package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.common.Role;
import com.svarupa.library.ReferenceService;
import com.svarupa.library.ReferenceService.ReferenceDto;
import com.svarupa.library.ReferenceStep;
import com.svarupa.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

class ReferenceServiceTest extends IntegrationTestBase {

  @Autowired ReferenceService referenceService;

  private ReferenceDto.Upsert upsert(String title) {
    return new ReferenceDto.Upsert(
        title, "Kuchipudi", "A test step", POSE_A, ReferenceStep.Difficulty.BEGINNER);
  }

  @Test
  void teacherCanCreateReference() {
    var view =
        runAs(teacher, () -> referenceService.create(teacher.getId(), upsert("My Step")));
    assertThat(view.id()).isNotNull();
    assertThat(view.title()).isEqualTo("My Step");
    assertThat(view.teacherName()).isEqualTo(teacher.getDisplayName());
    assertThat(view.jointAngles()).isEqualTo(POSE_A);
  }

  @Test
  void studentCannotCreateReference() {
    assertThatThrownBy(
            () -> runAs(student, () -> referenceService.create(student.getId(), upsert("Nope"))))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void browseIsPublic() {
    var page = referenceService.search(null, null, null, PageRequest.of(0, 24));
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
  }

  @Test
  void searchFiltersByQuery() {
    runAs(teacher, () -> referenceService.create(teacher.getId(), upsert("UniqueTitleXYZ")));
    var page = referenceService.search(null, null, "uniquetitlexyz", PageRequest.of(0, 24));
    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent().get(0).title()).isEqualTo("UniqueTitleXYZ");
  }

  @Test
  void searchFiltersByDifficulty() {
    var up =
        new ReferenceDto.Upsert(
            "Hard Step", "Kuchipudi", "hard", POSE_A, ReferenceStep.Difficulty.ADVANCED);
    runAs(teacher, () -> referenceService.create(teacher.getId(), up));
    var page =
        referenceService.search(
            null, ReferenceStep.Difficulty.ADVANCED, null, PageRequest.of(0, 24));
    assertThat(page.getContent()).allMatch(v -> v.difficulty() == ReferenceStep.Difficulty.ADVANCED);
  }

  @Test
  void teacherCanUpdateOwnReference() {
    var created =
        runAs(teacher, () -> referenceService.create(teacher.getId(), upsert("Before")));
    var updated =
        runAs(
            teacher,
            () ->
                referenceService.update(
                    teacher.getId(), created.id(), upsert("After")));
    assertThat(updated.title()).isEqualTo("After");
  }

  @Test
  void studentUpdateBlockedByRoleCheck() {
    var created =
        runAs(teacher, () -> referenceService.create(teacher.getId(), upsert("Mine")));
    // The @PreAuthorize role check fires before the ownership check.
    assertThatThrownBy(
            () ->
                runAs(
                    student,
                    () ->
                        referenceService.update(
                            student.getId(), created.id(), upsert("Hijack"))))
        .isInstanceOf(org.springframework.security.authorization.AuthorizationDeniedException.class);
  }

  @Test
  void teacherCannotUpdateAnotherTeachersReference() {
    var created =
        runAs(teacher, () -> referenceService.create(teacher.getId(), upsert("Mine")));
    var otherTeacher =
        users.save(
            new User(
                "other-" + java.util.UUID.randomUUID().toString().substring(0, 6) + "@test.local",
                passwordEncoder.encode("Teacher!123"),
                "Other Teacher",
                Role.TEACHER));
    assertThatThrownBy(
            () ->
                runAs(
                    otherTeacher,
                    () ->
                        referenceService.update(
                            otherTeacher.getId(), created.id(), upsert("Hijack"))))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("not your reference");
  }

  @Test
  void deactivateHidesFromBrowse() {
    var created =
        runAs(teacher, () -> referenceService.create(teacher.getId(), upsert("Temp Step")));
    runAsVoid(teacher, () -> referenceService.deactivate(teacher.getId(), created.id()));
    assertThatThrownBy(() -> referenceService.get(created.id()))
        .isInstanceOf(ResponseStatusException.class);
  }
}
