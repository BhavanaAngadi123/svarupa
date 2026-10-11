package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.follow.FollowService;
import com.svarupa.library.JointAngles;
import com.svarupa.practice.PracticeService;
import com.svarupa.practice.PracticeService.SessionDto;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

class FollowServiceTest extends IntegrationTestBase {

  @Autowired FollowService followService;
  @Autowired PracticeService practiceService;

  @Test
  void studentCanFollowTeacher() {
    followService.follow(student.getId(), teacher.getId());
    assertThat(followService.myTeachers(student.getId())).contains(teacher.getId());
  }

  @Test
  void followIsIdempotent() {
    followService.follow(student.getId(), teacher.getId());
    followService.follow(student.getId(), teacher.getId());
    assertThat(followService.myTeachers(student.getId())).hasSize(1);
  }

  @Test
  void cannotFollowSelf() {
    assertThatThrownBy(() -> followService.follow(student.getId(), student.getId()))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void cannotFollowNonTeacher() {
    assertThatThrownBy(() -> followService.follow(student.getId(), admin.getId()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("teacher not found");
  }

  @Test
  void unfollowRemoves() {
    followService.follow(student.getId(), teacher.getId());
    followService.unfollow(student.getId(), teacher.getId());
    assertThat(followService.myTeachers(student.getId())).isEmpty();
  }

  @Test
  void teacherDashboardShowsStudentProgress() {
    followService.follow(student.getId(), teacher.getId());
    practiceService.logSession(
        student.getId(),
        new SessionDto.LogRequest(
            reference.getId(), new JointAngles(105, 95, 160, 160, 45, 45, 100, 100),
            Instant.now(), 300, 60));
    var dash = followService.dashboard(teacher.getId());
    assertThat(dash.studentCount()).isEqualTo(1);
    assertThat(dash.students()).hasSize(1);
    var sp = dash.students().get(0);
    assertThat(sp.displayName()).isEqualTo(student.getDisplayName());
    assertThat(sp.totalSessions()).isEqualTo(1);
    assertThat(sp.avgMatchPct()).isNotNull();
  }

  @Test
  void emptyDashboardForNewTeacher() {
    var dash = followService.dashboard(teacher.getId());
    assertThat(dash.studentCount()).isEqualTo(0);
    assertThat(dash.students()).isEmpty();
  }
}
