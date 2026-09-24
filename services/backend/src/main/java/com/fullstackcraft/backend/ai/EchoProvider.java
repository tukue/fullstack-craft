package com.fullstackcraft.backend.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local stub used for dev/test without vendor keys.
 * Activate with: AI_PROVIDER=echo
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "echo")
public class EchoProvider implements AiProvider {
  @Override
  public String name() {
    return "echo";
  }

  @Override
  public String chat(String message) {
    return "Echo: " + message;
  }
}
