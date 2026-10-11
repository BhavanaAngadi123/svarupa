package com.svarupa.user;

import com.svarupa.common.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Public auth endpoints. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register a new student or teacher account")
  public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
    var tokens = authService.register(req.email(), req.password(), req.displayName(), req.role());
    return toResponse(tokens);
  }

  @PostMapping("/login")
  @Operation(summary = "Log in and receive access + refresh tokens")
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    return toResponse(authService.login(req.email(), req.password()));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Rotate the refresh token for a new token pair")
  public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
    return toResponse(authService.refresh(req.refreshToken()));
  }

  private static AuthResponse toResponse(AuthService.AuthTokens t) {
    return new AuthResponse(
        t.accessToken(), t.refreshToken(), t.userId(), t.email(), t.displayName(), t.role());
  }

  public record RegisterRequest(
      @Email @NotBlank String email,
      @Size(min = 8, max = 100) String password,
      @NotBlank @Size(max = 120) String displayName,
      Role role) {
    public RegisterRequest {
      if (role == null) {
        role = Role.STUDENT;
      }
    }
  }

  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

  public record RefreshRequest(@NotBlank String refreshToken) {}

  public record AuthResponse(
      String accessToken,
      String refreshToken,
      UUID userId,
      String email,
      String displayName,
      Role role) {}
}
