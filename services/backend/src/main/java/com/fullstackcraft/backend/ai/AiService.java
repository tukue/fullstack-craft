package com.fullstackcraft.backend.ai;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;

@Service
public class AiService {

  private final AiProviderRegistry registry;

  public AiService(AiProviderRegistry registry) {
    this.registry = registry;
  }

  public ChatResponse chat(String providerName, String modelOverride, String message) {
    var provider = registry.resolve(providerName);
    return new ChatResponse(provider.chat(message), provider.name(), provider.model());
  }

  public AvailableProviders available() {
    return new AvailableProviders(registry.available());
  }

  public record ChatResponse(String reply, String provider, String model) {}
  public record AvailableProviders(Set<String> providers) {}
}
