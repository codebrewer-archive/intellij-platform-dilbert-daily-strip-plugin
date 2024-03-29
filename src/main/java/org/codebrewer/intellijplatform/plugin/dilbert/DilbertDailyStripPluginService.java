/*
 *  Copyright 2005, 2007, 2008, 2018, 2022, 2023 Mark Scott
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
public interface DilbertDailyStripPluginService {
  /**
   * The text that will be displayed on the button for the plugin's tool window.
   */
  String TOOL_WINDOW_ID = "Dilbert";
}
