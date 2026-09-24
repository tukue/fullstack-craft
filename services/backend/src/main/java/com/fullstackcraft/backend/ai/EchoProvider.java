package com.fullstackcraft.backend.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local stub used for dev/test without vendor keys.
 * Always present as the default provider when AI_PROVIDER is not set,
 * or explicitly activated with AI_PROVIDER=echo.
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "echo", matchIfMissing = true)
public class EchoProvider implements AiProvider {
  @Override
  public String name() { return "echo"; }
  @Override
  public String model() { return "echo-v1"; }
  @Override
  public String chat(String message) { return "Echo: " + message; }
}
