package io.github.nikitasova.reportportal.extension.teams.collector.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty;
import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AttributesCollectorTest {

  private final AttributesCollector collector = new AttributesCollector();

  @Test
  void shouldCollectNonSystemAttributes() {
    Launch launch = new Launch();
    Set<ItemAttribute> attributes = new LinkedHashSet<>();

    ItemAttribute attr1 = new ItemAttribute();
    attr1.setKey("BRANCH");
    attr1.setValue("main");
    attr1.setSystem(false);
    attributes.add(attr1);

    ItemAttribute systemAttr = new ItemAttribute();
    systemAttr.setKey("agent");
    systemAttr.setValue("java-agent");
    systemAttr.setSystem(true);
    attributes.add(systemAttr);

    launch.setAttributes(attributes);

    Map<TemplateProperty, Object> result = collector.collect(launch);
    String attrString = (String) result.get(DefaultTemplateProperty.LAUNCH_ATTRIBUTES);

    assertTrue(attrString.contains("BRANCH:main"));
    // system attributes should be excluded
    assertTrue(!attrString.contains("agent"));
  }

  @Test
  void shouldHandleEmptyAttributes() {
    Launch launch = new Launch();
    launch.setAttributes(Collections.emptySet());

    Map<TemplateProperty, Object> result = collector.collect(launch);
    String attrString = (String) result.get(DefaultTemplateProperty.LAUNCH_ATTRIBUTES);

    assertEquals("", attrString);
  }
}
