package io.github.nikitasova.reportportal.extension.teams.event.launch.resolver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SenderCaseMatcherTest {

  private SenderCaseMatcher matcher;

  @BeforeEach
  void setUp() {
    matcher = new SenderCaseMatcher();
  }

  @Test
  void shouldMatchAlwaysCase() {
    SenderCase senderCase = createSenderCase(SendCase.ALWAYS);
    Launch launch = createLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT);

    assertTrue(matcher.isSenderCaseMatched(senderCase, launch));
  }

  @Test
  void shouldMatchFailedCaseWhenLaunchFailed() {
    SenderCase senderCase = createSenderCase(SendCase.FAILED);
    Launch launch = createLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT);

    assertTrue(matcher.isSenderCaseMatched(senderCase, launch));
  }

  @Test
  void shouldNotMatchFailedCaseWhenLaunchPassed() {
    SenderCase senderCase = createSenderCase(SendCase.FAILED);
    Launch launch = createLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT);

    assertFalse(matcher.isSenderCaseMatched(senderCase, launch));
  }

  @Test
  void shouldNotMatchDebugModeLaunch() {
    SenderCase senderCase = createSenderCase(SendCase.ALWAYS);
    Launch launch = createLaunch(StatusEnum.PASSED, LaunchModeEnum.DEBUG);

    assertFalse(matcher.isSenderCaseMatched(senderCase, launch));
  }

  @Test
  void shouldMatchWhenLaunchNameInFilter() {
    SenderCase senderCase = createSenderCase(SendCase.ALWAYS);
    senderCase.setLaunchNames(Set.of("Regression", "Smoke"));

    Launch launch = createLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT);
    launch.setName("Regression");

    assertTrue(matcher.isSenderCaseMatched(senderCase, launch));
  }

  @Test
  void shouldNotMatchWhenLaunchNameNotInFilter() {
    SenderCase senderCase = createSenderCase(SendCase.ALWAYS);
    senderCase.setLaunchNames(Set.of("Regression", "Smoke"));

    Launch launch = createLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT);
    launch.setName("Full Suite");

    assertFalse(matcher.isSenderCaseMatched(senderCase, launch));
  }

  private SenderCase createSenderCase(SendCase sendCase) {
    SenderCase sc = new SenderCase();
    sc.setSendCase(sendCase);
    sc.setEnabled(true);
    sc.setType("teams");
    return sc;
  }

  private Launch createLaunch(StatusEnum status, LaunchModeEnum mode) {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setName("Test Launch");
    launch.setNumber(1L);
    launch.setStatus(status);
    launch.setMode(mode);
    launch.setAttributes(Collections.emptySet());
    launch.setStatistics(Collections.emptySet());
    return launch;
  }
}
