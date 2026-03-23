<p align="center">
  <img src="src/main/resources/public/icon.png" alt="ReportPortal Teams Plugin" width="100">
</p>

<h1 align="center">ReportPortal Teams Plugin</h1>

<p align="center">
  <a href="https://github.com/nikitasova/report-portal-teams-plugin/actions/workflows/release.yml">
    <img src="https://github.com/nikitasova/report-portal-teams-plugin/actions/workflows/release.yml/badge.svg" alt="Release">
  </a>
</p>

<p align="center">
  <strong>Microsoft Teams notifications for your ReportPortal launches</strong>
</p>

<p align="center">
  <a href="https://github.com/nikitasova/report-portal-teams-plugin/blob/main/LICENSE"><img src="https://img.shields.io/github/license/nikitasova/report-portal-teams-plugin?style=flat-square" alt="License"></a>
  <img src="https://img.shields.io/badge/Java-21+-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21+">
  <img src="https://img.shields.io/badge/ReportPortal-24.x-00C853?style=flat-square" alt="ReportPortal 24.x">
  <img src="https://img.shields.io/badge/Teams-Webhooks%20%26%20Power%20Automate-6264A7?style=flat-square&logo=microsoftteams&logoColor=white" alt="Teams">
</p>

<p align="center">
  <a href="#quick-start">Quick Start</a> &bull;
  <a href="#features">Features</a> &bull;
  <a href="#template-variables">Template Variables</a> &bull;
  <a href="#troubleshooting">Troubleshooting</a> &bull;
  <a href="#contributing">Contributing</a>
</p>

---

> ⚠️ BETA VERSION as POC

Drop-in alternative to the official [plugin-slack](https://github.com/reportportal/plugin-slack) for Teams users.  
Works with both **default Teams webhooks** and **Power Automate Workflow** webhooks.  
Single JAR &mdash; install, configure a webhook, done.

## Features

| Feature | Description |
|---------|-------------|
| **Adaptive Card Notifications** | Rich, interactive cards posted when a launch finishes |
| **Execution Statistics** | Total, passed, failed, skipped &mdash; at a glance |
| **Defect Breakdown** | Product bug, automation bug, system issue, to investigate |
| **Direct Launch Link** | One-click navigation to the launch in ReportPortal |
| **Flexible Rules** | Always, Failed, To Investigate, More than 10/20/50% |
| **Filtering** | Filter by launch name and attributes |
| **Customizable Template** | Edit `finish-launch.json` to tailor the card layout |
| **Zero Dependencies** | Single JAR deployment &mdash; no external runtime deps |

## Quick Start

### 1. Build the Plugin

```bash
./gradlew shadowJar
```

The plugin JAR will be at `build/libs/plugin-teams-1.0.0.jar`.

### 2. Install in ReportPortal

1. Go to **Admin Panel** > **Plugins** > **Upload Plugin**
2. Upload the `plugin-teams-1.0.0.jar` file
3. The plugin appears as "Microsoft Teams" in the plugins list

## Template Variables

The `finish-launch.json` template supports these placeholder variables:

| Variable | Description | Example |
|---|---|---|
| `${LAUNCH_NAME}` | Launch name | `Regression Suite` |
| `${LAUNCH_NUMBER}` | Launch number | `107` |
| `${LAUNCH_ID}` | Launch database ID | `42` |
| `${LAUNCH_UUID}` | Launch UUID | `abc-def-123` |
| `${LAUNCH_DESCRIPTION}` | Launch description | `Pipeline #258875` |
| `${LAUNCH_START_TIME}` | Start timestamp | `2025-01-15 10:00:00` |
| `${LAUNCH_FINISH_TIME}` | Finish timestamp | `2025-01-15 10:05:30` |
| `${LAUNCH_ATTRIBUTES}` | Non-system attributes | `BRANCH:main, ENV:staging` |
| `${RESULT_COLOR}` | Hex color by status | `#008000` (green) |
| `${LAUNCH_LINK}` | Link to launch in RP | `https://rp.example.com/...` |
| `${PROJECT_NAME}` | Project name | `my-project` |
| `${statistics$executions$total}` | Total test count | `131` |
| `${statistics$executions$passed}` | Passed test count | `130` |
| `${statistics$executions$failed}` | Failed test count | `1` |
| `${statistics$executions$skipped}` | Skipped test count | `0` |
| `${statistics$defects$product_bug$total}` | Product bugs | `0` |
| `${statistics$defects$automation_bug$total}` | Automation bugs | `1` |
| `${statistics$defects$system_issue$total}` | System issues | `0` |
| `${statistics$defects$no_defect$total}` | No defect count | `0` |
| `${statistics$defects$to_investigate$total}` | To investigate count | `0` |

## Customizing the Template

Edit `src/main/resources/message-template/finish-launch.json` to customize the Adaptive Card layout. The template uses [Microsoft Adaptive Cards](https://adaptivecards.io/) format, which you can preview at [adaptivecards.io/designer](https://adaptivecards.io/designer/).

## Project Structure

```
src/main/java/io/github/nikitasova/reportportal/extension/teams/
├── TeamsPlugin.java                           # PF4J plugin entry point
├── TeamsPluginExtension.java                  # Spring @Extension, wires all dependencies
├── binary/
│   ├── JsonObjectLoader.java                  # Loads JSON template files
│   └── MessageTemplateStore.java              # Maps event types to template paths
├── command/
│   └── UpdateWebhookUrlCommand.java           # API command to set full webhook URL
├── collector/
│   ├── PropertyCollector.java                 # Interface for property collection
│   └── launch/
│       ├── AttributesCollector.java           # Collects launch attributes
│       ├── LaunchPropertiesCollector.java      # Collects launch metadata
│       ├── ResultColorCollector.java           # Maps status to color
│       └── StatisticsPropertiesCollector.java  # Collects test statistics
├── event/
│   ├── launch/
│   │   ├── TeamsLaunchFinishEventListener.java # Handles launch finish events
│   │   └── resolver/
│   │       ├── AttachmentResolver.java         # Resolves template placeholders
│   │       └── SenderCaseMatcher.java          # Matches notification rules
│   └── plugin/
│       └── PluginLoadedEventHandler.java       # Handles plugin lifecycle
├── factory/
│   └── PropertyCollectorFactory.java           # Creates property collectors
├── info/
│   ├── PluginInfoProvider.java
│   └── impl/
│       └── PluginInfoProviderImpl.java
├── model/
│   ├── enums/
│   │   ├── TeamsEventType.java
│   │   ├── TeamsIntegrationProperties.java
│   │   └── template/
│   │       ├── Color.java
│   │       ├── DefaultTemplateProperty.java
│   │       └── StatisticTemplateProperty.java
│   └── template/
│       ├── TemplateProperty.java
│       └── TextProperty.java
└── utils/
    ├── MemoizingSupplier.java
    └── NotificationConfigConverter.java
```

## Compatibility

| Component | Version |
|-----------|---------|
| **ReportPortal** | 24.x (5.12+) |
| **Java** | 21+ |
| **Teams** | Default webhooks & Power Automate Workflow webhooks (Adaptive Card v1.4) |

## Troubleshooting

### Verify webhook connectivity

Test your webhook URL directly with `curl` to rule out plugin issues:

**Connector webhook** (`webhook.office.com` / `outlook.office.com`):

```bash
curl -H "Content-Type: application/json" -d '{
  "@type": "MessageCard",
  "@context": "https://schema.org/extensions",
  "summary": "Test",
  "title": "ReportPortal Teams Plugin — Test Message",
  "sections": [{"activityTitle": "If you see this, the webhook works."}]
}' "YOUR_FULL_WEBHOOK_URL"
```

**Power Automate Workflow webhook** (`powerplatform.com`):

```bash
curl -H "Content-Type: application/json" -d '{
  "type": "message",
  "attachments": [{
    "contentType": "application/vnd.microsoft.card.adaptive",
    "content": {
      "type": "AdaptiveCard",
      "version": "1.4",
      "body": [{"type": "TextBlock", "text": "ReportPortal Teams Plugin — Test Message"}]
    }
  }]
}' "YOUR_FULL_WORKFLOW_URL"
```

### Common errors

| Error | Cause | Fix |
|-------|-------|-----|
| HTTP 200 + `403` in body | Webhook URL truncated (check `url length` in logs) | Use `updateWebhookUrl` command to set the full URL ([see below](#webhook-url-truncated-to-256-characters)) |
| HTTP 401 Unauthorized | Workflow URL truncated or `sig` expired | Use `updateWebhookUrl` command; regenerate URL in Power Automate if needed |
| `url length=256` in logs | RP UI truncated the URL | Use `updateWebhookUrl` command ([see below](#webhook-url-truncated-to-256-characters)) |

### Webhook URL truncated to 256 characters

**Problem:** The ReportPortal UI truncates webhook URLs to **256 characters**. This breaks long Power Automate Workflow URLs and some Connector URLs whose authentication signatures get cut off. The plugin declares `maxLength: 1024` in its field metadata, but the RP frontend ([`addEditNotificationModal.jsx`](https://github.com/reportportal/service-ui/blob/develop/app/src/pages/inside/projectSettingsPageContainer/content/notifications/modals/addEditNotificationModal/addEditNotificationModal.jsx)) does not forward `maxLength` from plugin `ruleFields` to the input component. This is an upstream issue in `reportportal/service-ui`.

**Fix:** This plugin ships with an `updateWebhookUrl` API command that writes the **full, untruncated URL** directly to the database, bypassing the UI limit.

1. **Create the notification rule** in the RP UI as usual (the URL will be truncated, but that's OK)
2. **Call the plugin command** to set the full URL:

```bash
curl -X PUT \
  "https://<rp-host>/api/v1/plugin/<project-key>/teams/common/updateWebhookUrl" \
  -H "Authorization: Bearer <your-api-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "Your Rule Name",
    "webhookURL": "https://your-full-webhook-url-here..."
  }'
```

Replace:
- `<rp-host>` &mdash; your ReportPortal host
- `<project-key>` &mdash; the project key (visible in the URL when you open the project)
- `<your-api-token>` &mdash; your RP API token (Profile > API Keys)
- `Your Rule Name` &mdash; the exact name of the notification rule you created in the UI
- The full webhook URL (any length)

The response confirms the update:

```json
{
  "message": "Webhook URL updated successfully for rule 'Your Rule Name'",
  "ruleName": "Your Rule Name",
  "urlLength": 310,
  "previousUrlLength": 256
}
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feat/my-feature`)
3. Commit your changes
4. Push to the branch (`git push origin feat/my-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 &mdash; see [LICENSE](LICENSE) for details.
