/*
 *  Copyright 2005, 2007 Mark Scott
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
package org.codebrewer.idea.dilbert;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import org.codebrewer.idea.dilbert.http.DilbertDailyStripFetcher;
import org.codebrewer.idea.dilbert.settings.ApplicationSettings;
import org.codebrewer.idea.dilbert.settings.UnattendedDownloadSettings;
import org.codebrewer.idea.dilbert.strategy.CurrentDailyStripProvider;
import org.codebrewer.idea.dilbert.strategy.DailyStripProvider;
import org.codebrewer.idea.dilbert.ui.DailyStripPresenter;
import org.codebrewer.idea.dilbert.ui.SettingsPanel;
import org.codebrewer.idea.dilbert.util.PeriodicStripFetcher;
import org.codebrewer.idea.dilbert.util.VersionInfo;
import org.codebrewer.idea.util.l10n.ResourceBundleManager;
import org.jdom.Element;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

/**
 * <p>
 * An implementation of a plugin that fetches and displays the current daily
 * cartoon strip from the dilbert.com website.
 * </p>
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class DilbertDailyStripPluginImpl implements DilbertDailyStripPlugin
{
  /**
   * Icon for use on the plugin settings page.
   *
   * @noinspection HardcodedFileSeparator
   */
  private static final Icon ICON_LARGE = IconLoader.getIcon("/dilbert32x32.png"); // NON-NLS

  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * The time at the Unix epoch.
   */
  private static final int EPOCH = 0;

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

  private final Timer backgroundTaskExecutor;
  private final PeriodicStripFetcher periodicStripFetcher;
  private final EventListenerList listenerList;

  /**
   * Constructs a plugin implementation.
   */
  public DilbertDailyStripPluginImpl()
  {
    LOGGER.info(new StringBuffer("Dilbert Daily Strip Plug-in, version ") // NON-NLS
        .append(VersionInfo.getVersionString())
        .append(", built ") // NON-NLS
        .append(VersionInfo.getBuildDate())
        .toString());

    dilbertDailyStrip = DilbertDailyStrip.MISSING_STRIP;
    settings = new ApplicationSettings();
    backgroundTaskExecutor = new Timer();
    periodicStripFetcher = new PeriodicStripFetcher();
    listenerList = new EventListenerList();
  }

  private void configureUnattendedDownloads()
  {
    final UnattendedDownloadSettings unattendedDownloadSettings;

    if (settings.getUnattendedDownloadSettings().isFetchStripAutomatically()) {
      unattendedDownloadSettings = settings.getUnattendedDownloadSettings();
    }
    else {
      unattendedDownloadSettings = UnattendedDownloadSettings.NO_DOWNLOAD_SETTINGS;
    }

    periodicStripFetcher.startPeriodicFetching(unattendedDownloadSettings);
  }

  // Implement DilbertDailyStripPlugin

  public void addDailyStripListener(final DailyStripListener listener)
  {
    if (listener != null) {
      listenerList.add(DailyStripListener.class, listener);
    }
  }

  public void fetchDailyStrip()
  {
    fetchDailyStrip(EPOCH);
  }

  public void fetchDailyStrip(final long ifModifiedSince)
  {
    if (isDisclaimerAcknowledged()) {
      LOGGER.info("disclaimer ack'd"); // NON-NLS

      backgroundTaskExecutor.schedule(new FetchDailyStripTask(ifModifiedSince), 0);
    }
  }

  public DilbertDailyStrip getCachedDailyStrip()
  {
    return dilbertDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP) ? null : dilbertDailyStrip;
  }

  public DailyStripProvider[] getDailyStripProviders(final DailyStripPresenter presenter)
  {
    return new DailyStripProvider[]{ new CurrentDailyStripProvider(presenter) };
  }

  public boolean isDisclaimerAcknowledged()
  {
    return settings.isDisclaimerAcknowledged();
  }

  public void removeDailyStripListener(final DailyStripListener listener)
  {
    if (listener != null) {
      listenerList.remove(DailyStripListener.class, listener);
    }
  }

  // Implement BaseComponent

  public void disposeComponent()
  {
    periodicStripFetcher.stopPeriodicFetching();
    backgroundTaskExecutor.cancel();
  }

  public String getComponentName()
  {
    return DilbertDailyStripPlugin.class.getName();
  }

  public void initComponent()
  {
  }

  // Implement Configurable

  public String getDisplayName()
  {
    return ResourceBundleManager.getResourceBundle(
        DilbertDailyStripPlugin.class).getString("plugin.name.configuration");
  }

  public Icon getIcon()
  {
    return ICON_LARGE;
  }

  public String getHelpTopic()
  {
    // Return the value of the "target" attribute of a helpset "tocitem" element
    //
    return "settings"; // NON-NLS
  }

  // Implement JDOMExternalizable

  public void readExternal(final Element element)
  {
    settings.readExternal(element);
    configureUnattendedDownloads();
  }

  public void writeExternal(final Element element)
  {
    settings.writeExternal(element);
  }

  // Implement NamedJDOMExternalizable

  /**
   * Return the root part of the name of the file to which the plugin will save
   * its configuration data.  The value returned will have the suffix .xml
   * appended to form the full filename, and the file will be created in the
   * ${idea.config.path}/options/ directory.
   *
   * @return the root part of the configuration settings filename for the
   *         plugin.
   */
  public String getExternalFileName()
  {
    return "dilbert.plugin"; // NON-NLS
  }

  // Implement UnnamedConfigurable

  public JComponent createComponent()
  {
    settingsPanel = new SettingsPanel(settings);

    return settingsPanel;
  }

  public boolean isModified()
  {
    boolean isModified = false;

    if (settingsPanel != null) {
      isModified = settingsPanel.isModified(settings);
    }

    return isModified;
  }

  public void apply()
  {
    if (settingsPanel != null) {
      // Save the current settings for future use
      //
      settings = settingsPanel.getDisplayedSettings();

      // Account for any changes made to the unattended download settings
      //
      configureUnattendedDownloads();
    }
  }

  public void reset()
  {
    if (settingsPanel != null) {
      settingsPanel.setSettings(settings);
    }
  }

  public void disposeUIResources()
  {
  }

  private class FetchDailyStripTask extends TimerTask
  {
    private final long ifModifiedSince;

    private FetchDailyStripTask(final long ifModifiedSince)
    {
      this.ifModifiedSince = ifModifiedSince;
    }

    private void fireDailyStripUpdated(final DilbertDailyStrip dailyStrip)
    {
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

    public void run()
    {
      try {
        final DilbertDailyStrip dailyStrip = new DilbertDailyStripFetcher().fetchDailyStrip(ifModifiedSince);

        if (dailyStrip != null) {
          fireDailyStripUpdated(dailyStrip);
        }
      }
      catch (IOException e) {
        LOGGER.info("Error fetching current daily strip from dilbert.com", e); // NON-NLS
        fireDailyStripUpdated(DilbertDailyStrip.MISSING_STRIP);
      }
    }
  }
}
