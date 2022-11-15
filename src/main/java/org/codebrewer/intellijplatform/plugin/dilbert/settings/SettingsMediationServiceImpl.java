package org.codebrewer.intellijplatform.plugin.dilbert.settings;

import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.DEFAULT_FETCH_AUTOMATICALLY;
import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.DEFAULT_FETCH_INTERVAL;
import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.DEFAULT_LOCAL_DOWNLOAD_TIME;
import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.DEFAULT_MAX_FETCH_ATTEMPTS;
import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.FETCH_INTERVAL_KEY;
import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.FETCH_STRIP_AUTOMATICALLY_KEY;
import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.LOCAL_DOWNLOAD_TIME_KEY;
import static org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsState.MAX_FETCH_ATTEMPTS_KEY;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SettingsMediationServiceImpl implements SettingsMediationService {
  @Override
  public ApplicationSettings fromSettingsState(@NotNull SettingsState settingsState) {
    return new ApplicationSettings(
        settingsState.isDisclaimerAcknowledged(),
        new UnattendedDownloadSettings(
            Boolean.parseBoolean(
                settingsState.getUnattendedDownloadSettings().getOrDefault(
                    FETCH_STRIP_AUTOMATICALLY_KEY, DEFAULT_FETCH_AUTOMATICALLY)),
            Integer.parseInt(
                settingsState.getUnattendedDownloadSettings().getOrDefault(
                    LOCAL_DOWNLOAD_TIME_KEY, DEFAULT_LOCAL_DOWNLOAD_TIME)),
            Integer.parseInt(
                settingsState.getUnattendedDownloadSettings().getOrDefault(
                    MAX_FETCH_ATTEMPTS_KEY, DEFAULT_MAX_FETCH_ATTEMPTS)),
            Integer.parseInt(
                settingsState.getUnattendedDownloadSettings().getOrDefault(
                    FETCH_INTERVAL_KEY, DEFAULT_FETCH_INTERVAL))
        )
    );
  }

  @Override
  public SettingsState fromApplicationSettings(@NotNull ApplicationSettings applicationSettings) {
    return new SettingsState(
        applicationSettings.isDisclaimerAcknowledged(),
        Map.of(
            SettingsState.FETCH_INTERVAL_KEY,
            String.valueOf(
                applicationSettings.getUnattendedDownloadSettings().getFetchInterval()),
            FETCH_STRIP_AUTOMATICALLY_KEY,
            String.valueOf(
                applicationSettings.getUnattendedDownloadSettings().isFetchStripAutomatically()),
            SettingsState.LOCAL_DOWNLOAD_TIME_KEY,
            String.valueOf(
                applicationSettings.getUnattendedDownloadSettings().getLocalDownloadTime()),
            SettingsState.MAX_FETCH_ATTEMPTS_KEY,
            String.valueOf(
                applicationSettings.getUnattendedDownloadSettings().getMaxFetchAttempts())));
  }
}
