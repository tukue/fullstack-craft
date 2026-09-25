package com.fullstackcraft.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fullstackcraft.backend.ai.AiProviderRegistry;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "ai.provider=echo")
class ApplicationTests {
  @Autowired
  AiProviderRegistry registry;

  @Test
  void contextLoads() {}

  @Test
  void echoProviderIsAvailable() {
    assertTrue(registry.available().contains("echo"));
  }
}
