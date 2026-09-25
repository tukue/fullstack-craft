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
 * Anthropic Messages API provider.
 * Activate with AI_PROVIDER=anthropic and AI_ANTHROPIC_API_KEY=...
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "anthropic")
public class AnthropicProvider implements AiProvider {

  private final RestClient http;
  private final String model;
  private final double maxTokens;

  public AnthropicProvider(
      @Value("${ai.anthropic.base-url:https://api.anthropic.com/v1}") String baseUrl,
      @Value("${ai.anthropic.api-key:}") String apiKey,
      @Value("${ai.anthropic.model:claude-sonnet-4-20250514}") String model,
      @Value("${ai.anthropic.max-tokens:4096}") double maxTokens) {
    this.model = model;
    this.maxTokens = maxTokens;
    this.http = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("anthropic-version", "2023-06-01")
        .build();
  }

  @Override
  public String name() { return "anthropic"; }
  @Override
  public String model() { return model; }

  @Override
  public String chat(String message) {
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "max_tokens", (int) maxTokens,
          "messages", List.of(Map.of("role", "user", "content", message)));
      Map<String, Object> res = http.post().uri("/messages").body(body)
          .retrieve().body(Map.class);
      if (res == null) return "";
      var content = (List<Map<String, Object>>) res.get("content");
      return String.valueOf(content.get(0).get("text"));
    } catch (Exception e) {
      return "[anthropic] " + message + " (error: " + e.getMessage() + ")";
    }
  }
}
