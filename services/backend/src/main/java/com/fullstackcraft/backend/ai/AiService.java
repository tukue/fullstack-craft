package com.fullstackcraft.backend.ai;

import org.springframework.stereotype.Service;

@Service
public class AiService {
  private final AiProvider provider;

  public AiService(AiProvider provider) {
    this.provider = provider;
  }

  public ChatResponse chat(String message) {
    return new ChatResponse(provider.chat(message), provider.name(), provider.model());
  }

  public record ChatResponse(String reply, String provider, String model) {}
}
