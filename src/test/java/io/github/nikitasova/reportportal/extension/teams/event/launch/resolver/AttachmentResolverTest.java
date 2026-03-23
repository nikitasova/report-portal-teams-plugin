package io.github.nikitasova.reportportal.extension.teams.event.launch.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import io.github.nikitasova.reportportal.extension.teams.binary.MessageTemplateStore;
import io.github.nikitasova.reportportal.extension.teams.factory.PropertyCollectorFactory;
import io.github.nikitasova.reportportal.extension.teams.model.enums.TeamsEventType;
import io.github.nikitasova.reportportal.extension.teams.model.enums.WebhookFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttachmentResolverTest {

  @Mock
  private MessageTemplateStore messageTemplateStore;

  private AttachmentResolver attachmentResolver;

  private static final String TEMPLATE = "{\"text\":\"Launch: ${LAUNCH_NAME}#${LAUNCH_NUMBER}\"}";

  @TempDir
  File tempDir;

  @BeforeEach
  void setUp() {
    attachmentResolver = new AttachmentResolver(
        messageTemplateStore, new PropertyCollectorFactory());
  }

  @Test
  void shouldResolveTemplateWithLaunchProperties() throws IOException {
    File templateFile = new File(tempDir, "finish-launch.json");
    Files.writeString(templateFile.toPath(), TEMPLATE);

    when(messageTemplateStore.get(TeamsEventType.LAUNCH_FINISHED, WebhookFormat.ADAPTIVE_CARD))
        .thenReturn(Optional.of(templateFile));

    Launch launch = createLaunch();
    Optional<String> result = attachmentResolver.resolve(launch, "https://rp.example.com/launch/1",
        WebhookFormat.ADAPTIVE_CARD, "my-project");

    assertTrue(result.isPresent());
    assertTrue(result.get().contains("Test Launch"));
    assertTrue(result.get().contains("#42"));
  }

  @Test
  void shouldReturnEmptyWhenTemplateNotFound() {
    when(messageTemplateStore.get(TeamsEventType.LAUNCH_FINISHED, WebhookFormat.ADAPTIVE_CARD))
        .thenReturn(Optional.empty());

    Launch launch = createLaunch();
    Optional<String> result = attachmentResolver.resolve(launch, "https://rp.example.com/launch/1",
        WebhookFormat.ADAPTIVE_CARD, "my-project");

    assertTrue(result.isEmpty());
  }

  private Launch createLaunch() {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setUuid("test-uuid-123");
    launch.setName("Test Launch");
    launch.setNumber(42L);
    launch.setStatus(StatusEnum.PASSED);
    launch.setAttributes(Collections.emptySet());
    launch.setStatistics(Collections.emptySet());
    return launch;
  }
}
