/*
 *  Copyright 2007, 2018, 2022 Mark Scott
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

package org.codebrewer.intellijplatform.plugin.dilbert.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizer;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import javax.swing.JComponent;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStripPluginService;
import org.codebrewer.intellijplatform.plugin.dilbert.ui.UnattendedDownloadSettingsPanel;
import org.codebrewer.intellijplatform.plugin.dilbert.util.TimeUtils;
import org.jdom.Element;

/**
 * <p>
 * Encapsulates the user-specified settings for unattended downloading of
 * strips and permits their persistence by implementing
 * {@link JDOMExternalizable}.  The settings are applied to all open projects.
 * </p>
 *
 * @author Mark Scott
 */
public final class UnattendedDownloadSettings implements JDOMExternalizable, Modifiable {
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPluginService.class.getName());

  // Keys used when persisting download settings
  //
  private static final String FETCH_STRIP_AUTOMATICALLY_KEY = "fetchStripAutomatically";
  private static final String LOCAL_DOWNLOAD_TIME_KEY = "localDownloadTime";
  private static final String MAX_FETCH_ATTEMPTS_KEY = "maxFetchAttempts";
  private static final String FETCH_INTERVAL_KEY = "fetchInterval";

  /**
   * The ID of the timezone in which the dilbert.com website seems to be hosted.
   */
  private static final String DILBERT_TIME_ZONE_ID_NAME = "America/Los_Angeles";

  /**
   * The timezone in which the dilbert.com website seems to be hosted.
   */
  private static final ZoneId DILBERT_TIME_ZONE_ID = ZoneId.of(DILBERT_TIME_ZONE_ID_NAME);

  /**
   * Default value for fetching strips automatically.
   */
  private static final boolean DEFAULT_FETCH_AUTOMATICALLY = false;

  /**
   * The number of seconds between the IDEA instance hosting the plug-in and the
   * dilbert.com website (assuming that the user's machine is configured for
   * localtime and has the expected locale set).
   */
  private static final int DEFAULT_LOCAL_DOWNLOAD_TIME = getDefaultLocalDownloadTime();

  /**
   * Default maximum number of fetch attempts.
   */
  public static final int DEFAULT_MAX_FETCH_ATTEMPTS = 5;

  /**
   * Default interval (in minutes) between successive fetch attempts.
   */
  public static final int DEFAULT_FETCH_INTERVAL = 10;

  /**
   * The maximum number of minutes that may elapse between successive fetch
   * attempts.
   */
  public static final int MAX_FETCH_INTERVAL = 60;

  /**
   * Upper limit on the number of fetch attempts that will be made.
   */
  public static final int MAX_MAX_FETCH_ATTEMPTS = 10;

  /**
   * The minimum number of minutes that must elapse between successive fetch
   * attempts.
   */
  public static final int MIN_FETCH_INTERVAL = 1;

  /**
   * Lower limit on the number of fetch attempts that will be made.
   */
  public static final int MIN_MAX_FETCH_ATTEMPTS = 1;

  /**
   * An instance of this class that indicates that unattended downloads should
   * be performed using default values.
   *
   * @see #DEFAULT_LOCAL_DOWNLOAD_TIME
   * @see #DEFAULT_MAX_FETCH_ATTEMPTS
   * @see #DEFAULT_FETCH_INTERVAL
   */
  public static final UnattendedDownloadSettings DEFAULT_DOWNLOAD_SETTINGS =
      new UnattendedDownloadSettings(!DEFAULT_FETCH_AUTOMATICALLY,
          DEFAULT_LOCAL_DOWNLOAD_TIME,
          DEFAULT_MAX_FETCH_ATTEMPTS,
          DEFAULT_FETCH_INTERVAL);

  /**
   * An instance of this class that indicates that unattended downloads should
   * not be performed.
   *
   * @see #DEFAULT_FETCH_AUTOMATICALLY
   */
  public static final UnattendedDownloadSettings NO_DOWNLOAD_SETTINGS =
      new UnattendedDownloadSettings(DEFAULT_FETCH_AUTOMATICALLY,
          DEFAULT_LOCAL_DOWNLOAD_TIME,
          DEFAULT_MAX_FETCH_ATTEMPTS,
          DEFAULT_FETCH_INTERVAL);

  /**
   * Gets the number of minutes that IDEA's timezone is in advance of the
   * timezone hosting the dilbert.com website.  A negative return value
   * indicates that IDEA is west of Dilbert.
   * <p/>
   * Gets the number of minutes after midnight localtime at which it's midnight
   * in LA.
   *
   * @return the number of minutes between IDEA and Dilbert.
   */
  private static int getDefaultLocalDownloadTime() {
    final Instant now = Instant.now();
    final ZoneOffset systemZoneOffset = ZoneId.systemDefault().getRules().getOffset(now);
    final ZoneOffset dilbertZoneOffset = DILBERT_TIME_ZONE_ID.getRules().getOffset(now);

    int difference = systemZoneOffset.getTotalSeconds() - dilbertZoneOffset.getTotalSeconds();

    if (difference < 0) {
      difference = TimeUtils.SECONDS_PER_DAY + difference;
    }

    return TimeUtils.secondsToMinutes(difference);
  }

  /**
   * Should the strip be fetched periodically without user intervention?
   */
  private boolean fetchStripAutomatically;

  /**
   * The time (in minutes) after midnight local time at which the strip should
   * be fetched.
   */
  private int localDownloadTime;

  /**
   * How many attempts should be made to fetch a strip?
   */
  private int maxFetchAttempts;

  /**
   * How many minutes should we wait before re-trying if an attempt fails?
   */
  private int fetchInterval;

  /**
   * Holds the configuration UI for this object.
   *
   * @noinspection InstanceVariableMayNotBeInitialized
   */
  private UnattendedDownloadSettingsPanel settingsPanel;

  /**
   * Constructs an instance using the given parameters.
   *
   * @param fetchStripAutomatically whether or not the strip should be fetched
   * automatically.
   * @param localDownloadTime the number of minutes that IDEA's timezone
   * differs from Dilbert's.
   * @param maxFetchAttempts the maximum number of attempts that should be made
   * to fetch the daily strip.
   * @param fetchInterval the number of minutes that should elapse between
   * successive fetch attempts.
   */
  public UnattendedDownloadSettings(
      final boolean fetchStripAutomatically,
      final int localDownloadTime,
      final int maxFetchAttempts,
      final int fetchInterval) {
    if (localDownloadTime < 0 || localDownloadTime >= TimeUtils.MINUTES_PER_DAY) {
      throw new IllegalArgumentException(
          MessageFormat.format("tzOffsetFromDilbert must be in range {0} to {1} : {2}", // NON-NLS
              0,
              TimeUtils.MINUTES_PER_DAY,
              localDownloadTime));
    }

    if (maxFetchAttempts < MIN_MAX_FETCH_ATTEMPTS || maxFetchAttempts > MAX_MAX_FETCH_ATTEMPTS) {
      throw new IllegalArgumentException(
          MessageFormat.format("maxFetchAttempts must be in range {0} to {1} : {2}", // NON-NLS
              MIN_MAX_FETCH_ATTEMPTS,
              MAX_MAX_FETCH_ATTEMPTS,
              maxFetchAttempts));
    }

    if (fetchInterval < MIN_FETCH_INTERVAL || fetchInterval > MAX_FETCH_INTERVAL) {
      throw new IllegalArgumentException(
          MessageFormat.format("fetchInterval must be in range {0} to {1} : {2}", // NON-NLS
              MIN_FETCH_INTERVAL,
              MAX_FETCH_INTERVAL,
              fetchInterval));
    }

    this.fetchStripAutomatically = fetchStripAutomatically;
    this.localDownloadTime = localDownloadTime;
    this.maxFetchAttempts = maxFetchAttempts;
    this.fetchInterval = fetchInterval;
  }

  public boolean equalsIgnoreFetchAutomatically(final UnattendedDownloadSettings settings) {
    final boolean result;

    if (settings == null) {
      result = false;
    } else {
      result = settings.getFetchInterval() == DEFAULT_FETCH_INTERVAL &&
               settings.getLocalDownloadTime() == DEFAULT_LOCAL_DOWNLOAD_TIME &&
               settings.getMaxFetchAttempts() == DEFAULT_MAX_FETCH_ATTEMPTS;
    }

    return result;
  }

  public Object getCurrentSettings() {
    UnattendedDownloadSettings modifiedSettings = null;

    if (settingsPanel != null) {
      modifiedSettings = settingsPanel.getDisplayedSettings();
    }

    return modifiedSettings;
  }

  public int getFetchInterval() {
    return fetchInterval;
  }

  public int getMaxFetchAttempts() {
    return maxFetchAttempts;
  }

  public int getLocalDownloadTime() {
    return localDownloadTime;
  }

  public boolean isFetchStripAutomatically() {
    return fetchStripAutomatically;
  }

  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final UnattendedDownloadSettings that = (UnattendedDownloadSettings) o;

    if (fetchInterval != that.fetchInterval) {
      return false;
    }
    if (fetchStripAutomatically != that.fetchStripAutomatically) {
      return false;
    }
    if (localDownloadTime != that.localDownloadTime) {
      return false;
    }
    //noinspection RedundantIfStatement
    if (maxFetchAttempts != that.maxFetchAttempts) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = fetchStripAutomatically ? 1 : 0;
    result = 31 * result + localDownloadTime;
    result = 31 * result + maxFetchAttempts;
    result = 31 * result + fetchInterval;
    return result;
  }

  // Implement JDOMExternalizable

  public void readExternal(final Element element) {
    LOGGER.debug("reading download settings"); // NON-NLS
    fetchStripAutomatically = JDOMExternalizer.readBoolean(element, FETCH_STRIP_AUTOMATICALLY_KEY);
    localDownloadTime =
        JDOMExternalizer.readInteger(element, LOCAL_DOWNLOAD_TIME_KEY, DEFAULT_LOCAL_DOWNLOAD_TIME);
    maxFetchAttempts =
        JDOMExternalizer.readInteger(element, MAX_FETCH_ATTEMPTS_KEY, DEFAULT_MAX_FETCH_ATTEMPTS);
    fetchInterval =
        JDOMExternalizer.readInteger(element, FETCH_INTERVAL_KEY, DEFAULT_FETCH_INTERVAL);
  }

  public void writeExternal(final Element element) {
    LOGGER.debug("writing download settings"); // NON-NLS
    JDOMExternalizer.write(element, FETCH_STRIP_AUTOMATICALLY_KEY, fetchStripAutomatically);
    JDOMExternalizer.write(element, LOCAL_DOWNLOAD_TIME_KEY, localDownloadTime);
    JDOMExternalizer.write(element, MAX_FETCH_ATTEMPTS_KEY, maxFetchAttempts);
    JDOMExternalizer.write(element, FETCH_INTERVAL_KEY, fetchInterval);
  }

  // Implement Modifiable

  public void setConfigurationUIEnabled(final boolean enabled) {
    if (settingsPanel != null) {
      settingsPanel.setEnabled(enabled);
    }
  }

  // Implement UnnamedConfigurable

  public JComponent createComponent() {
    if (settingsPanel == null) {
      settingsPanel = new UnattendedDownloadSettingsPanel(this);
    }

    return settingsPanel;
  }

  public boolean isModified() {
    boolean isModified = false;

    if (settingsPanel != null) {
      isModified = settingsPanel.isModified(this);
    }

    return isModified;
  }

  public void apply() {
    if (settingsPanel != null) {
      final UnattendedDownloadSettings displayedSettings = settingsPanel.getDisplayedSettings();
      fetchStripAutomatically = displayedSettings.isFetchStripAutomatically();
      localDownloadTime = displayedSettings.getLocalDownloadTime();
      maxFetchAttempts = displayedSettings.getMaxFetchAttempts();
      fetchInterval = displayedSettings.getFetchInterval();
    }
  }

  public void reset() {
    if (settingsPanel != null) {
      settingsPanel.setDisplayedSettings(this);
    }
  }

  public void disposeUIResources() {
    LOGGER.info("UnattendedDownloadSettings.disposeUIResources()"); // NON-NLS
  }
}
