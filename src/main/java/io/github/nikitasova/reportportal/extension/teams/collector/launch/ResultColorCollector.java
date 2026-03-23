package io.github.nikitasova.reportportal.extension.teams.collector.launch;

import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.RESULT_COLOR;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.RESULT_COLOR_NOHASH;
import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.RESULT_STYLE;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.collector.PropertyCollector;
import io.github.nikitasova.reportportal.extension.teams.model.enums.template.Color;
import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import java.util.HashMap;
import java.util.Map;

public class ResultColorCollector implements PropertyCollector<Launch> {

  @Override
  public Map<TemplateProperty, Object> collect(Launch launch) {
    Map<TemplateProperty, Object> properties = new HashMap<>();
    StatusEnum status = launch.getStatus();
    Color color = resolveColor(status);
    properties.put(RESULT_COLOR, color.getHexCode());
    properties.put(RESULT_COLOR_NOHASH, color.getHexCode().replace("#", ""));
    properties.put(RESULT_STYLE, resolveAdaptiveCardStyle(status));
    return properties;
  }

  private Color resolveColor(StatusEnum status) {
    if (status == null) {
      return Color.INTERRUPTED;
    }
    return switch (status) {
      case PASSED -> Color.PASSED;
      case FAILED -> Color.FAILED;
      default -> Color.INTERRUPTED;
    };
  }

  private String resolveAdaptiveCardStyle(StatusEnum status) {
    if (status == null) {
      return "warning";
    }
    return switch (status) {
      case PASSED -> "good";
      case FAILED -> "attention";
      default -> "warning";
    };
  }
}
