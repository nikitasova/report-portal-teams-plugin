package io.github.nikitasova.reportportal.extension.teams.event.launch.resolver;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import io.github.nikitasova.reportportal.extension.teams.utils.NotificationConfigConverter;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SenderCaseMatcher {

  private final NotificationConfigConverter attributeConverter = new NotificationConfigConverter();

  public boolean isSenderCaseMatched(SenderCase senderCase, Launch launch) {
    if (LaunchModeEnum.DEBUG.equals(launch.getMode())) {
      return false;
    }
    return isSendCaseConditionMatched(senderCase.getSendCase(), launch)
        && isLaunchNameMatched(senderCase, launch)
        && isAttributeMatched(senderCase, launch);
  }

  private boolean isSendCaseConditionMatched(SendCase sendCase, Launch launch) {
    if (sendCase == null) {
      return false;
    }
    return switch (sendCase) {
      case ALWAYS -> true;
      case FAILED -> StatusEnum.FAILED.equals(launch.getStatus());
      case TO_INVESTIGATE -> hasToInvestigate(launch);
      case MORE_10 -> getFailurePercentage(launch) > 10;
      case MORE_20 -> getFailurePercentage(launch) > 20;
      case MORE_50 -> getFailurePercentage(launch) > 50;
    };
  }

  private boolean isLaunchNameMatched(SenderCase senderCase, Launch launch) {
    Set<String> launchNames = senderCase.getLaunchNames();
    if (launchNames == null || launchNames.isEmpty()) {
      return true;
    }
    return launchNames.contains(launch.getName());
  }

  private boolean isAttributeMatched(SenderCase senderCase, Launch launch) {
    Set<LaunchAttributeRule> attributeRules = senderCase.getLaunchAttributeRules();
    if (attributeRules == null || attributeRules.isEmpty()) {
      return true;
    }

    Set<ItemAttributeResource> ruleAttributes = attributeRules.stream()
        .map(attributeConverter)
        .collect(Collectors.toSet());

    Set<ItemAttributeResource> launchAttributes = Optional.ofNullable(launch.getAttributes())
        .stream()
        .flatMap(Collection::stream)
        .filter(attr -> !attr.isSystem())
        .map(this::toResource)
        .collect(Collectors.toSet());

    if (LogicalOperator.AND.equals(senderCase.getAttributesOperator())) {
      return launchAttributes.containsAll(ruleAttributes);
    } else {
      return ruleAttributes.stream().anyMatch(launchAttributes::contains);
    }
  }

  private ItemAttributeResource toResource(ItemAttribute attribute) {
    ItemAttributeResource resource = new ItemAttributeResource();
    resource.setKey(attribute.getKey());
    resource.setValue(attribute.getValue());
    return resource;
  }

  private boolean hasToInvestigate(Launch launch) {
    return getStatValue(launch, "statistics$defects$to_investigate$total") > 0;
  }

  private double getFailurePercentage(Launch launch) {
    int total = getStatValue(launch, "statistics$executions$total");
    int failed = getStatValue(launch, "statistics$executions$failed");
    if (total == 0) {
      return 0;
    }
    return (double) failed / total * 100;
  }

  private int getStatValue(Launch launch, String fieldName) {
    Set<Statistics> stats = launch.getStatistics();
    if (stats == null) {
      return 0;
    }
    return stats.stream()
        .filter(s -> fieldName.equals(s.getStatisticsField().getName()))
        .map(Statistics::getCounter)
        .findFirst()
        .orElse(0);
  }
}
