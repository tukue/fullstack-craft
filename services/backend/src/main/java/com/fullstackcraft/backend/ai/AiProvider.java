package com.fullstackcraft.backend.ai;

/** Generic AI provider contract. Add new vendors by implementing this interface. */
public interface AiProvider {
  /** Provider id, e.g. "openai", "anthropic", "ollama", "echo". */
  String name();

  String chat(String message);

  default String model() {
    return "default";
  }
}
