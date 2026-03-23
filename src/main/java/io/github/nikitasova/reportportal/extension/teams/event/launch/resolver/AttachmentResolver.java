package io.github.nikitasova.reportportal.extension.teams.event.launch.resolver;

import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.binary.MessageTemplateStore;
import io.github.nikitasova.reportportal.extension.teams.collector.PropertyCollector;
import io.github.nikitasova.reportportal.extension.teams.factory.PropertyCollectorFactory;
import io.github.nikitasova.reportportal.extension.teams.model.enums.TeamsEventType;
import io.github.nikitasova.reportportal.extension.teams.model.enums.WebhookFormat;
import io.github.nikitasova.reportportal.extension.teams.model.enums.template.DefaultTemplateProperty;
import io.github.nikitasova.reportportal.extension.teams.model.template.TemplateProperty;
import io.github.nikitasova.reportportal.extension.teams.model.template.TextProperty;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttachmentResolver {

  private final MessageTemplateStore messageTemplateStore;
  private final PropertyCollectorFactory propertyCollectorFactory;

  public AttachmentResolver(MessageTemplateStore messageTemplateStore,
      PropertyCollectorFactory propertyCollectorFactory) {
    this.messageTemplateStore = messageTemplateStore;
    this.propertyCollectorFactory = propertyCollectorFactory;
  }

  public Optional<String> resolve(Launch launch, String launchLink, WebhookFormat format,
      String projectName) {
    return messageTemplateStore.get(TeamsEventType.LAUNCH_FINISHED, format)
        .flatMap(this::readTemplate)
        .map(template -> mapLaunchPropertiesToTemplate(launch, template, launchLink, projectName));
  }

  private Optional<String> readTemplate(File file) {
    try {
      return Optional.of(Files.readString(file.toPath()));
    } catch (IOException e) {
      log.error("Failed to read template: {}", file, e);
      return Optional.empty();
    }
  }

  private String mapLaunchPropertiesToTemplate(Launch launch, String template,
      String launchLink, String projectName) {
    Map<TemplateProperty, Object> allProperties = new HashMap<>();

    for (PropertyCollector<Launch> collector :
        propertyCollectorFactory.getDefaultPropertyCollectors()) {
      allProperties.putAll(collector.collect(launch));
    }

    if (launchLink != null && !launchLink.isBlank()) {
      allProperties.put(new TextProperty(DefaultTemplateProperty.LAUNCH_LINK.getName()), launchLink);
    }
    if (projectName != null && !projectName.isBlank()) {
      allProperties.put(new TextProperty(DefaultTemplateProperty.PROJECT_NAME.getName()), projectName);
    }

    String result = template;
    for (Map.Entry<TemplateProperty, Object> entry : allProperties.entrySet()) {
      String placeholder = "${" + entry.getKey().getName() + "}";
      String raw = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
      result = result.replace(placeholder, escapeJson(raw));
    }

    if (launchLink == null || launchLink.isBlank()) {
      result = removeActionsBlock(result);
    }

    warnUnreplacedPlaceholders(result);

    return result;
  }

  private static String escapeJson(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    StringBuilder sb = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.toString();
  }

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

  private void warnUnreplacedPlaceholders(String json) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(json);
    while (matcher.find()) {
      log.warn("Unreplaced template placeholder found: ${{}}", matcher.group(1));
    }
  }

  private String removeActionsBlock(String json) {
    int actionsIdx = json.indexOf("\"actions\"");
    if (actionsIdx < 0) {
      actionsIdx = json.indexOf("\"potentialAction\"");
    }
    if (actionsIdx < 0) {
      return json;
    }
    int commaBeforeActions = json.lastIndexOf(',', actionsIdx);
    int closingBracket = json.indexOf(']', actionsIdx);
    if (commaBeforeActions >= 0 && closingBracket >= 0) {
      return json.substring(0, commaBeforeActions) + json.substring(closingBracket + 1);
    }
    return json;
  }
}
