package io.github.nikitasova.reportportal.extension.teams.model.enums;

import lombok.Getter;

@Getter
public enum TeamsIntegrationProperties {

  WEBHOOK_URL("webhookURL");

  private final String name;

  TeamsIntegrationProperties(String name) {
    this.name = name;
  }
}
