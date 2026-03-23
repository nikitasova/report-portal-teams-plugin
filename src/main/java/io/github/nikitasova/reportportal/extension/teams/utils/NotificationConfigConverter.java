package io.github.nikitasova.reportportal.extension.teams.utils;

import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import java.util.function.Function;

public class NotificationConfigConverter implements Function<LaunchAttributeRule, ItemAttributeResource> {

  @Override
  public ItemAttributeResource apply(LaunchAttributeRule rule) {
    ItemAttributeResource resource = new ItemAttributeResource();
    resource.setKey(rule.getKey());
    resource.setValue(rule.getValue());
    return resource;
  }
}
