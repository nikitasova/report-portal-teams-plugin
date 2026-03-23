package io.github.nikitasova.reportportal.extension.teams.collector.launch;

import static io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty.LAUNCH_ATTRIBUTES;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.collector.PropertyCollector;
import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AttributesCollector implements PropertyCollector<Launch> {

  @Override
  public Map<TemplateProperty, Object> collect(Launch launch) {
    Map<TemplateProperty, Object> properties = new HashMap<>();
    Set<ItemAttribute> attributes = launch.getAttributes();
    String attributeString = Optional.ofNullable(attributes)
        .map(attrs -> attrs.stream()
            .filter(attr -> !attr.isSystem())
            .map(this::formatAttribute)
            .collect(Collectors.joining(", ")))
        .orElse("");
    properties.put(LAUNCH_ATTRIBUTES, attributeString);
    return properties;
  }

  private String formatAttribute(ItemAttribute attribute) {
    if (attribute.getKey() != null && !attribute.getKey().isEmpty()) {
      return attribute.getKey() + ":" + attribute.getValue();
    }
    return attribute.getValue();
  }
}
