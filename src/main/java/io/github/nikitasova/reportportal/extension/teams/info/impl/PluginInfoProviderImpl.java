package io.github.nikitasova.reportportal.extension.teams.info.impl;

import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import io.github.nikitasova.reportportal.extension.teams.info.PluginInfoProvider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginInfoProviderImpl implements PluginInfoProvider {

  private static final String BINARY_DATA_KEY = "binaryData";
  private static final String DESCRIPTION_KEY = "description";
  private static final String FIELDS_KEY = "ruleFields";
  private static final String RULE_DESCRIPTION_KEY = "ruleDescription";
  private static final String METADATA_KEY = "metadata";

  private static final String PLUGIN_DESCRIPTION =
      "Integrate ReportPortal with Microsoft Teams. "
          + "Receive launch finish notifications in your Teams channel via Power Automate webhooks.";

  private static final String RULE_DESCRIPTION =
      "Provide a Microsoft Teams webhook URL for every rule to send launch notifications";

  private static final Map<String, Object> PLUGIN_METADATA = new HashMap<>();

  static {
    PLUGIN_METADATA.put("isIntegrationsAllowed", false);
  }

  private final String resourcesDir;
  private final String propertyFile;

  public PluginInfoProviderImpl(String resourcesDir, String propertyFile) {
    this.resourcesDir = resourcesDir;
    this.propertyFile = propertyFile;
  }

  @Override
  public IntegrationType provide(IntegrationType integrationType) {
    integrationType.setIntegrationGroup(IntegrationGroupEnum.NOTIFICATION);
    loadBinaryDataInfo(integrationType);
    updateDescription(integrationType);
    updateMetadata(integrationType);
    updateDeveloper(integrationType);
    addFieldsInfo(integrationType);
    addRuleDescriptionInfo(integrationType);
    return integrationType;
  }

  private void loadBinaryDataInfo(IntegrationType integrationType) {
    Map<String, Object> details = getDetails(integrationType);
    try (InputStream stream = Files.newInputStream(
        Paths.get(resourcesDir, propertyFile))) {
      Properties props = new Properties();
      props.load(stream);
      Map<String, String> binaryData = new HashMap<>();
      props.forEach((k, v) -> binaryData.put(String.valueOf(k), String.valueOf(v)));
      details.put(BINARY_DATA_KEY, binaryData);
    } catch (IOException e) {
      log.error("Failed to load binary data properties", e);
    }
  }

  private void updateDescription(IntegrationType integrationType) {
    getDetails(integrationType).put(DESCRIPTION_KEY, PLUGIN_DESCRIPTION);
  }

  private void updateMetadata(IntegrationType integrationType) {
    getDetails(integrationType).put(METADATA_KEY, PLUGIN_METADATA);
  }

  private void updateDeveloper(IntegrationType integrationType) {
    getDetails(integrationType).put("developer", Map.of("name", "nikitasova"));
  }

  private void addRuleDescriptionInfo(IntegrationType integrationType) {
    getDetails(integrationType).put(RULE_DESCRIPTION_KEY, RULE_DESCRIPTION);
  }

  private void addFieldsInfo(IntegrationType integrationType) {
    Map<String, Object> ruleField = new HashMap<>();
    ruleField.put("name", "webhookURL");
    ruleField.put("label", "Webhook URL");
    ruleField.put("type", "text");
    ruleField.put("placeholder", "https://...");
    ruleField.put("required", true);
    ruleField.put("maxLength", 1024);

    Map<String, Object> validation = new HashMap<>();
    validation.put("type", "url");
    validation.put("maxLength", 1024);
    validation.put("errorMessage", "Please provide a valid webhook URL");
    ruleField.put("validation", validation);

    getDetails(integrationType).put(FIELDS_KEY, List.of(ruleField));
  }

  private Map<String, Object> getDetails(IntegrationType integrationType) {
    if (integrationType.getDetails() == null) {
      IntegrationTypeDetails details = new IntegrationTypeDetails();
      details.setDetails(new HashMap<>());
      integrationType.setDetails(details);
    }
    if (integrationType.getDetails().getDetails() == null) {
      integrationType.getDetails().setDetails(new HashMap<>());
    }
    return integrationType.getDetails().getDetails();
  }
}
