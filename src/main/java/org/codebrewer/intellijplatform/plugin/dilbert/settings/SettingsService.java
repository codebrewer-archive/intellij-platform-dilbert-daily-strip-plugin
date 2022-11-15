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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;

public interface SettingsService extends PersistentStateComponent<SettingsState> {

  static SettingsService getInstance() {
    return ApplicationManager.getApplication().getService(SettingsService.class);
  }

  @NotNull
  ApplicationSettings getSavedApplicationSettings();

  void setSavedApplicationSettings(@NotNull ApplicationSettings applicationSettings);

}
