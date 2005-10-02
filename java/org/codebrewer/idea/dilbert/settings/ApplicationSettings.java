/*
 *  Copyright 2005 Mark Scott
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
 * persistence by implementing {@link JDOMExternalizable JDOMExternalizable}.
 * The settings are applied to all open projects.
 * </p>
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class ApplicationSettings implements JDOMExternalizable
{
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());
  private static final String DISCLAIMER_ACKNOWLEDGED_KEY = "disclaimerAcknowledged";
  private static final String LOAD_STRIP_ON_STARTUP_KEY = "loadStripOnStartup";
  private static final String REFRESH_ALL_PROJECTS_KEY = "refreshAllOpenProjects";

  private boolean disclaimerAcknowledged = false;
  private boolean loadStripOnStartup = false;
  private boolean refreshAllOpenProjects = false;

  /**
   * Default constructor that creates an instance having configuration options
   * (boolean properties) <code>false</code>.
   */
  public ApplicationSettings()
  {
    LOGGER.debug("ApplicationSettings()");
  }

  /**
   * Indicates whether or not the user has acknowledged the plugin's disclaimer.
   *
   * @return <code>true</code> if the user has acknowledged the plugin's
   *         disclaimer, <code>false</code> if not.
   */
  public boolean isDisclaimerAcknowledged()
  {
    return disclaimerAcknowledged;
  }

  /**
   * Indicates whether the plugin should automatically download the current
   * daily strip when a project is opened or should only do so when the user
   * explicitly requests it.
   *
   * @return <code>true</code> if the current daily strip should be fetched as
   *         soon as a project is opened, <code>false</code> if not.
   */
  public boolean isLoadStripOnStartup()
  {
    return loadStripOnStartup;
  }

  /**
   * Indicates whether refreshing the plugin's toolwindow in one project should
   * cause all other open projects to be refreshed as well.
   *
   * @return <code>true</code> if updating one project should cause all other
   *         projects to be updated, <code>false</code> if not.
   */
  public boolean isRefreshAllOpenProjects()
  {
    return refreshAllOpenProjects;
  }

  /**
   * Sets whether or not the user has acknowledged the plugin's disclaimer.
   *
   * @param disclaimerAcknowledged <code>true</code> if the user has
   *                               acknowledged the plugin's disclaimer,
   *                               <code>false</code> if not.
   */
  public void setDisclaimerAcknowledged(final boolean disclaimerAcknowledged)
  {
    this.disclaimerAcknowledged = disclaimerAcknowledged;
  }

  /**
   * Sets whether or not the plugin should automatically download the current
   * daily strip when a project is opened or should only do so when the user
   * explicitly requests it.
   *
   * @param loadStripOnStartup <code>true</code> if the current daily strip
   *                           should be fetched as soon as a project is opened,
   *                           <code>false</code> if not.
   */
  public void setLoadStripOnStartup(final boolean loadStripOnStartup)
  {
    this.loadStripOnStartup = loadStripOnStartup;
  }

  /**
   * Sets whether or not refreshing the plugin's toolwindow in one project
   * should cause all other open projects to be refreshed as well.
   *
   * @param refreshAllOpenProjects <code>true</code> if updating one project
   *                               should cause all other projects to be updated,
   *                               <code>false</code> if not.
   */
  public void setRefreshAllOpenProjects(final boolean refreshAllOpenProjects)
  {
    this.refreshAllOpenProjects = refreshAllOpenProjects;
  }

  /**
   * @noinspection MethodWithMultipleReturnPoints,NonFinalFieldReferenceInEquals
   */
  public boolean equals(final Object o)
  {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (getClass() != o.getClass()) {
      return false;
    }

    final ApplicationSettings settings = (ApplicationSettings) o;

    if (disclaimerAcknowledged != settings.disclaimerAcknowledged) {
      return false;
    }
    if (loadStripOnStartup != settings.loadStripOnStartup) {
      return false;
    }

    return refreshAllOpenProjects == settings.refreshAllOpenProjects;
  }

  /**
   * @noinspection NonFinalFieldReferencedInHashCode
   */
  public int hashCode()
  {
    int result;
    result = disclaimerAcknowledged ? 1 : 0;
    result = 29 * result + (loadStripOnStartup ? 1 : 0);
    result = 29 * result + (refreshAllOpenProjects ? 1 : 0);

    return result;
  }

  // Implement JDOMExternalizable

  public void readExternal(final Element element)
  {
    LOGGER.debug("reading application settings");
    disclaimerAcknowledged = JDOMExternalizer.readBoolean(element, DISCLAIMER_ACKNOWLEDGED_KEY);
    loadStripOnStartup = JDOMExternalizer.readBoolean(element, LOAD_STRIP_ON_STARTUP_KEY);
    refreshAllOpenProjects = JDOMExternalizer.readBoolean(element, REFRESH_ALL_PROJECTS_KEY);
  }

  public void writeExternal(final Element element)
  {
    LOGGER.debug("writing application settings");
    JDOMExternalizer.write(element, DISCLAIMER_ACKNOWLEDGED_KEY, disclaimerAcknowledged);
    JDOMExternalizer.write(element, LOAD_STRIP_ON_STARTUP_KEY, loadStripOnStartup);
    JDOMExternalizer.write(element, REFRESH_ALL_PROJECTS_KEY, refreshAllOpenProjects);
  }
}