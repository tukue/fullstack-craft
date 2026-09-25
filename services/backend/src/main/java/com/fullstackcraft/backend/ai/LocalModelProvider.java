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
 * Local model provider serving any OpenAI-compatible local endpoint
 * (e.g. LM Studio, Ollama in OpenAI mode, ollama-threads, llama-cpp-python).
 * Activate with AI_PROVIDER=local and AI_LOCAL_BASE_URL=http://localhost:1234/v1
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "local")
public class LocalModelProvider implements AiProvider {

  private final RestClient http;
  private final String model;

  public LocalModelProvider(
      @Value("${ai.local.base-url:http://localhost:1234/v1}") String baseUrl,
      @Value("${ai.local.model:local-model}") String model) {
    this.model = model;
    this.http = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Override
  public String name() { return "local"; }
  @Override
  public String model() { return model; }

  @Override
  public String chat(String message) {
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "messages", List.of(Map.of("role", "user", "content", message)),
          "stream", false);
      Map<String, Object> res = http.post().uri("/chat/completions").body(body)
          .retrieve().body(Map.class);
      if (res == null) return "";
      var choices = (List<Map<String, Object>>) res.get("choices");
      var msg = (Map<String, Object>) choices.get(0).get("message");
      return String.valueOf(msg.get("content"));
    } catch (Exception e) {
      return "[local] " + message + " (error: " + e.getMessage() + ")";
    }
  }
}
