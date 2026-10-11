package com.svarupa.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * AI practice coach: Spring AI ChatClient backed by Ollama (local, free).
 * The ChatClient bean only exists when Ollama is reachable at startup
 * (probed with a 2-second timeout); otherwise CoachService runs in
 * offline (rule-based) mode. This keeps the app fast and safe on hosts
 * without Ollama (e.g. Render free tier) and in tests.
 */
@Configuration
public class AiConfig {

  @Bean
  @ConditionalOnProperty(name = "svarupa.coach.enabled", havingValue = "true", matchIfMissing = true)
  @Conditional(OllamaAvailable.class)
  public ChatClient coachChatClient(
      SvarupaProperties props,
      @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl) {
    String model =
        props.getCoach() != null && props.getCoach().getOllamaModel() != null
            ? props.getCoach().getOllamaModel()
            : "qwen3:8b";
    OllamaApi api = OllamaApi.builder().baseUrl(baseUrl).build();
    OllamaChatModel chatModel =
        OllamaChatModel.builder()
            .ollamaApi(api)
            .options(OllamaChatOptions.builder().model(model).temperature(0.4).build())
            .build();
    return ChatClient.builder(chatModel).build();
  }

  /** True when Ollama's /api/tags answers within 2 seconds. */
  static class OllamaAvailable implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      String baseUrl =
          context.getEnvironment().getProperty("spring.ai.ollama.base-url", "http://localhost:11434");
      try {
        HttpClient client =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        HttpRequest request =
            HttpRequest.newBuilder(URI.create(baseUrl + "/api/tags"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        HttpResponse<Void> response =
            client.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode() / 100 == 2;
      } catch (Exception e) {
        return false;
      }
    }
  }
}
