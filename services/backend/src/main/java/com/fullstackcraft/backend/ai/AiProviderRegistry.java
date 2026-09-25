package com.fullstackcraft.backend.ai;

import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registry that resolves the active {@link AiProvider} by name.
 * Delegates to Spring's bean lookup so any @Component implementing
 * AiProvider is auto-wired here.
 */
@Component
public class AiProviderRegistry {

  private final Map<String, AiProvider> providers;
  private final AiProvider fallback;

  public AiProviderRegistry(List<AiProvider> allProviders, AiProvider fallback) {
    this.providers = allProviders.stream()
        .collect(Collectors.toMap(
            AiProvider::name, p -> p, (a, b) -> b, LinkedHashMap::new));
    this.fallback = fallback;
  }

  public AiProvider resolve(String name) {
    return providers.getOrDefault(name, fallback);
  }

  public Set<String> available() {
    return providers.keySet();
  }

  public AiProvider getFallback() {
    return fallback;
  }
}
