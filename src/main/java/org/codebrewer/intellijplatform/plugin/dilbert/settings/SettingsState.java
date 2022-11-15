/*
 *  Copyright 2022 Mark Scott
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

import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.XMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Encapsulates the user-specified settings for the plug-in, persisted using the
 * {@code PersistentStateComponent} API.
 * </p>
 *
 * <p>
 * To avoid inconvenience for users, this class uses the same serialization
 * representation as when the plug-in used the legacy {@code JDOMExternalizable}
 * API.
 * </p>
 *
 * @author Mark Scott
 */
public final class SettingsState {
  /**
   * Key for the property stating how many minutes should elapse before
   * re-trying after a failed attempt to download the strip.
   */
  static final String FETCH_INTERVAL_KEY = "fetchInterval";

  /**
   * Key for the property stating whether the strip should be fetched
   * periodically without user intervention.
   */
  static final String FETCH_STRIP_AUTOMATICALLY_KEY = "fetchStripAutomatically";

  /**
   * Key for the property stating the time (in minutes) after midnight local
   * time at which the strip should be fetched.
   */
  static final String LOCAL_DOWNLOAD_TIME_KEY = "localDownloadTime";

  /**
   * Key for the property stating how many attempts should be made to fetch a
   * strip.
   */
  static final String MAX_FETCH_ATTEMPTS_KEY = "maxFetchAttempts";

  /**
   * Default value for whether the user has acknowledged the plug-in's
   * disclaimer.
   */
  static final boolean DEFAULT_DISCLAIMER_ACKNOWLEDGED =
      ApplicationSettings.DEFAULT_DISCLAIMER_ACKNOWLEDGED;

  /**
   * Default value for fetching strips automatically.
   */
  static final String DEFAULT_FETCH_AUTOMATICALLY =
      String.valueOf(UnattendedDownloadSettings.DEFAULT_FETCH_AUTOMATICALLY);

  /**
   * Default interval (in minutes) between successive fetch attempts.
   */
  static final String DEFAULT_FETCH_INTERVAL =
      String.valueOf(UnattendedDownloadSettings.DEFAULT_FETCH_INTERVAL);

  /**
   * The number of seconds between the IDEA instance hosting the plug-in and the
   * dilbert.com website (assuming that the user's machine is configured for
   * localtime and has the expected locale set).
   */
  static final String DEFAULT_LOCAL_DOWNLOAD_TIME =
      String.valueOf(UnattendedDownloadSettings.getDefaultLocalDownloadTime());

  /**
   * Default maximum number of fetch attempts.
   */
  static final String DEFAULT_MAX_FETCH_ATTEMPTS =
      String.valueOf(UnattendedDownloadSettings.DEFAULT_MAX_FETCH_ATTEMPTS);

  /**
   * Default unattended download settings.
   *
   * @see #DEFAULT_FETCH_INTERVAL
   * @see #DEFAULT_FETCH_AUTOMATICALLY
   * @see #DEFAULT_LOCAL_DOWNLOAD_TIME
   * @see #DEFAULT_MAX_FETCH_ATTEMPTS
   */
  private static final Map<String, String> DEFAULT_UNATTENDED_DOWNLOAD_SETTINGS =
      Map.of(FETCH_INTERVAL_KEY, DEFAULT_FETCH_INTERVAL,
          FETCH_STRIP_AUTOMATICALLY_KEY, DEFAULT_FETCH_AUTOMATICALLY,
          LOCAL_DOWNLOAD_TIME_KEY, DEFAULT_LOCAL_DOWNLOAD_TIME,
          MAX_FETCH_ATTEMPTS_KEY, DEFAULT_MAX_FETCH_ATTEMPTS);

  /**
   * Has the plug-in's disclaimer been acknowledged?
   */
  @OptionTag(tag = "setting")
  private final boolean disclaimerAcknowledged;

  @XMap(propertyElementName = "downloads", entryTagName = "setting", keyAttributeName = "name")
  private final Map<String, String> unattendedDownloadSettings;

  /**
   * Constructs an instance using default parameter values.
   *
   * @see #DEFAULT_DISCLAIMER_ACKNOWLEDGED
   * @see #DEFAULT_UNATTENDED_DOWNLOAD_SETTINGS
   */
  public SettingsState() {
    //noinspection ConstantConditions
    this(DEFAULT_DISCLAIMER_ACKNOWLEDGED, new HashMap<>(DEFAULT_UNATTENDED_DOWNLOAD_SETTINGS));
  }

  /**
   * Constructs an instance using the given parameters.
   *
   * @param disclaimerAcknowledged whether the user has acknowledged the
   * plug-in's disclaimer.
   * @param unattendedDownloadSettings the configuration for automatically
   * downloading the daily strip.
   */
  public SettingsState(
      final boolean disclaimerAcknowledged,
      @NotNull final Map<String, String> unattendedDownloadSettings) {
    this.disclaimerAcknowledged = disclaimerAcknowledged;
    this.unattendedDownloadSettings = unattendedDownloadSettings;
  }

  public boolean isDisclaimerAcknowledged() {
    return disclaimerAcknowledged;
  }

  public Map<String, String> getUnattendedDownloadSettings() {
    return unattendedDownloadSettings;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SettingsState that = (SettingsState) o;
    return disclaimerAcknowledged == that.disclaimerAcknowledged &&
           Objects.equals(unattendedDownloadSettings, that.unattendedDownloadSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(disclaimerAcknowledged, unattendedDownloadSettings);
  }

  @Override
  public String toString() {
    return "SettingsState{" +
           "disclaimerAcknowledged=" + disclaimerAcknowledged +
           ", unattendedDownloadSettings=" + unattendedDownloadSettings +
           '}';
  }
}
