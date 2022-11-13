/*
 * Copyright 2005, 2007, 2008, 2010, 2018, 2022 Mark Scott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codebrewer.intellijplatform.plugin.dilbert.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.MouseShortcut;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStrip;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStripPluginService;
import org.codebrewer.intellijplatform.plugin.dilbert.strategy.DailyStripProvider;
import org.codebrewer.intellijplatform.plugin.util.l10n.ResourceBundleManager;
import org.jetbrains.annotations.NotNull;

/**
 * A panel that can be used by an IDEA ToolWindow to show a Dilbert daily strip
 * cartoon.
 *
 * @author Mark Scott
 */
public final class DailyStripPanel extends JPanel implements DailyStripPresenter {
  private static final Icon ABOUT_ICON =
      IconLoader.getIcon("/compiler/information.png", DailyStripPanel.class);

  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPluginService.class.getName());
  private final Project project;
  private DilbertDailyStrip dailyStrip;
  private JLabel stripLabel;
  private DailyStripProvider dailyStripProvider;

  /**
   * Creates a panel that can be used by an IDEA ToolWindow to show a Dilbert
   * daily strip cartoon.
   *
   * @throws NullPointerException if <code>dilbertDailyStripPlugin</code> is
   * <code>null</code>.
   */
  public DailyStripPanel(final Project project) {
    LOGGER.debug("DailyStripPanel()");

    this.project = project;
    dailyStrip = DilbertDailyStrip.MISSING_STRIP;
  }

  private void build() {
    setLayout(new BorderLayout());
    add(createToolbarView(), BorderLayout.NORTH);
    add(createStripView(), BorderLayout.CENTER);
  }

  private JComponent createToolbarView() {
    final ActionManager actionManager = ActionManager.getInstance();
    final DefaultActionGroup dag = new DefaultActionGroup();

    dag.add(new AboutAction());

    final ActionToolbar actionToolbar =
        actionManager.createActionToolbar("DilbertDailyStripToolbar", dag, true);
    final JComponent actionToolbarComponent = actionToolbar.getComponent();
    final JPanel actionsPanel = new JPanel();

    actionToolbar.setReservePlaceAutoPopupIcon(false);
    actionsPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
    actionsPanel.add(actionToolbarComponent);
    actionsPanel.add(createControlPanelView());

    return actionsPanel;
  }

  private JComponent createStripView() {
    // The cartoon strip is displayed as the Icon in a JLabel (positioned at the top)
    //
    stripLabel = new JLabel();
    stripLabel.setAlignmentY(0.0f);

    // The cartoon's JLabel is displayed left-aligned in a JPanel
    // having a small border
    //
    final JPanel stripPanel = new JPanel();
    stripPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    stripPanel.setLayout(new BoxLayout(stripPanel, BoxLayout.LINE_AXIS));
    stripPanel.add(stripLabel);

    // Put the cartoon in a scrollpane, so it can be viewed even when
    // the containing ToolWindow is small
    //
    final JScrollPane scroller = new JBScrollPane(stripPanel);
    scroller.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    scroller.getVerticalScrollBar().setUnitIncrement(4);

    // Install an action on the scrollpane to open dilbert.com in a
    // web browser
    //
    new LaunchWebsiteAction().install(scroller);

    return scroller;
  }

  private JComponent createControlPanelView() {
    final DailyStripProvider currentDailyStripProvider = getCurrentDailyStripProvider();
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    final JComponent controlPanel = currentDailyStripProvider.getControlPanel();
    if (controlPanel != null) {
      panel.add(controlPanel);
    }

    return panel;
  }

  private DailyStripProvider getCurrentDailyStripProvider() {
    return dailyStripProvider;
  }

  /**
   * An action that displays an 'about' dialog for the plug-in.
   */
  private final class AboutAction extends AnAction implements DumbAware {
    private AboutAction() {
      super(ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.about.tooltip"),
          ResourceBundleManager
              .getLocalizedString(DailyStripPanel.class, "button.about.statusbartext"),
          ABOUT_ICON);

      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      final ToolWindow toolWindow =manager.getToolWindow(DilbertDailyStripPluginService.TOOL_WINDOW_ID);

      if (toolWindow != null) {
        final int modifiers =
            SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
        final KeyStroke keyStroke =
            KeyStroke.getKeyStroke(KeyEvent.VK_I, modifiers);
        final CustomShortcutSet shortcutSet = new CustomShortcutSet(keyStroke);
        registerCustomShortcutSet(shortcutSet, toolWindow.getComponent());
      }
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      final AboutWindow aboutWindow = AboutWindow.getInstance();
      aboutWindow.setLocationRelativeTo(getTopLevelAncestor());
      aboutWindow.setVisible(true);
      aboutWindow.requestFocus();
    }
  }

  /**
   * An action that opens the Dilbert website in a web browser.
   */
  private final class LaunchWebsiteAction extends AnAction implements DumbAware {
    private LaunchWebsiteAction() {
      super();

      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      final ToolWindow toolWindow = manager.getToolWindow(DilbertDailyStripPluginService.TOOL_WINDOW_ID);

      if (toolWindow != null) {
        final int modifiers = SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
        final MouseShortcut mouseShortcut = new MouseShortcut(MouseEvent.BUTTON1, modifiers, 1);
        final CustomShortcutSet shortcutSet = new CustomShortcutSet(mouseShortcut);
        setShortcutSet(shortcutSet);
      }
    }

    private void install(JComponent component) {
      registerCustomShortcutSet(getShortcutSet(), component);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      BrowserUtil.browse(DilbertDailyStrip.DILBERT_DOT_COM_URL);
    }
  }

  // Implement DailyStripPresenter

  public void dispose() {
    dailyStripProvider.stop();
  }

  public void initialise() {
    final DilbertDailyStripPluginService dilbertPluginService =
        ApplicationManager.getApplication().getService(DilbertDailyStripPluginService.class);

    dailyStripProvider = dilbertPluginService.getDailyStripProviders(this)[0];
    build();
    dailyStripProvider.start();
  }

  public DilbertDailyStrip getDilbertDailyStrip() {
    return dailyStrip;
  }

  public void setDailyStrip(final DilbertDailyStrip newDailyStrip) {
    SwingUtilities.invokeLater(() -> {
      if (newDailyStrip == null) {
        stripLabel.setIcon(null);
        stripLabel.setText(null);
        stripLabel.setToolTipText(null);
      } else {
        stripLabel.setIcon(newDailyStrip.getIcon());
        stripLabel.setText(null);

        if (newDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP)) {
          stripLabel.setToolTipText(ResourceBundleManager.getLocalizedString(
              DailyStripPanel.class, "panel.icon-missing.tooltip"));
        } else {
          final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
          final long lastModified = newDailyStrip.getRetrievalTime();

          stripLabel.setToolTipText(dateFormat.format(new Date(lastModified)));
        }
      }
    });

    dailyStrip = newDailyStrip;
  }

  public Project getProject() {
    return project;
  }
}
