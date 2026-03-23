package io.github.nikitasova.reportportal.extension.teams.model.enums.template;

import lombok.Getter;

@Getter
public enum Color {

  PASSED("#008000"),
  FAILED("#FF0000"),
  INTERRUPTED("#FFA500");

  private final String hexCode;

  Color(String hexCode) {
    this.hexCode = hexCode;
  }
}
