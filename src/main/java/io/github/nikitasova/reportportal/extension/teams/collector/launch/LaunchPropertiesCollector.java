package io.github.nikitasova.reportportal.extension.teams.collector.launch;

import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_DESCRIPTION;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_FINISH_TIME;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_ID;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_MODE;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_NAME;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_NUMBER;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_START_TIME;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_UUID;

import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.collector.PropertyCollector;
import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LaunchPropertiesCollector implements PropertyCollector<Launch> {

  private static final int MAX_DESCRIPTION_LENGTH = 1000;

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

  @Override
  public Map<TemplateProperty, Object> collect(Launch launch) {
    Map<TemplateProperty, Object> properties = new HashMap<>();
    properties.put(LAUNCH_ID, launch.getId());
    properties.put(LAUNCH_UUID, launch.getUuid());
    properties.put(LAUNCH_NAME, launch.getName());
    properties.put(LAUNCH_NUMBER, launch.getNumber());
    properties.put(LAUNCH_MODE, launch.getMode());
    properties.put(LAUNCH_DESCRIPTION, truncate(
        Optional.ofNullable(launch.getDescription()).orElse("N/A")));
    properties.put(LAUNCH_START_TIME, formatInstant(launch.getStartTime()));
    properties.put(LAUNCH_FINISH_TIME, formatInstant(launch.getEndTime()));
    return properties;
  }

  private String formatInstant(Instant instant) {
    return instant != null ? FORMATTER.format(instant) : "N/A";
  }

  private String truncate(String value) {
    if (value.length() <= MAX_DESCRIPTION_LENGTH) {
      return value;
    }
    return value.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
  }
}
