/*
 *  Copyright 2005, 2007, 2018, 2022 Mark Scott
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

/**
 * <p>
 * Encapsulates the user-specified settings for the plugin. The settings are
 * applied to all open projects.
 * </p>
 *
 * @author Mark Scott
 */
public final class ApplicationSettings {
  static final boolean DEFAULT_DISCLAIMER_ACKNOWLEDGED = false;

  /**
   * Has the plugin's disclaimer been acknowledged?
   */
  private final boolean disclaimerAcknowledged;

  /**
   * Settings to control unattended downloading of strips.
   */
  private final UnattendedDownloadSettings unattendedDownloadSettings;

  /**
   * Default constructor that creates an instance having the disclaimer
   * acknowledgment configuration parameter <code>false</code> and no
   * unattended downloads configured.
   */
  public ApplicationSettings() {
    this(DEFAULT_DISCLAIMER_ACKNOWLEDGED, UnattendedDownloadSettings.NO_DOWNLOAD_SETTINGS);
  }

  /**
   * Creates an instance having the given disclaimer acknowledgment
   * configuration parameter, persistence service settings and unattended
   * download settings.
   *
   * @param disclaimerAcknowledged has the user acknowledged the plugin's
   * disclaimer?
   * @param unattendedDownloadSettings settings that control unattended download
   * of strips.
   */
  public ApplicationSettings(final boolean disclaimerAcknowledged,
                             final UnattendedDownloadSettings unattendedDownloadSettings) {
    this.disclaimerAcknowledged = disclaimerAcknowledged;
    this.unattendedDownloadSettings = unattendedDownloadSettings;
  }

  /**
   * Gets the settings that control unattended downloading of strips.
   *
   * @return the settings that control unattended downloading of strips.
   */
  public UnattendedDownloadSettings getUnattendedDownloadSettings() {
    return unattendedDownloadSettings;
  }

  /**
   * Indicates whether the user has acknowledged the plugin's disclaimer.
   *
   * @return <code>true</code> if the user has acknowledged the plugin's
   * disclaimer, <code>false</code> if not.
   */
  public boolean isDisclaimerAcknowledged() {
    return disclaimerAcknowledged;
  }

  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ApplicationSettings that = (ApplicationSettings) o;

    if (disclaimerAcknowledged != that.disclaimerAcknowledged) {
      return false;
    }
    //noinspection RedundantIfStatement
    if (!unattendedDownloadSettings.equals(that.unattendedDownloadSettings)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = disclaimerAcknowledged ? 1 : 0;
    result = 31 * result + unattendedDownloadSettings.hashCode();

    return result;
  }
}
