package io.github.nikitasova.reportportal.extension.teams.collector.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty;
import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LaunchPropertiesCollectorTest {

  private final LaunchPropertiesCollector collector = new LaunchPropertiesCollector();

  @Test
  void shouldCollectAllLaunchProperties() {
    Launch launch = new Launch();
    launch.setId(42L);
    launch.setUuid("uuid-123");
    launch.setName("My Launch");
    launch.setNumber(7L);
    launch.setDescription("Test description");
    launch.setStartTime(Instant.parse("2025-01-15T10:00:00Z"));
    launch.setEndTime(Instant.parse("2025-01-15T10:05:30Z"));

    Map<TemplateProperty, Object> result = collector.collect(launch);

    assertEquals(42L, result.get(DefaultTemplateProperty.LAUNCH_ID));
    assertEquals("uuid-123", result.get(DefaultTemplateProperty.LAUNCH_UUID));
    assertEquals("My Launch", result.get(DefaultTemplateProperty.LAUNCH_NAME));
    assertEquals(7L, result.get(DefaultTemplateProperty.LAUNCH_NUMBER));
    assertEquals("Test description", result.get(DefaultTemplateProperty.LAUNCH_DESCRIPTION));
    assertEquals("2025-01-15 10:00:00", result.get(DefaultTemplateProperty.LAUNCH_START_TIME));
    assertEquals("2025-01-15 10:05:30", result.get(DefaultTemplateProperty.LAUNCH_FINISH_TIME));
  }

  @Test
  void shouldHandleNullDescription() {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setName("Test");
    launch.setNumber(1L);

    Map<TemplateProperty, Object> result = collector.collect(launch);

    assertEquals("N/A", result.get(DefaultTemplateProperty.LAUNCH_DESCRIPTION));
  }
}
