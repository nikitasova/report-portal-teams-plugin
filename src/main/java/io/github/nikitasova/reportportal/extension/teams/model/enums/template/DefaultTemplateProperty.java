package io.github.nikitasova.reportportal.extension.teams.model.enums.template;

import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import lombok.Getter;

@Getter
public enum DefaultTemplateProperty implements TemplateProperty {

  LAUNCH_ID("LAUNCH_ID"),
  LAUNCH_UUID("LAUNCH_UUID"),
  LAUNCH_NAME("LAUNCH_NAME"),
  LAUNCH_NUMBER("LAUNCH_NUMBER"),
  LAUNCH_START_TIME("LAUNCH_START_TIME"),
  LAUNCH_FINISH_TIME("LAUNCH_FINISH_TIME"),
  LAUNCH_MODE("LAUNCH_MODE"),
  LAUNCH_DESCRIPTION("LAUNCH_DESCRIPTION"),
  LAUNCH_ATTRIBUTES("LAUNCH_ATTRIBUTES"),
  LAUNCH_LINK("LAUNCH_LINK"),
  PROJECT_NAME("PROJECT_NAME"),
  RESULT_COLOR("RESULT_COLOR"),
  RESULT_COLOR_NOHASH("RESULT_COLOR_NOHASH"),
  RESULT_STYLE("RESULT_STYLE");

  private final String name;

  DefaultTemplateProperty(String name) {
    this.name = name;
  }
}
