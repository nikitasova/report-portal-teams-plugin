package io.github.nikitasova.reportportal.extension.teams.model.enums;

import lombok.Getter;

@Getter
public enum TeamsEventType {

  LAUNCH_FINISHED("launchFinished", "Launch finished");

  private final String id;
  private final String label;

  TeamsEventType(String id, String label) {
    this.id = id;
    this.label = label;
  }
}