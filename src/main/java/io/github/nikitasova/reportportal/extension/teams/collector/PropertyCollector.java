package io.github.nikitasova.reportportal.extension.teams.collector;

import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import java.util.Map;

public interface PropertyCollector<T> {

  Map<TemplateProperty, Object> collect(T source);
}
