package com.fullstackcraft.backend.ai;

/**
 * Generic AI provider contract. Add any vendor by implementing this
 * interface and annotating with @Component + @ConditionalOnProperty.
 */
public interface AiProvider {
  /** Provider id, e.g. "openai", "anthropic", "ollama", "gemini", "mistral", "local", "echo". */
  String name();

  /** The model id used for this request (e.g. "gpt-4o", "claude-sonnet-4", "llama3"). */
  String model();

  String chat(String message);
}
