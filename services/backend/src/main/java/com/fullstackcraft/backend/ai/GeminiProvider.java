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
 * Google Gemini Generative Language API provider.
 * Activate with AI_PROVIDER=gemini and AI_GEMINI_API_KEY=...
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiProvider implements AiProvider {

  private final RestClient http;
  private final String model;

  public GeminiProvider(
      @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
      @Value("${ai.gemini.api-key:}") String apiKey,
      @Value("${ai.gemini.model:gemini-2.5-flash}") String model) {
    this.model = model;
    this.http = RestClient.builder()
        .baseUrl(baseUrl + "/models/" + model + ":generateContent")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .build();
  }

  @Override
  public String name() { return "gemini"; }
  @Override
  public String model() { return model; }

  @Override
  public String chat(String message) {
    try {
      Map<String, Object> body = Map.of(
          "contents", List.of(Map.of(
              "role", "user",
              "parts", List.of(Map.of("text", message)))));
      Map<String, Object> res = http.post().uri("").body(body)
          .retrieve().body(Map.class);
      if (res == null) return "";
      var candidates = (List<Map<String, Object>>) res.get("candidates");
      var content = (Map<String, Object>) candidates.get(0).get("content");
      var parts = (List<Map<String, Object>>) content.get("parts");
      return String.valueOf(parts.get(0).get("text"));
    } catch (Exception e) {
      return "[gemini] " + message + " (error: " + e.getMessage() + ")";
    }
  }
}
