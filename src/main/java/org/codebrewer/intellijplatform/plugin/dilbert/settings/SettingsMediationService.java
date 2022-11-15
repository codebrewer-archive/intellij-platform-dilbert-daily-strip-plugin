package org.codebrewer.intellijplatform.plugin.dilbert.settings;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

/**
 * Class that mediates between the plug-in's state as originally persisted via
 * the legacy {@code JDOMExternalizable} API and the state as persisted via the
 * replacement {@code PersistentStateComponent} API.
 */
public interface SettingsMediationService {

  static SettingsMediationService getInstance() {
    return ApplicationManager.getApplication().getService(SettingsMediationService.class);
  }

  /**
   * Transforms from a {@code PersistentStateComponent}-persisted state
   * representation to a {@code JDOMExternalizable}-persisted state
   * representation.
   *
   * @param settingsState {@code PersistentStateComponent}-persisted state
   * representation.
   *
   * @return a {@code JDOMExternalizable}-persisted state representation.
   */
  ApplicationSettings fromSettingsState(@NotNull SettingsState settingsState);

  /**
   * Transforms from a {@code JDOMExternalizable}-persisted state representation
   * to a {@code PersistentStateComponent}-persisted state representation.
   *
   * @param applicationSettings {@code JDOMExternalizable}-persisted state
   * representation.
   *
   * @return a {@code PersistentStateComponent}-persisted state representation.
   */
  SettingsState fromApplicationSettings(@NotNull ApplicationSettings applicationSettings);

}
