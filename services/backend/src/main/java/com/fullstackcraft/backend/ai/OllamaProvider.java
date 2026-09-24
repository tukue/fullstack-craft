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
 * Ollama local-model provider (runs self-hosted LLMs like llama3, mistral,
 * qwen, deepseek etc.).
 * Activate with AI_PROVIDER=ollama and AI_OLLAMA_BASE_URL=http://host:11434
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "ollama")
public class OllamaProvider implements AiProvider {

  private final RestClient http;
  private final String model;

  public OllamaProvider(
      @Value("${ai.ollama.base-url:http://localhost:11434}") String baseUrl,
      @Value("${ai.ollama.model:llama3}") String model) {
    this.model = model;
    this.http = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Override
  public String name() { return "ollama"; }
  @Override
  public String model() { return model; }

  @Override
  public String chat(String message) {
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "messages", List.of(Map.of("role", "user", "content", message)),
          "stream", false);
      Map<String, Object> res = http.post().uri("/api/generate").body(body)
          .retrieve().body(Map.class);
      if (res == null) return "";
      return String.valueOf(res.get("response"));
    } catch (Exception e) {
      return "[ollama] " + message + " (error: " + e.getMessage() + ")";
    }
  }
}
