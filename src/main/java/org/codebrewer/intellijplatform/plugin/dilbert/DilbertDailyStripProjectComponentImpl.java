/*
 *  Copyright 2007, 2018 Mark Scott
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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javax.swing.Icon;
import org.codebrewer.intellijplatform.plugin.dilbert.ui.DailyStripPanel;
import org.jetbrains.annotations.NotNull;

/**
 * A project-level component implementation that manages the plug-in toolwindow
 * that's created for each open IDEA project.
 *
 * @author Mark Scott
 */
public class DilbertDailyStripProjectComponentImpl implements ProjectComponent {
  /**
   * Icon for use on the toolwindow button.
   *
   * @noinspection HardcodedFileSeparator
   */
  private static final Icon ICON_SMALL = IconLoader.getIcon("/dilbert13x13.png"); // NON-NLS

  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * The <code>Project</code> instance for which this project-level component
   * was created.
   */
  private final Project project;

  /**
   * The project-level UI of this component (presented in a toolwindow).
   */
  private DailyStripPanel dailyStripPanel;

  /**
   * Sole constructor that IDEA presumably invokes via reflection.
   *
   * @param project the <code>Project</code> instance with which this
   * <code>ProjectComponent</code> is associated.
   */
  public DilbertDailyStripProjectComponentImpl(final Project project) {
    this.project = project;
  }

  // Implement BaseComponent

  @NotNull
  public String getComponentName() {
    // The value returned is used inter alia as the value of the 'name'
    // attribute of a 'component' element in the project's .iws file (this
    // plug-in stores project-level configuration data in the .iws file rather
    // than the .ipr file), so we use this class's fully-qualified name to
    // reduce the possibility of clashing with another element
    //
    return getClass().getPackage().getName();
  }

  public void initComponent() {
    // Nothing to do?
  }

  public void disposeComponent() {
    // Nothing to do?
  }

  // Implement ProjectComponent

  public void projectOpened() {
    if (project != null && !project.isDefault()) {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectOpened(" + project.getName() + ')');

      dailyStripPanel = new DailyStripPanel(project);

      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      final ToolWindow toolWindow = manager.registerToolWindow(
          DilbertDailyStripPlugin.TOOL_WINDOW_ID, false, ToolWindowAnchor.BOTTOM, project, true);
      final Content content =
          ContentFactory.SERVICE.getInstance().createContent(dailyStripPanel, null, true);

      toolWindow.getContentManager().addContent(content);
      toolWindow.setIcon(ICON_SMALL);
      dailyStripPanel.initialise();
    } else {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectOpened(null or default project)");
    }
  }

  public void projectClosed() {
    if (project != null && !project.isDefault()) {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectClosed(" + project.getName() + ')');

      if (dailyStripPanel != null) {
        dailyStripPanel.dispose();
      }

      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      manager.unregisterToolWindow(DilbertDailyStripPlugin.TOOL_WINDOW_ID);
    } else {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectClosed(null or default project)");
    }
  }
}
