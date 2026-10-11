package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.common.Role;
import com.svarupa.user.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest extends IntegrationTestBase {

  @Autowired AuthService authService;

  @Test
  void registerIssuesTokenPair() {
    var tokens = authService.register("new@test.local", "Password!123", "Newbie", Role.STUDENT);
    assertThat(tokens.accessToken()).isNotBlank();
    assertThat(tokens.refreshToken()).isNotBlank();
    assertThat(tokens.role()).isEqualTo(Role.STUDENT);
    assertThat(users.findByEmailIgnoreCase("new@test.local")).isPresent();
  }

  @Test
  void registerDefaultsToStudentWhenRoleNull() {
    var tokens = authService.register("nonull@test.local", "Password!123", "No Role", null);
    assertThat(tokens.role()).isEqualTo(Role.STUDENT);
  }

  @Test
  void registerTeacherRole() {
    var tokens = authService.register("teach2@test.local", "Password!123", "Guru", Role.TEACHER);
    assertThat(tokens.role()).isEqualTo(Role.TEACHER);
  }

  @Test
  void duplicateEmailRejected() {
    authService.register("dup@test.local", "Password!123", "One", Role.STUDENT);
    assertThatThrownBy(
            () -> authService.register("DUP@test.local", "Password!123", "Two", Role.STUDENT))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("already registered");
  }

  @Test
  void cannotSelfRegisterAsAdmin() {
    assertThatThrownBy(
            () -> authService.register("evil@test.local", "Password!123", "Evil", Role.ADMIN))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void loginSucceedsWithCorrectPassword() {
    authService.register("login@test.local", "Password!123", "Logger", Role.STUDENT);
    var tokens = authService.login("login@test.local", "Password!123");
    assertThat(tokens.accessToken()).isNotBlank();
  }

  @Test
  void loginFailsWithWrongPassword() {
    authService.register("login2@test.local", "Password!123", "Logger", Role.STUDENT);
    assertThatThrownBy(() -> authService.login("login2@test.local", "wrong-password"))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void refreshRotatesTokens() {
    var first = authService.register("ref@test.local", "Password!123", "Ref", Role.STUDENT);
    var second = authService.refresh(first.refreshToken());
    assertThat(second.accessToken()).isNotEqualTo(first.accessToken());
    // Old refresh token is single-use.
    assertThatThrownBy(() -> authService.refresh(first.refreshToken()))
        .isInstanceOf(ResponseStatusException.class);
  }
}
