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
package org.codebrewer.idea.dilbert;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.NamedJDOMExternalizable;

import java.io.IOException;

import org.codebrewer.idea.dilbert.ui.DailyStripPresenter;

/**
 * <p>
 * An interface for a plugin that fetches and displays the current daily cartoon
 * strip from the dilbert.com website.
 * </p>
 * <p>
 * An implementation has application-level configurable settings that it stores
 * in its own configuration file, and has project-level state.
 * </p>
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public interface DilbertDailyStripPlugin extends ApplicationComponent,
    Configurable, NamedJDOMExternalizable, ProjectManagerListener
{
  /**
   * The text that will be displayed on the button for the plugin's toolwindow.
   */
  String TOOL_WINDOW_ID = "Dilbert";

  /**
   * Requests that the given presenter is refreshed with the current daily strip
   * from the dilbert.com website.
   *
   * @param presenter the client requesting an update.
   * @throws IOException if an error occurs fetching the strip.
   */
  void fetchDailyStrip(DailyStripPresenter presenter) throws IOException;

  /**
   * Requests that the given presenter is refreshed with the current daily strip
   * from the dilbert.com website if it was modified more recently than the
   * given time.
   *
   * @param presenter the client requesting an update.
   * @param ifModifiedSince a number of milliseconds since the epoch.
   * @throws IOException if an error occurs fetching the strip.
   */
  void fetchDailyStrip(DailyStripPresenter presenter, long ifModifiedSince) throws IOException;

  /**
   * Indicates whether or not the user has acknowledged the plugin's disclaimer.
   *
   * @return <code>true</code> if the user has acknowledged the plugin's
   *         disclaimer, <code>false</code> if not.
   */
  boolean isDisclaimerAcknowledged();

}