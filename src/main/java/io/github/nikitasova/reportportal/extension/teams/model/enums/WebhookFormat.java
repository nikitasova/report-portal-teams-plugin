package io.github.nikitasova.reportportal.extension.teams.model.enums;

public enum WebhookFormat {

  ADAPTIVE_CARD,
  CONNECTOR_CARD;

  public static WebhookFormat fromWebhookUrl(String url) {
    if (url == null) {
      return ADAPTIVE_CARD;
    }
    if (url.contains("webhook.office.com") || url.contains("outlook.office.com")) {
      return CONNECTOR_CARD;
    }
    return ADAPTIVE_CARD;
  }
}
