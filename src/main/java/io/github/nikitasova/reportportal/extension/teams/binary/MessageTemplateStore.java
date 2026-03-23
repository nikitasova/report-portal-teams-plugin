package io.github.nikitasova.reportportal.extension.teams.binary;

import io.github.nikitasova.reportportal.extension.teams.model.enums.TeamsEventType;
import io.github.nikitasova.reportportal.extension.teams.model.enums.WebhookFormat;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public class MessageTemplateStore {

  private static final String TEMPLATE_DIR = "message-template";

  private final Map<TeamsEventType, File> adaptiveCardTemplates;
  private final Map<TeamsEventType, File> connectorCardTemplates;

  public MessageTemplateStore(String resourcesDir) {
    this.adaptiveCardTemplates = Map.of(
        TeamsEventType.LAUNCH_FINISHED,
        Paths.get(resourcesDir, TEMPLATE_DIR, "finish-launch.json").toFile()
    );
    this.connectorCardTemplates = Map.of(
        TeamsEventType.LAUNCH_FINISHED,
        Paths.get(resourcesDir, TEMPLATE_DIR, "finish-launch-connector.json").toFile()
    );
  }

  public Optional<File> get(TeamsEventType eventType, WebhookFormat format) {
    Map<TeamsEventType, File> templates =
        format == WebhookFormat.CONNECTOR_CARD ? connectorCardTemplates : adaptiveCardTemplates;
    return Optional.ofNullable(templates.get(eventType));
  }
}
