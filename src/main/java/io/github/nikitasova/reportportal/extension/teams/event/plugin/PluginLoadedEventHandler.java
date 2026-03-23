package io.github.nikitasova.reportportal.extension.teams.event.plugin;

import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import io.github.nikitasova.reportportal.extension.teams.info.PluginInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@Slf4j
public class PluginLoadedEventHandler implements ApplicationListener<ApplicationEvent> {

  private final String pluginId;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final PluginInfoProvider pluginInfoProvider;

  public PluginLoadedEventHandler(String pluginId,
      IntegrationTypeRepository integrationTypeRepository,
      PluginInfoProvider pluginInfoProvider) {
    this.pluginId = pluginId;
    this.integrationTypeRepository = integrationTypeRepository;
    this.pluginInfoProvider = pluginInfoProvider;
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    String eventClassName = event.getClass().getSimpleName();

    if ("PluginUploadedEvent".equals(eventClassName)) {
      handlePluginUploaded(event);
    } else if ("ContextRefreshedEvent".equals(eventClassName)) {
      handleContextRefreshed();
    }
  }

  private void handlePluginUploaded(ApplicationEvent event) {
    try {
      Object resource = event.getClass().getMethod("getPluginActivityResource").invoke(event);
      String eventPluginId = (String) resource.getClass().getMethod("getName").invoke(resource);
      log.info("PluginUploadedEvent received for plugin: '{}'", eventPluginId);
      if (pluginId.equals(eventPluginId)) {
        configureIntegrationType();
      }
    } catch (Exception e) {
      log.error("Failed to handle PluginUploadedEvent", e);
    }
  }

  private void handleContextRefreshed() {
    log.info("ContextRefreshedEvent received, configuring Teams integration type");
    configureIntegrationType();
  }

  private void configureIntegrationType() {
    try {
      integrationTypeRepository.findByName(pluginId).ifPresentOrElse(
          type -> {
            pluginInfoProvider.provide(type);
            integrationTypeRepository.save(type);
            log.info("Teams integration type configured: group={}", type.getIntegrationGroup());
          },
          () -> log.warn("Integration type '{}' not yet in DB", pluginId)
      );
    } catch (Exception e) {
      log.error("Failed to configure integration type", e);
    }
  }
}
