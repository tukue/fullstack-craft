package com.fullstackcraft.backend.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

  private final AiService ai;
  private final AiProviderRegistry registry;

  public AiController(AiService ai, AiProviderRegistry registry) {
    this.ai = ai;
    this.registry = registry;
  }

  public record ChatRequest(
      @NotBlank String message,
      String provider,
      String model) {}

  @PostMapping("/chat")
  public AiService.ChatResponse chat(@Valid @RequestBody ChatRequest req) {
    String provider = (req.provider != null && !req.provider.isBlank())
        ? req.provider : registry.getFallback().name();
    return ai.chat(provider, req.model(), req.message());
  }

  @GetMapping("/health")
  public java.util.Map<String, String> health() {
    var active = registry.resolve(System.getenv().getOrDefault("AI_PROVIDER", "echo"));
    return java.util.Map.of(
        "provider", active.name(),
        "model", active.model(),
        "providers", String.join(",", registry.available()));
  }

  @GetMapping("/providers")
  public java.util.Map<String, java.util.Set<String>> providers() {
    return java.util.Map.of("available", registry.available());
  }
}
