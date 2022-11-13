/*
 *  Copyright 2020, 2022 Mark Scott
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

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.codebrewer.intellijplatform.plugin.dilbert.ui.DailyStripPanel;
import org.jetbrains.annotations.NotNull;

public class DilbertDailyStripToolWindowFactory implements DumbAware, ToolWindowFactory {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    final DailyStripPanel dailyStripPanel = new DailyStripPanel(project);
    final Content content =
        ContentFactory.getInstance().createContent(dailyStripPanel, null, true);

    content.setDisposer(dailyStripPanel::dispose);
    content.setPreferredFocusableComponent(dailyStripPanel);
    toolWindow.getContentManager().addContent(content);
    dailyStripPanel.initialise();
  }
}
