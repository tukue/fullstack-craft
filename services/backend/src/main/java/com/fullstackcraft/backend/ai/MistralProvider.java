package com.fullstackcraft.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

/**
 * Mistral AI Chat Completions API provider (OpenAI-compatible endpoint
 * hosted by Mistral).
 * Activate with AI_PROVIDER=mistral and AI_MISTRAL_API_KEY=...
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mistral")
public class MistralProvider implements AiProvider {

  private final RestClient http;
  private final String model;

  public MistralProvider(
      @Value("${ai.mistral.base-url:https://api.mistral.ai/v1}") String baseUrl,
      @Value("${ai.mistral.api-key:}") String apiKey,
      @Value("${ai.mistral.model:mistral-large-latest}") String model) {
    this.model = model;
    this.http = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Override
  public String name() { return "mistral"; }
  @Override
  public String model() { return model; }

  @Override
  public String chat(String message) {
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "messages", List.of(Map.of("role", "user", "content", message)),
          "max_tokens", 4096);
      Map<String, Object> res = http.post().uri("/chat/completions").body(body)
          .retrieve().body(Map.class);
      if (res == null) return "";
      var choices = (List<Map<String, Object>>) res.get("choices");
      var msg = (Map<String, Object>) choices.get(0).get("message");
      return String.valueOf(msg.get("content"));
    } catch (Exception e) {
      return "[mistral] " + message + " (error: " + e.getMessage() + ")";
    }
  }
}
