package io.github.nikitasova.reportportal.extension.teams.factory;

import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.collector.PropertyCollector;
import io.github.nikitasova.reportportal.extension.teams.collector.launch.AttributesCollector;
import io.github.nikitasova.reportportal.extension.teams.collector.launch.LaunchPropertiesCollector;
import io.github.nikitasova.reportportal.extension.teams.collector.launch.ResultColorCollector;
import io.github.nikitasova.reportportal.extension.teams.collector.launch.StatisticsPropertiesCollector;
import java.util.List;

public class PropertyCollectorFactory {

  private final List<PropertyCollector<Launch>> defaultPropertyCollectors;

  public PropertyCollectorFactory() {
    defaultPropertyCollectors = List.of(
        new LaunchPropertiesCollector(),
        new AttributesCollector(),
        new StatisticsPropertiesCollector(),
        new ResultColorCollector()
    );
  }

  public List<PropertyCollector<Launch>> getDefaultPropertyCollectors() {
    return defaultPropertyCollectors;
  }
}
