package com.svarupa.config;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed config bound to {@code svarupa.*}. Fails fast when required secrets are missing. */
@ConfigurationProperties(prefix = "svarupa")
public class SvarupaProperties {

  private String jwtSecret;
  private long jwtAccessTtlSeconds = 900;
  private long jwtRefreshTtlSeconds = 604800;
  private String corsAllowedOrigins = "http://localhost:8080";
  private final Stripe stripe = new Stripe();
  private final Coach coach = new Coach();

  @PostConstruct
  void validate() {
    if (jwtSecret == null || jwtSecret.length() < 32) {
      throw new IllegalStateException(
          "svarupa.jwt-secret must be set to at least 32 characters (env SVARUPA_JWT_SECRET). "
              + "Generate one with: openssl rand -base64 48");
    }
  }

  public Duration accessTtl() {
    return Duration.ofSeconds(jwtAccessTtlSeconds);
  }

  public Duration refreshTtl() {
    return Duration.ofSeconds(jwtRefreshTtlSeconds);
  }

  public String getJwtSecret() {
    return jwtSecret;
  }

  public void setJwtSecret(String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  public long getJwtAccessTtlSeconds() {
    return jwtAccessTtlSeconds;
  }

  public void setJwtAccessTtlSeconds(long v) {
    this.jwtAccessTtlSeconds = v;
  }

  public long getJwtRefreshTtlSeconds() {
    return jwtRefreshTtlSeconds;
  }

  public void setJwtRefreshTtlSeconds(long v) {
    this.jwtRefreshTtlSeconds = v;
  }

  public String getCorsAllowedOrigins() {
    return corsAllowedOrigins;
  }

  public void setCorsAllowedOrigins(String v) {
    this.corsAllowedOrigins = v;
  }

  public Stripe getStripe() {
    return stripe;
  }

  public Coach getCoach() {
    return coach;
  }

  public static class Stripe {
    private boolean enabled = false;
    private String secretKey = "sk_test_PLACEHOLDER";
    private String webhookSecret = "whsec_PLACEHOLDER";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    public String getWebhookSecret() {
      return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
      this.webhookSecret = webhookSecret;
    }
  }

  public static class Coach {
    private boolean enabled = true;
    private String ollamaModel = "qwen3:8b";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getOllamaModel() {
      return ollamaModel;
    }

    public void setOllamaModel(String ollamaModel) {
      this.ollamaModel = ollamaModel;
    }
  }
}
