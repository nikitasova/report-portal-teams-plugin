package io.github.nikitasova.reportportal.extension.teams;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import io.github.nikitasova.reportportal.extension.teams.binary.MessageTemplateStore;
import io.github.nikitasova.reportportal.extension.teams.command.UpdateWebhookUrlCommand;
import io.github.nikitasova.reportportal.extension.teams.event.launch.TeamsLaunchFinishEventListener;
import io.github.nikitasova.reportportal.extension.teams.event.launch.resolver.AttachmentResolver;
import io.github.nikitasova.reportportal.extension.teams.event.launch.resolver.SenderCaseMatcher;
import io.github.nikitasova.reportportal.extension.teams.event.plugin.PluginLoadedEventHandler;
import io.github.nikitasova.reportportal.extension.teams.factory.PropertyCollectorFactory;
import io.github.nikitasova.reportportal.extension.teams.info.PluginInfoProvider;
import io.github.nikitasova.reportportal.extension.teams.info.impl.PluginInfoProviderImpl;
import io.github.nikitasova.reportportal.extension.teams.utils.MemoizingSupplier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Extension
public class TeamsPluginExtension implements ReportPortalExtensionPoint, DisposableBean {

  private static final String PLUGIN_ID = "teams";
  private static final String PLUGIN_NAME = "Microsoft Teams";
  private static final String PLUGIN_PROVIDER = "nikitasova";
  private static final String BINARY_DATA_PROPERTIES_FILE_ID = "binary-data.properties";
  private static final String DOCUMENTATION_LINK =
      "https://github.com/nikitasova/report-portal-teams-plugin";
  private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";
  private static final String NAME_FIELD = "name";
  private static final String SCRIPTS_DIR = "scripts";

  private final String resourcesDir;

  private final Supplier<Map<String, PluginCommand>> pluginCommandMapping =
      new MemoizingSupplier<>(this::getCommands);
  private final Supplier<Map<String, CommonPluginCommand<?>>> commonPluginCommandMapping =
      new MemoizingSupplier<>(this::getCommonCommands);

  private final PluginInfoProvider pluginInfoProvider;
  private final Supplier<ApplicationListener<?>> pluginLoadedListener;
  private final Supplier<ApplicationListener<?>> launchFinishedListener;

  @Autowired
  private com.epam.ta.reportportal.dao.IntegrationTypeRepository integrationTypeRepository;
  @Autowired
  private com.epam.ta.reportportal.dao.LaunchRepository launchRepository;
  @Autowired
  private com.epam.ta.reportportal.dao.ProjectRepository projectRepository;
  @Autowired
  private com.epam.ta.reportportal.dao.SenderCaseRepository senderCaseRepository;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private DataSource dataSource;

  private final RestTemplate restTemplate = new RestTemplate();

  public TeamsPluginExtension(Map<String, Object> initParams) {
    resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams)
        .map(String::valueOf)
        .orElse("");

    pluginInfoProvider = new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID);

    pluginLoadedListener = new MemoizingSupplier<>(
        () -> new PluginLoadedEventHandler(PLUGIN_ID, integrationTypeRepository,
            pluginInfoProvider));

    Supplier<SenderCaseMatcher> senderCaseMatcher = new MemoizingSupplier<>(SenderCaseMatcher::new);
    Supplier<MessageTemplateStore> templateStore =
        new MemoizingSupplier<>(() -> new MessageTemplateStore(resourcesDir));
    Supplier<AttachmentResolver> attachmentResolver = new MemoizingSupplier<>(
        () -> new AttachmentResolver(
            templateStore.get(),
            new PropertyCollectorFactory()));

    launchFinishedListener = new MemoizingSupplier<>(
        () -> new TeamsLaunchFinishEventListener(
            projectRepository, launchRepository,
            senderCaseMatcher.get(), attachmentResolver.get(), restTemplate));
  }

  @PostConstruct
  public void createIntegration() throws IOException {
    initListeners();
    initScripts();
    scheduleIntegrationTypeInit();
  }

  private void scheduleIntegrationTypeInit() {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "teams-plugin-init");
      t.setDaemon(true);
      return t;
    });
    scheduler.schedule(() -> {
      try {
        configureIntegrationType();
      } finally {
        scheduler.shutdown();
      }
    }, 5, TimeUnit.SECONDS);
  }

  private void configureIntegrationType() {
    try {
      integrationTypeRepository.findByName(PLUGIN_ID).ifPresentOrElse(
          type -> {
            pluginInfoProvider.provide(type);
            integrationTypeRepository.save(type);
            log.info("Teams plugin integration type configured: group={}",
                type.getIntegrationGroup());
          },
          () -> log.warn("Integration type '{}' still not found in DB", PLUGIN_ID)
      );
    } catch (Exception e) {
      log.error("Failed to configure Teams integration type", e);
    }
  }

  private void initListeners() {
    ApplicationEventMulticaster multicaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class);
    multicaster.addApplicationListener(pluginLoadedListener.get());
    multicaster.addApplicationListener(launchFinishedListener.get());
  }

  private void initScripts() throws IOException {
    if (!Files.exists(Paths.get(resourcesDir, SCRIPTS_DIR))) {
      return;
    }
    try (Stream<java.nio.file.Path> paths = Files.list(Paths.get(resourcesDir, SCRIPTS_DIR))) {
      FileSystemResource[] scripts = paths.sorted()
          .map(FileSystemResource::new)
          .toArray(FileSystemResource[]::new);
      ResourceDatabasePopulator populator = new ResourceDatabasePopulator(scripts);
      populator.execute(dataSource);
    }
  }

  @Override
  public void destroy() {
    ApplicationEventMulticaster multicaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class);
    multicaster.removeApplicationListener(pluginLoadedListener.get());
    multicaster.removeApplicationListener(launchFinishedListener.get());
  }

  @Override
  public Map<String, ?> getPluginParams() {
    Map<String, Object> params = new HashMap<>();
    params.put(NAME_FIELD, PLUGIN_NAME);
    params.put(ALLOWED_COMMANDS, new ArrayList<>(pluginCommandMapping.get().keySet()));
    params.put(DOCUMENTATION_LINK_FIELD, DOCUMENTATION_LINK);
    params.put(COMMON_COMMANDS, new ArrayList<>(commonPluginCommandMapping.get().keySet()));
    params.put("developer", Map.of("name", PLUGIN_PROVIDER));
    return params;
  }

  @Override
  public IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.NOTIFICATION;
  }

  @Override
  public CommonPluginCommand<?> getCommonCommand(String commandName) {
    return commonPluginCommandMapping.get().get(commandName);
  }

  @Override
  public PluginCommand getIntegrationCommand(String commandName) {
    return pluginCommandMapping.get().get(commandName);
  }

  private Map<String, PluginCommand> getCommands() {
    return new HashMap<>();
  }

  private Map<String, CommonPluginCommand<?>> getCommonCommands() {
    Map<String, CommonPluginCommand<?>> commands = new HashMap<>();
    UpdateWebhookUrlCommand updateWebhookUrlCommand = new UpdateWebhookUrlCommand(
        senderCaseRepository);
    commands.put(updateWebhookUrlCommand.getName(), updateWebhookUrlCommand);
    return commands;
  }
}
