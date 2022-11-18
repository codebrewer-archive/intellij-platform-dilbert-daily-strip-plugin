/*
 *  Copyright 2005, 2007, 2008, 2018, 2022 Mark Scott
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.codebrewer.intellijplatform.plugin.dilbert;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.codebrewer.intellijplatform.plugin.dilbert.http.DilbertDailyStripFetcher;
import org.codebrewer.intellijplatform.plugin.dilbert.settings.ApplicationSettings;
import org.codebrewer.intellijplatform.plugin.dilbert.settings.SettingsService;
import org.codebrewer.intellijplatform.plugin.dilbert.settings.UnattendedDownloadSettings;
import org.codebrewer.intellijplatform.plugin.dilbert.strategy.CurrentDailyStripProvider;
import org.codebrewer.intellijplatform.plugin.dilbert.strategy.DailyStripProvider;
import org.codebrewer.intellijplatform.plugin.dilbert.ui.DailyStripPresenter;
import org.codebrewer.intellijplatform.plugin.dilbert.ui.SettingsPanel;
import org.codebrewer.intellijplatform.plugin.dilbert.util.PeriodicStripFetcher;
import org.codebrewer.intellijplatform.plugin.dilbert.util.VersionInfo;
import org.codebrewer.intellijplatform.plugin.util.l10n.ResourceBundleManager;

/**
 * <p>
 * An implementation of a plugin that fetches and displays the current daily
 * cartoon strip from the dilbert.com website.
 * </p>
 *
 * @author Mark Scott
 */
public final class DilbertDailyStripPluginServiceImpl implements DilbertDailyStripPluginService {
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER =
      Logger.getInstance(DilbertDailyStripPluginService.class.getName());

  /**
   * Application-level settings for the plug-in, shared by all open projects.
   */
  private ApplicationSettings settings;

  /**
   * A UI component that permits our <code>ApplicationSettings</code> to be
   * edited.
   *
   * @noinspection InstanceVariableMayNotBeInitialized
   */
  private SettingsPanel settingsPanel;

  private DilbertDailyStrip dilbertDailyStrip;

  private final PeriodicStripFetcher periodicStripFetcher;
  private final EventListenerList listenerList;

  /**
   * Constructs a plugin implementation.
   */
  public DilbertDailyStripPluginServiceImpl() {
    LOGGER.info(
        "Dilbert Daily Strip Plug-in, version " + VersionInfo.getVersionString() + ", built " +
        VersionInfo.getBuildDate());

    dilbertDailyStrip = DilbertDailyStrip.MISSING_STRIP;
    settings = SettingsService.getInstance().getSavedApplicationSettings();
    periodicStripFetcher = new PeriodicStripFetcher();
    listenerList = new EventListenerList();
    configureUnattendedDownloads();
  }

  private void configureUnattendedDownloads() {
    final UnattendedDownloadSettings unattendedDownloadSettings;

    if (settings.getUnattendedDownloadSettings().isFetchStripAutomatically()) {
      unattendedDownloadSettings = settings.getUnattendedDownloadSettings();
    } else {
      unattendedDownloadSettings = UnattendedDownloadSettings.NO_DOWNLOAD_SETTINGS;
    }

    periodicStripFetcher.startPeriodicFetching(unattendedDownloadSettings);
  }

  // Implement DilbertDailyStripPlugin

  public void addDailyStripListener(final DailyStripListener listener) {
    if (listener != null) {
      listenerList.add(DailyStripListener.class, listener);
    }
  }

  public void fetchDailyStrip() {
    fetchDailyStrip(DilbertDailyStrip.MISSING_STRIP.getImageChecksum());
  }

  public void fetchDailyStrip(final String homepageEtag) {
    if (isDisclaimerAcknowledged()) {
      LOGGER.info("Disclaimer acknowledged, fetching Dilbert daily strip asynchronously");

      AppExecutorUtil.getAppExecutorService().submit(new FetchDailyStripTask(homepageEtag));
    }
  }

  public DilbertDailyStrip getCachedDailyStrip() {
    return dilbertDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP) ? null : dilbertDailyStrip;
  }

  public DailyStripProvider[] getDailyStripProviders(final DailyStripPresenter presenter) {
    return new DailyStripProvider[] { new CurrentDailyStripProvider(presenter) };
  }

  public boolean isDisclaimerAcknowledged() {
    return settings.isDisclaimerAcknowledged();
  }

  public void removeDailyStripListener(final DailyStripListener listener) {
    if (listener != null) {
      listenerList.remove(DailyStripListener.class, listener);
    }
  }

  // Implement Disposable

  public void dispose() {
    LOGGER.info("Disposing Dilbert");
    periodicStripFetcher.stopPeriodicFetching();
    LOGGER.info("Dilbert disposed");
  }

  // Implement Configurable

  public String getDisplayName() {
    final ResourceBundle resourceBundle =
        ResourceBundleManager.getResourceBundle(DilbertDailyStripPluginService.class);

    return resourceBundle.getString("plugin.name.configuration");
  }

  public String getHelpTopic() {
    return null;
  }

  // Implement UnnamedConfigurable

  public JComponent createComponent() {
    settingsPanel = new SettingsPanel(settings);

    return settingsPanel;
  }

  public boolean isModified() {
    boolean isModified = false;

    if (settingsPanel != null) {
      isModified = settingsPanel.isModified(settings);
    }

    return isModified;
  }

  public void apply() {
    if (settingsPanel != null) {
      // Save the current settings for future use
      //
      settings = settingsPanel.getDisplayedSettings();
      SettingsService.getInstance().setSavedApplicationSettings(settings);

      // Account for any changes made to the unattended download settings
      //
      configureUnattendedDownloads();
    }
  }

  public void reset() {
    if (settingsPanel != null) {
      settingsPanel.setSettings(settings);
    }
  }

  public void disposeUIResources() {
  }

  private class FetchDailyStripTask implements Runnable {
    private final String homepageEtag;

    private FetchDailyStripTask(final String homepageEtag) {
      this.homepageEtag = homepageEtag;
    }

    private void fireDailyStripUpdated(final DilbertDailyStrip dailyStrip) {
      dilbertDailyStrip = dailyStrip;

      DailyStripEvent e = null;

      final Object[] listeners = listenerList.getListenerList();
      for (int i = 0; i < listeners.length; i += 2) {
        if (listeners[i] == DailyStripListener.class) {
          if (e == null) {
            e = new DailyStripEvent(this, dailyStrip);
          }
          ((DailyStripListener) listeners[i + 1]).dailyStripUpdated(e);
        }
      }
    }

    public void run() {
      try {
        final DilbertDailyStrip dailyStrip =
            new DilbertDailyStripFetcher().fetchDailyStrip(homepageEtag);

        if (dailyStrip != null) {
          fireDailyStripUpdated(dailyStrip);
        }
      } catch (IOException e) {
        LOGGER.info("Error fetching current daily strip from dilbert.com", e); // NON-NLS
        fireDailyStripUpdated(DilbertDailyStrip.MISSING_STRIP);
      }
    }
  }
}
