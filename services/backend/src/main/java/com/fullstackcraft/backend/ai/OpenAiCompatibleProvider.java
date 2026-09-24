package com.fullstackcraft.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

/**
 * OpenAI-compatible provider (OpenAI, Azure OpenAI, Together, Groq, Ollama
 * OpenAI-mode, etc.). Generic: only base-url + api-key + model differ.
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiCompatibleProvider implements AiProvider {

  private final RestClient http;
  private final String model;

  public OpenAiCompatibleProvider(
      @Value("${ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
      @Value("${ai.openai.api-key:}") String apiKey,
      @Value("${ai.openai.model:gpt-4o-mini}") String model) {
    this.model = model;
    this.http = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Override
  public String name() {
    return "openai";
  }

  @Override
  public String model() {
    return model;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String chat(String message) {
    if (http == null) return "[openai not configured] " + message;
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "messages", new Object[] { Map.of("role", "user", "content", message) });
      Map<String, Object> res = http.post().uri("/chat/completions").body(body)
          .retrieve().body(Map.class);
      if (res == null) return "";
      var choices = (java.util.List<Map<String, Object>>) res.get("choices");
      var msg = (Map<String, Object>) choices.get(0).get("message");
      return String.valueOf(msg.get("content"));
    } catch (Exception e) {
      // Fail soft in scaffold: echo mode so frontend works without a key.
      return "[openai fallback] " + message + " (" + e.getMessage() + ")";
    }
  }
}
