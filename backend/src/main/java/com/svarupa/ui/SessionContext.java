package com.svarupa.ui;

import com.svarupa.common.Role;
import com.vaadin.flow.server.VaadinSession;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Auth state for the Vaadin UI session. The JWT is mirrored into the
 * browser's localStorage (same origin) so the embedded practice tracker
 * (static page) can call the REST API directly.
 */
@Component
@SessionScope
public class SessionContext {

  private String accessToken;
  private String refreshToken;
  private UUID userId;
  private String email;
  private String displayName;
  private Role role;

  public boolean isLoggedIn() {
    return accessToken != null && userId != null;
  }

  public void login(
      String accessToken, String refreshToken, UUID userId, String email, String displayName,
      Role role) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.userId = userId;
    this.email = email;
    this.displayName = displayName;
    this.role = role;
  }

  public void logout() {
    accessToken = null;
    refreshToken = null;
    userId = null;
    email = null;
    displayName = null;
    role = null;
    VaadinSession.getCurrent().getSession().invalidate();
  }

  public String getAccessToken() {
    return accessToken;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Role getRole() {
    return role;
  }

  public boolean isTeacher() {
    return role == Role.TEACHER || role == Role.ADMIN;
  }

  public boolean isAdmin() {
    return role == Role.ADMIN;
  }
}
