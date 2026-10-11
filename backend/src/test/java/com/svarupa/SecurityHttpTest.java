package com.svarupa;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svarupa.common.Role;
import com.svarupa.user.AuthService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class SecurityHttpTest extends IntegrationTestBase {

  @Autowired MockMvc mockMvc;
  @Autowired AuthService authService;
  @Autowired ObjectMapper objectMapper;

  private String studentToken() {
    return "Bearer " + authService.login(student.getEmail(), "Student!123").accessToken();
  }

  private String teacherToken() {
    return "Bearer " + authService.login(teacher.getEmail(), "Teacher!123").accessToken();
  }

  @Test
  void healthIsPublic() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void browseReferencesIsPublic() throws Exception {
    mockMvc.perform(get("/api/references")).andExpect(status().isOk());
  }

  @Test
  void coachStatusIsPublic() throws Exception {
    mockMvc.perform(get("/api/coach/status")).andExpect(status().isOk());
  }

  @Test
  void sessionsRequireAuth() throws Exception {
    mockMvc.perform(get("/api/sessions/mine")).andExpect(status().isUnauthorized());
  }

  @Test
  void progressRequiresAuth() throws Exception {
    mockMvc.perform(get("/api/progress/summary")).andExpect(status().isUnauthorized());
  }

  @Test
  void invalidTokenRejected() throws Exception {
    mockMvc
        .perform(get("/api/sessions/mine").header("Authorization", "Bearer garbage"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void studentCanLogSessionOverHttp() throws Exception {
    var body =
        Map.of(
            "referenceStepId", reference.getId().toString(),
            "finalAngles", Map.of("lKnee", 95, "rKnee", 95, "lElbow", 160, "rElbow", 160,
                "lShoulder", 45, "rShoulder", 45, "lHip", 100, "rHip", 100),
            "durationSeconds", 120,
            "frameCount", 60);
    mockMvc
        .perform(
            post("/api/sessions")
                .header("Authorization", studentToken())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.overallMatchPct").value(100.0));
  }

  @Test
  void studentCannotCreateReferenceOverHttp() throws Exception {
    var body =
        Map.of(
            "title", "Sneaky",
            "jointAngles", Map.of("lKnee", 95, "rKnee", 95, "lElbow", 160, "rElbow", 160,
                "lShoulder", 45, "rShoulder", 45, "lHip", 100, "rHip", 100));
    mockMvc
        .perform(
            post("/api/references")
                .header("Authorization", studentToken())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isForbidden());
  }

  @Test
  void teacherCanCreateReferenceOverHttp() throws Exception {
    var body =
        Map.of(
            "title", "Teacher Step",
            "jointAngles", Map.of("lKnee", 95, "rKnee", 95, "lElbow", 160, "rElbow", 160,
                "lShoulder", 45, "rShoulder", 45, "lHip", 100, "rHip", 100));
    mockMvc
        .perform(
            post("/api/references")
                .header("Authorization", teacherToken())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Teacher Step"));
  }

  @Test
  void teacherDashboardForbiddenForStudent() throws Exception {
    mockMvc
        .perform(get("/api/teachers/dashboard").header("Authorization", studentToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void registerOverHttp() throws Exception {
    var body =
        Map.of(
            "email", "http" + UUID.randomUUID().toString().substring(0, 6) + "@test.local",
            "password", "Password!123",
            "displayName", "Http User",
            "role", "STUDENT");
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @Test
  void swaggerUiIsPublic() throws Exception {
    // /swagger-ui.html redirects to the UI bundle — public, not a login wall.
    mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
  }
}
