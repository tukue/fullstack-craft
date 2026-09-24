package com.fullstackcraft.backend.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

  private final AiService ai;

  public AiController(AiService ai) {
    this.ai = ai;
  }

  public record ChatRequest(@NotBlank String message) {}

  @PostMapping("/chat")
  public AiService.ChatResponse chat(@Valid @RequestBody ChatRequest req) {
    return ai.chat(req.message());
  }

  @GetMapping("/health")
  public java.util.Map<String, String> health(org.springframework.beans.factory.ObjectProvider<AiProvider> provider) {
    var p = provider.getIfAvailable();
    return java.util.Map.of(
        "provider", p == null ? "none" : p.name(),
        "model", p == null ? "none" : p.model());
  }
}
