package com.svarupa.user;

import com.svarupa.common.Role;
import com.svarupa.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Registration, login, and refresh-token rotation. */
@Service
public class AuthService {

  private final UserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserRepository users,
      RefreshTokenRepository refreshTokens,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.users = users;
    this.refreshTokens = refreshTokens;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthTokens register(String email, String password, String displayName, Role role) {
    String normalized = email.trim().toLowerCase();
    if (users.existsByEmailIgnoreCase(normalized)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
    }
    Role finalRole = role == null ? Role.STUDENT : role;
    if (finalRole == Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot self-register as admin");
    }
    var user =
        new User(normalized, passwordEncoder.encode(password), displayName.trim(), finalRole);
    users.save(user);
    return issueTokens(user);
  }

  @Transactional
  public AuthTokens login(String email, String password) {
    var user =
        users
            .findByEmailIgnoreCase(email.trim().toLowerCase())
            .filter(User::isEnabled)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }
    return issueTokens(user);
  }

  @Transactional
  public AuthTokens refresh(String refreshToken) {
    UUID subject = jwtService.parseRefreshTokenSubject(refreshToken);
    var stored =
        refreshTokens
            .findByTokenHash(sha256(refreshToken))
            .filter(t -> !t.isRevoked() && !t.isExpired())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh token"));
    if (!stored.getUser().getId().equals(subject) || !stored.getUser().isEnabled()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh token");
    }
    stored.setRevoked(true); // rotation: single use
    return issueTokens(stored.getUser());
  }

  private AuthTokens issueTokens(User user) {
    String access = jwtService.issueAccessToken(user.getId(), user.getEmail(), user.getRole());
    String refresh = jwtService.issueRefreshToken(user.getId());
    refreshTokens.save(
        new RefreshToken(user, sha256(refresh), Instant.now().plusSeconds(7 * 24 * 3600)));
    return new AuthTokens(access, refresh, user.getId(), user.getEmail(), user.getDisplayName(),
        user.getRole());
  }

  static String sha256(String value) {
    try {
      var digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public record AuthTokens(
      String accessToken, String refreshToken, UUID userId, String email, String displayName, Role role) {}
}
