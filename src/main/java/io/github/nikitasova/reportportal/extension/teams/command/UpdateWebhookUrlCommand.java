package io.github.nikitasova.reportportal.extension.teams.command;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.project.email.SenderCaseOptions;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class UpdateWebhookUrlCommand implements CommonPluginCommand<Map<String, Object>> {

  private static final String COMMAND_NAME = "updateWebhookUrl";
  private static final String NOTIFICATION_TYPE = "teams";
  private static final String PARAM_RULE_NAME = "ruleName";
  private static final String PARAM_WEBHOOK_URL = "webhookURL";
  private static final String PARAM_PROJECT_ID = "projectId";

  private final SenderCaseRepository senderCaseRepository;

  public UpdateWebhookUrlCommand(SenderCaseRepository senderCaseRepository) {
    this.senderCaseRepository = senderCaseRepository;
  }

  @Override
  public String getName() {
    return COMMAND_NAME;
  }

  @Override
  @Transactional
  public Map<String, Object> executeCommand(Map<String, Object> params) {
    String ruleName = extractRequired(params, PARAM_RULE_NAME);
    String webhookUrl = extractRequired(params, PARAM_WEBHOOK_URL);
    Long projectId = extractProjectId(params);

    SenderCase senderCase = senderCaseRepository
        .findByProjectIdAndTypeAndRuleNameIgnoreCase(projectId, NOTIFICATION_TYPE, ruleName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Teams notification rule '" + ruleName + "' not found in project " + projectId));

    SenderCaseOptions ruleDetails = senderCase.getRuleDetails();
    if (ruleDetails == null) {
      ruleDetails = new SenderCaseOptions(new HashMap<>());
    }
    Map<String, Object> options = ruleDetails.getOptions();
    if (options == null) {
      options = new HashMap<>();
      ruleDetails.setOptions(options);
    }

    String previousUrl = options.containsKey(PARAM_WEBHOOK_URL)
        ? options.get(PARAM_WEBHOOK_URL).toString() : "(not set)";
    options.put(PARAM_WEBHOOK_URL, webhookUrl);
    senderCase.setRuleDetails(ruleDetails);
    senderCaseRepository.save(senderCase);

    log.info("Updated webhook URL for rule '{}' (project {}): {} chars -> {} chars",
        ruleName, projectId, previousUrl.length(), webhookUrl.length());

    Map<String, Object> result = new HashMap<>();
    result.put("message", "Webhook URL updated successfully for rule '" + ruleName + "'");
    result.put("ruleName", ruleName);
    result.put("urlLength", webhookUrl.length());
    result.put("previousUrlLength", previousUrl.length());
    return result;
  }

  private String extractRequired(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value == null || value.toString().isBlank()) {
      throw new IllegalArgumentException("Required parameter '" + key + "' is missing or blank");
    }
    return value.toString();
  }

  private Long extractProjectId(Map<String, Object> params) {
    Object value = params.get(PARAM_PROJECT_ID);
    if (value == null) {
      throw new IllegalArgumentException("projectId not available in command context");
    }
    if (value instanceof Long) {
      return (Long) value;
    }
    return Long.parseLong(value.toString());
  }
}
