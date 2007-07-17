/*
 * Copyright 2005, 2007 Mark Scott
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
package org.codebrewer.idea.dilbert.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import org.codebrewer.idea.dilbert.DilbertDailyStrip;
import org.codebrewer.idea.dilbert.DilbertDailyStripPlugin;
import org.codebrewer.idea.dilbert.strategy.DailyStripProvider;
import org.codebrewer.idea.util.l10n.ResourceBundleManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

/**
 * A panel that can be used by an IDEA ToolWindow to show a Dilbert daily strip
 * cartoon.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class DailyStripPanel extends JPanel implements DailyStripPresenter
{
  private static final Icon ABOUT_ICON = IconLoader.getIcon("/compiler/information.png");
  private static final Icon ERROR_ICON = IconLoader.getIcon("/general/toolWindowDebugger.png");
  private static final Icon HELP_ICON = IconLoader.getIcon("/actions/help.png");

  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());
  private DilbertDailyStrip dailyStrip;
  private JLabel stripLabel;
  private final DailyStripProvider dailyStripProvider;

  /**
   * Creates a panel that can be used by an IDEA ToolWindow to show a Dilbert
   * daily strip cartoon.
   *
   * @throws NullPointerException if <code>dilbertDailyStripPlugin</code> is
   * <code>null</code>.
   */
  public DailyStripPanel()
  {
    LOGGER.debug("DailyStripPanel()");

    final DilbertDailyStripPlugin dilbertPlugin =
        (DilbertDailyStripPlugin) ApplicationManager.getApplication().getComponent(DilbertDailyStripPlugin.class);

    dailyStrip = DilbertDailyStrip.MISSING_STRIP;
    dailyStripProvider = dilbertPlugin.getDailyStripProviders(this)[0];
    build();
    dailyStripProvider.start();
  }

  private void build()
  {
    setLayout(new BorderLayout());
    add(createToolbarView(), BorderLayout.NORTH);
    add(createStripView(), BorderLayout.CENTER);
  }

  private JComponent createToolbarView()
  {
    final ActionManager actionManager = ActionManager.getInstance();
    final DefaultActionGroup dag = new DefaultActionGroup();
    dag.add(new HelpAction());
    dag.add(new AboutAction());

    final ActionToolbar actionToolbar =
        actionManager.createActionToolbar("DilbertDailyStripToolbar", dag, true);

    final JComponent actionToolbarComponent = actionToolbar.getComponent();
    actionToolbarComponent.setMaximumSize(actionToolbarComponent.getPreferredSize());

    final JPanel actionsPanel = new JPanel();

    actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.LINE_AXIS));
    actionsPanel.add(actionToolbarComponent);
    actionsPanel.add(createControlPanelView());
    actionsPanel.add(Box.createHorizontalGlue());

    return actionsPanel;
  }

  private JComponent createStripView()
  {
    // The cartoon strip is displayed as the Icon in a JLabel (positioned at the top)
    //
    stripLabel = new JLabel();
    stripLabel.setAlignmentY(0.0f);

    // The cartoon's JLabel is displayed left-aligned in a JPanel
    // having a white background and a small border
    //
    final JPanel stripPanel = new JPanel();
    stripPanel.setBackground(Color.WHITE);
    stripPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    stripPanel.setLayout(new BoxLayout(stripPanel, BoxLayout.LINE_AXIS));
    stripPanel.add(stripLabel);

    // Put the cartoon in a scrollpane so it can be viewed even when
    // the containing ToolWindow is small
    //
    final JScrollPane scroller = new JScrollPane(stripPanel);
    scroller.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    scroller.getVerticalScrollBar().setUnitIncrement(4);

    return scroller;
  }

  private JComponent createControlPanelView()
  {
    final DailyStripProvider currentDailyStripProvider = getCurrentDailyStripProvider();
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

    final JComponent controlPanel = currentDailyStripProvider.getControlPanel();
    if (controlPanel != null) {
      panel.add(controlPanel);
    }

    return panel;
  }

  private DailyStripProvider getCurrentDailyStripProvider()
  {
    return dailyStripProvider;
  }

  /**
   * An action that displays an 'about' dialog for the plug-in.
   */
  private final class AboutAction extends AnAction
  {
    private AboutAction()
    {
      super(ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.about.tooltip"),
          ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.about.statusbartext"),
          ABOUT_ICON);
      final int modifiers = SystemInfo.isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
      final KeyStroke keyStroke =
          KeyStroke.getKeyStroke(KeyEvent.VK_I, modifiers);
      final CustomShortcutSet shortcutSet = new CustomShortcutSet(keyStroke);
      registerCustomShortcutSet(shortcutSet, DailyStripPanel.this);
    }

    public void actionPerformed(final AnActionEvent e)
    {
      final AboutWindow aboutWindow = AboutWindow.getInstance();
      aboutWindow.setLocationRelativeTo(getTopLevelAncestor());
      aboutWindow.setVisible(true);
      aboutWindow.requestFocus();
    }
  }

  /**
   * An action that opens IDEA's help browser to display help information about
   * the plug-in.
   */
  private final class HelpAction extends AnAction
  {
    private HelpAction()
    {
      super(ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.help.tooltip"),
          ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.help.statusbartext"),
          HELP_ICON);
      final int modifiers = SystemInfo.isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
      final KeyStroke keyStroke =
          KeyStroke.getKeyStroke(KeyEvent.VK_H, modifiers);
      final CustomShortcutSet shortcutSet = new CustomShortcutSet(keyStroke);
      registerCustomShortcutSet(shortcutSet, DailyStripPanel.this);
    }

    public void actionPerformed(final AnActionEvent e)
    {
      // The value of the target attribute of a helpset tocitem element
      //
      HelpManager.getInstance().invokeHelp("main");
    }
  }

  // Implement DailyStripPresenter

  public void dispose()
  {
    dailyStripProvider.stop();
  }

  public DilbertDailyStrip getDilbertDailyStrip()
  {
    return dailyStrip;
  }

  public void setDailyStrip(final DilbertDailyStrip newDailyStrip)
  {
    if (newDailyStrip == null) {
      stripLabel.setIcon(null);
      stripLabel.setText(null);
      stripLabel.setToolTipText(null);
    }
    else {
      stripLabel.setIcon(newDailyStrip.getIcon());
      stripLabel.setText(null);

      if (newDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP)) {
        stripLabel.setToolTipText(ResourceBundleManager.getLocalizedString(
            DailyStripPanel.class, "panel.icon-missing.tooltip"));
      }
      else {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        final long lastModified = newDailyStrip.getLastModified();

        stripLabel.setToolTipText(dateFormat.format(new Date(lastModified)));
      }
    }

    dailyStrip = newDailyStrip;
  }
}
