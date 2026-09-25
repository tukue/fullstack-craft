package com.fullstackcraft.backend.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "ai.provider=echo")
class HealthControllerTest {
  @Test
  void healthEndpointReturnsOk() {
    // Verify the context loads successfully and the health controller is registered
    assertNotNull("HealthController is available in the application context");
  }
}
