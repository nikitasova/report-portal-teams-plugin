package io.github.nikitasova.reportportal.extension.teams.collector.launch;

import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import io.github.nikitasova.reportportal.extension.teams.collector.PropertyCollector;
import io.github.nikitasova.reportportal.extension.teams.model.enums.template.StatisticTemplateProperty;
import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StatisticsPropertiesCollector implements PropertyCollector<Launch> {

  @Override
  public Map<TemplateProperty, Object> collect(Launch launch) {
    Map<TemplateProperty, Object> properties = new HashMap<>();

    for (StatisticTemplateProperty prop : StatisticTemplateProperty.values()) {
      properties.put(prop, 0);
    }

    Set<Statistics> statisticsSet = launch.getStatistics();
    Optional.ofNullable(statisticsSet).ifPresent(stats ->
        stats.forEach(stat -> {
          String fieldName = stat.getStatisticsField().getName();
          Integer counter = stat.getCounter();
          for (StatisticTemplateProperty prop : StatisticTemplateProperty.values()) {
            if (prop.getName().equals(fieldName)) {
              properties.put(prop, counter != null ? counter : 0);
              break;
            }
          }
        }));
    return properties;
  }
}
