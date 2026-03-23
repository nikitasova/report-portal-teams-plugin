package io.github.nikitasova.reportportal.extension.teams.model.enums.template;

import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import lombok.Getter;

@Getter
public enum StatisticTemplateProperty implements TemplateProperty {

  EXECUTIONS_TOTAL("statistics$executions$total"),
  EXECUTIONS_PASSED("statistics$executions$passed"),
  EXECUTIONS_FAILED("statistics$executions$failed"),
  EXECUTIONS_SKIPPED("statistics$executions$skipped"),
  DEFECTS_PRODUCT_BUG("statistics$defects$product_bug$total"),
  DEFECTS_AUTOMATION_BUG("statistics$defects$automation_bug$total"),
  DEFECTS_SYSTEM_ISSUE("statistics$defects$system_issue$total"),
  DEFECTS_NO_DEFECT("statistics$defects$no_defect$total"),
  DEFECTS_TO_INVESTIGATE("statistics$defects$to_investigate$total");

  private final String name;

  StatisticTemplateProperty(String name) {
    this.name = name;
  }
}
