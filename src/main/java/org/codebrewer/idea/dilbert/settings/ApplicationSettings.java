/*
 *  Copyright 2005, 2007, 2018 Mark Scott
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

package org.codebrewer.idea.dilbert.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizer;
import org.codebrewer.idea.dilbert.DilbertDailyStripPlugin;
import org.jdom.Element;

/**
 * <p>
 * Encapsulates the user-specified settings for the plugin and permits their
 * persistence by implementing {@link JDOMExternalizable}.  The settings are
 * applied to all open projects.
 * </p>
 *
 * @author Mark Scott
 */
public final class ApplicationSettings implements JDOMExternalizable {
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());
  private static final boolean DEFAULT_DISCLAIMER_ACKNOWLEDGED = false;
  private static final String DISCLAIMER_ACKNOWLEDGED_KEY = "disclaimerAcknowledged";

  /**
   * Has the plugin's disclaimer been acknowledged?
   */
  private boolean disclaimerAcknowledged;

  /**
   * Settings to control unattended downloading of strips.
   */
  private UnattendedDownloadSettings unattendedDownloadSettings;

  /**
   * The key used when persisting unattended download settings.
   */
  private static final String DOWNLOAD_SETTINGS_PROVIDER_KEY = "downloads";

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
   * Indicates whether or not the user has acknowledged the plugin's disclaimer.
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

  // Implement JDOMExternalizable

  public void readExternal(final Element element) {
    LOGGER.debug("reading application settings"); // NON-NLS
    disclaimerAcknowledged = JDOMExternalizer.readBoolean(element, DISCLAIMER_ACKNOWLEDGED_KEY);

    final Element downloadsElement = element.getChild(DOWNLOAD_SETTINGS_PROVIDER_KEY);

    if (downloadsElement != null) {
      unattendedDownloadSettings.readExternal(downloadsElement);
    }
  }

  public void writeExternal(final Element element) {
    LOGGER.debug("writing application settings"); // NON-NLS
    JDOMExternalizer.write(element, DISCLAIMER_ACKNOWLEDGED_KEY, disclaimerAcknowledged);

    final Element downloadsElement = new Element(DOWNLOAD_SETTINGS_PROVIDER_KEY);

    unattendedDownloadSettings.writeExternal(downloadsElement);
    element.addContent(downloadsElement);
  }
}
