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

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStripPluginService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStripPlugin",
       storages = {
           @Storage(value = "dilbert.plugin.xml")
       })
public class SettingsServiceImpl implements SettingsService {
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER =
      Logger.getInstance(DilbertDailyStripPluginService.class.getName());

  private SettingsState state;

  @NotNull
  @Override
  public ApplicationSettings getSavedApplicationSettings() {
    return state == null ?
           new ApplicationSettings() :
           SettingsMediationService.getInstance().fromSettingsState(state);
  }

  @Override
  public void setSavedApplicationSettings(@NotNull ApplicationSettings applicationSettings) {
    loadState(SettingsMediationService.getInstance().fromApplicationSettings(applicationSettings));
  }

  @Nullable
  @Override
  public SettingsState getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull SettingsState state) {
    LOGGER.info("Loading settings state: " + state);
    this.state = state;
  }

  @Override
  public void noStateLoaded() {
    LOGGER.info("Loading default settings state");
    state = new SettingsState();
  }
}
