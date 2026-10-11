package com.svarupa.security;

import com.svarupa.config.SvarupaProperties;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security: JWT stateless auth, RBAC via @PreAuthorize, strict CORS.
 * Camera is explicitly allowed (permissions policy) — the practice tracker
 * needs it. Vaadin uses its own CSRF protection.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final SvarupaProperties props;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, SvarupaProperties props) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.props = props;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // stateless JWT API; Vaadin uses its own CSRF
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public: auth, docs, health, reference browsing, tracker page
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/webhooks/**")
                    .permitAll() // signature-verified
                    .requestMatchers("/api/coach/status")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/references", "/api/references/**")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/actuator/info",
                        "/error")
                    .permitAll()
                    // Vaadin frontend routes + embedded practice tracker (static)
                    .requestMatchers(
                        "/",
                        "/login",
                        "/practice",
                        "/library",
                        "/progress",
                        "/teacher/**",
                        "/admin/**",
                        "/svarupa-tracker/**",
                        "/VAADIN/**",
                        "/frontend/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                    .accessDeniedHandler(
                        (req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN)))
        .headers(
            headers ->
                headers
                    .contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                    + "style-src 'self' 'unsafe-inline'; img-src 'self' data: blob: https:; "
                                    + "worker-src 'self' blob:; connect-src 'self' https://cdn.jsdelivr.net"))
                    .referrerPolicy(
                        referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    // Camera allowed for the in-browser pose tracker.
                    .permissionsPolicy(
                        permissions -> permissions.policy("camera=(self), microphone=(), geolocation=()")))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins =
        Arrays.stream(props.getCorsAllowedOrigins().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Stripe-Signature"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }
}
