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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import org.codebrewer.intellijplatform.plugin.dilbert.strategy.DailyStripProvider;
import org.codebrewer.intellijplatform.plugin.dilbert.ui.DailyStripPresenter;

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
 */
public interface DilbertDailyStripPluginService extends Configurable, Disposable {
  /**
   * The text that will be displayed on the button for the plugin's tool window.
   */
  String TOOL_WINDOW_ID = "Dilbert";

  /**
   * Adds the specified daily strip listener as a recipient of daily strip
   * events.  If listener <code>listener</code> is <code>null</code> then no
   * exception is thrown and no action is performed.
   *
   * @param listener the listener to add.
   */
  void addDailyStripListener(DailyStripListener listener);

  /**
   * Requests that registered listeners are refreshed with the current daily
   * strip from the dilbert.com website.
   */
  void fetchDailyStrip();

  /**
   * Requests that registered listeners are refreshed with the current daily
   * strip from the dilbert.com website if it has a checksum value that differs
   * from that given.
   *
   * @param md5Hash the 32-character MD5 checksum hash for the current strip.
   */
  void fetchDailyStrip(String md5Hash);

  /**
   * Gets the last downloaded strip, if any.  Implementations should return
   * <code>null</code> if no cached strip is available and should
   * <strong>not</strong> attempt to download the current strip.
   *
   * @return the last strip downloaded or <code>null</code> if none has been
   * downloaded.
   */
  DilbertDailyStrip getCachedDailyStrip();

  /**
   * Gets an array of <code>DailyStripProvider</code>s that should operate in
   * the context of <code>presenter</code> (to provide it with daily strips
   * according to the strategies they implement).
   *
   * @param presenter a client <code>DailyStripPresenter</code>.
   *
   * @return a non-<code>null</code> but possibly empty array of providers for
   * <code>presenter</code>.
   */
  DailyStripProvider[] getDailyStripProviders(DailyStripPresenter presenter);

  /**
   * Has the user has acknowledged the plugin's disclaimer?
   *
   * @return <code>true</code> if the user has acknowledged the plugin's
   * disclaimer, <code>false</code> if not.
   */
  boolean isDisclaimerAcknowledged();

  /**
   * Removes the specified daily strip listener as a recipient of daily strip
   * events.  If listener <code>listener</code> is <code>null</code> or was
   * never added as a listener then no exception is thrown and no action is
   * performed.
   *
   * @param listener the listener to remove.
   */
  void removeDailyStripListener(DailyStripListener listener);
}
