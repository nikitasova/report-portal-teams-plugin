package io.github.nikitasova.reportportal.extension.teams.event.launch;

import com.epam.reportportal.extension.event.LaunchFinishedPluginEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import io.github.nikitasova.reportportal.extension.teams.event.launch.resolver.AttachmentResolver;
import io.github.nikitasova.reportportal.extension.teams.event.launch.resolver.SenderCaseMatcher;
import io.github.nikitasova.reportportal.extension.teams.model.enums.WebhookFormat;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class TeamsLaunchFinishEventListener implements
    ApplicationListener<LaunchFinishedPluginEvent> {

  public static final String TEAMS_NOTIFICATION_ATTRIBUTE = "notifications.teams.enabled";
  public static final String GENERAL_NOTIFICATION_ATTRIBUTE = "notifications.enabled";
  public static final String WEBHOOK_DETAILS = "webhookURL";
  public static final String PLUGIN_NOTIFICATION_TYPE = "teams";

  private final ProjectRepository projectRepository;
  private final LaunchRepository launchRepository;
  private final SenderCaseMatcher senderCaseMatcher;
  private final AttachmentResolver attachmentResolver;
  private final RestTemplate restTemplate;

  public TeamsLaunchFinishEventListener(ProjectRepository projectRepository,
      LaunchRepository launchRepository,
      SenderCaseMatcher senderCaseMatcher,
      AttachmentResolver attachmentResolver,
      RestTemplate restTemplate) {
    this.projectRepository = projectRepository;
    this.launchRepository = launchRepository;
    this.senderCaseMatcher = senderCaseMatcher;
    this.attachmentResolver = attachmentResolver;
    this.restTemplate = restTemplate;
  }

  @Override
  public void onApplicationEvent(LaunchFinishedPluginEvent event) {
    try {
      Long launchId = event.getSource();
      Project project = getProject(event.getProjectId());
      if (!isNotificationsEnabled(project)) {
        log.debug("Teams notifications disabled for project: {}", project.getName());
        return;
      }

      Launch launch = getLaunch(launchId);
      processSenderCases(project, launch, event.getLaunchLink());
    } catch (Exception e) {
      log.error("Failed to process Teams notification for launch event", e);
    }
  }

  private void processSenderCases(Project project, Launch launch, String launchLink) {
    Set<SenderCase> senderCases = project.getSenderCases();
    if (senderCases == null || senderCases.isEmpty()) {
      return;
    }

    String projectName = project.getName();
    senderCases.stream()
        .filter(sc -> PLUGIN_NOTIFICATION_TYPE.equalsIgnoreCase(sc.getType()))
        .filter(SenderCase::isEnabled)
        .filter(sc -> senderCaseMatcher.isSenderCaseMatched(sc, launch))
        .forEach(sc -> sendNotification(sc, launch, launchLink, projectName));
  }

  private void sendNotification(SenderCase senderCase, Launch launch, String launchLink,
      String projectName) {
    String webhookUrl = getWebhookUrl(senderCase);
    if (webhookUrl == null || webhookUrl.isBlank()) {
      log.warn("No webhook URL configured for sender case: {}", senderCase.getRuleName());
      return;
    }

    WebhookFormat format = WebhookFormat.fromWebhookUrl(webhookUrl);
    log.info("Sending Teams notification for launch '{}' via rule '{}' using {} format (url length={})",
        launch.getName(), senderCase.getRuleName(), format, webhookUrl.length());

    Optional<String> attachment = attachmentResolver.resolve(launch, launchLink, format, projectName);
    attachment.ifPresent(payload -> postToWebhook(webhookUrl, payload, launch, senderCase));
  }

  private void postToWebhook(String webhookUrl, String payload, Launch launch,
      SenderCase senderCase) {
    try {
      log.debug("Payload for launch '{}' (length={}): {}", launch.getName(), payload.length(), payload);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> request = new HttpEntity<>(payload, headers);
      URI uri = URI.create(webhookUrl);

      ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
      String body = response.getBody();

      if (body != null && body.contains("error")) {
        log.error("Teams notification for launch '{}' via rule '{}' — HTTP {} but webhook "
                + "reported error: {}", launch.getName(), senderCase.getRuleName(),
            response.getStatusCode(), body);
      } else {
        log.info("Teams notification sent for launch '{}' via rule '{}' — HTTP {}",
            launch.getName(), senderCase.getRuleName(), response.getStatusCode());
      }
    } catch (Exception e) {
      log.error("Failed to send Teams notification for launch '{}' via rule '{}' "
              + "(url length={}): {}",
          launch.getName(), senderCase.getRuleName(), webhookUrl.length(), e.getMessage());
    }
  }

  private String getWebhookUrl(SenderCase senderCase) {
    if (senderCase.getRuleDetails() == null
        || senderCase.getRuleDetails().getOptions() == null) {
      return null;
    }
    Object url = senderCase.getRuleDetails().getOptions().get(WEBHOOK_DETAILS);
    return url != null ? url.toString() : null;
  }

  private boolean isNotificationsEnabled(Project project) {
    Set<ProjectAttribute> attributes = project.getProjectAttributes();
    if (attributes == null) {
      return false;
    }
    boolean generalEnabled = attributes.stream()
        .anyMatch(attr ->
            GENERAL_NOTIFICATION_ATTRIBUTE.equals(attr.getAttribute().getName())
                && "true".equalsIgnoreCase(attr.getValue()));
    boolean teamsEnabled = attributes.stream()
        .anyMatch(attr ->
            TEAMS_NOTIFICATION_ATTRIBUTE.equals(attr.getAttribute().getName())
                && "true".equalsIgnoreCase(attr.getValue()));
    return generalEnabled && teamsEnabled;
  }

  private Project getProject(Long projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new IllegalStateException("Project not found: " + projectId));
  }

  private Launch getLaunch(Long launchId) {
    return launchRepository.findById(launchId)
        .orElseThrow(() -> new IllegalStateException("Launch not found: " + launchId));
  }
}
