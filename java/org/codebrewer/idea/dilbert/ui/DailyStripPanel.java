/*
 *  Copyright 2005, 2006 Mark Scott
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
package org.codebrewer.idea.dilbert.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import org.codebrewer.idea.dilbert.DilbertDailyStrip;
import org.codebrewer.idea.dilbert.DilbertDailyStripPlugin;
import org.codebrewer.idea.util.l10n.ResourceBundleManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
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
  private static final Icon REFRESH_ICON = IconLoader.getIcon("/vcs/refresh.png");
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  private final DilbertDailyStripPlugin dailyStripPlugin;
  private DilbertDailyStrip dailyStrip;
  private JLabel stripLabel;

  /**
   * Creates a panel that can be used by an IDEA ToolWindow to show a Dilbert
   * daily strip cartoon.
   *
   * @param dilbertDailyStripPlugin a non-null {@link DilbertDailyStripPlugin}
   *                                that gives this panel access to the plugin's
   *                                settings and functionality.
   * @throws NullPointerException if <code>dilbertDailyStripPlugin</code> is
   *                              <code>null</code>.
   */
  public DailyStripPanel(final DilbertDailyStripPlugin dilbertDailyStripPlugin)
  {
    LOGGER.debug("DailyStripPanel()");

    if (dilbertDailyStripPlugin == null) {
      LOGGER.error("null DilbertDailyStripPlugin passed to constructor");
    }

    dailyStripPlugin = dilbertDailyStripPlugin;
    setLayout(new BorderLayout());
    build();
  }

  private void build()
  {
    add(createToolbarView(), BorderLayout.WEST);
    add(createStripView(), BorderLayout.CENTER);
  }

  private JComponent createToolbarView()
  {
    final DefaultActionGroup dag = new DefaultActionGroup();
    dag.add(new RefreshDailyStripAction());
    dag.add(new HelpAction());
    dag.add(new AboutAction());

    // Add a button to intentionally generate an exception (so that the error
    // handler can be tested)
    //
    if (Boolean.valueOf(System.getProperty("org.codebrewer.idea.dilbert.ShowErrorGenerationButton")).booleanValue()) {
      dag.add(new ErrorAction());
    }

    final ActionManager actionManager = ActionManager.getInstance();
    final ActionToolbar actionToolbar =
        actionManager.createActionToolbar("DilbertDailyStripToolbar", dag, false);
    final JPanel actionsPanel = new JPanel(new BorderLayout());
    actionsPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

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
    stripPanel.setLayout(new BoxLayout(stripPanel, BoxLayout.X_AXIS));
    stripPanel.add(stripLabel);

    // Put the cartoon in a scrollpane so it can be viewed even when
    // the containing ToolWindow is small
    //
    final JScrollPane scroller = new JScrollPane(stripPanel);
    scroller.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    scroller.getVerticalScrollBar().setUnitIncrement(4);

    return scroller;
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
      aboutWindow.setLocationRelativeTo(DailyStripPanel.this.getTopLevelAncestor());
      aboutWindow.setVisible(true);
      aboutWindow.requestFocus();
    }
  }

  /**
   * An action that generates an uncaught exception, to test the plug-in's error
   * report submitter.
   */
  private final class ErrorAction extends AnAction
  {
    private ErrorAction()
    {
      super(ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.error.tooltip"),
          ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.error.statusbartext"),
          ERROR_ICON);
      final int modifiers = SystemInfo.isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
      final KeyStroke keyStroke =
          KeyStroke.getKeyStroke(KeyEvent.VK_E, modifiers);
      final CustomShortcutSet shortcutSet = new CustomShortcutSet(keyStroke);
      registerCustomShortcutSet(shortcutSet, DailyStripPanel.this);
    }

    public void actionPerformed(final AnActionEvent e)
    {
      throw new NullPointerException("Testing error reporting");
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

  /**
   * An action that refreshes the cartoon strip displayed.
   */
  private final class RefreshDailyStripAction extends AnAction
  {
    private RefreshDailyStripAction()
    {
      super(ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.refresh.tooltip"),
          ResourceBundleManager.getLocalizedString(DailyStripPanel.class, "button.refresh.statusbartext"),
          REFRESH_ICON);
      final int modifiers = SystemInfo.isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
      final KeyStroke keyStroke =
          KeyStroke.getKeyStroke(KeyEvent.VK_R, modifiers);
      final CustomShortcutSet shortcutSet = new CustomShortcutSet(keyStroke);
      registerCustomShortcutSet(shortcutSet, DailyStripPanel.this);
    }

    public void actionPerformed(final AnActionEvent e)
    {
      // The strip is only fetched if the user has acknowledged the plug-in's
      // disclaimer
      //
      if (dailyStripPlugin.isDisclaimerAcknowledged()) {
        fetchDailyStrip();
      }
      else {
        // The user hasn't acknowledged the plug-in's disclaimer so notify them
        // and ask if they'd like to open the plug-in settings dialog now
        //
        final DisclaimerNotAcknowledgedDialog dlg = new DisclaimerNotAcknowledgedDialog();
        dlg.show();
        if (dlg.getExitCode() == DialogWrapper.OK_EXIT_CODE && dlg.isOpenSettings()) {
          ShowSettingsUtil.getInstance().editConfigurable(
              DailyStripPanel.this.getTopLevelAncestor(), DailyStripPanel.this.dailyStripPlugin);

          // If the disclaimer has now been acknowledged then fetch the strip
          //
          if (dailyStripPlugin.isDisclaimerAcknowledged()) {
            fetchDailyStrip();
          }
        }
      }
    }

    private void fetchDailyStrip()
    {
      // Request an update with the latest strip, taking account of the
      // modification time of the currently displayed strip (if any)
      //
      try {
        if (DailyStripPanel.this.dailyStrip != null) {
          dailyStripPlugin.fetchDailyStrip(DailyStripPanel.this,
              DailyStripPanel.this.dailyStrip.getLastModified());
        }
        else {
          dailyStripPlugin.fetchDailyStrip(DailyStripPanel.this);
        }
      }
      catch (IOException ioe) {
        LOGGER.debug("Couldn't get Dilbert daily strip" + ioe.getMessage());
        dailyStrip = DilbertDailyStrip.MISSING_STRIP;
        setDailyStrip(dailyStrip);
      }
    }
  }

  // Implement DailyStripPresenter

  public DilbertDailyStrip getDilbertDailyStrip()
  {
    return dailyStrip;
  }

  public void setDailyStrip(final DilbertDailyStrip newDailyStrip)
  {
    if (newDailyStrip == null) {
      stripLabel.setIcon(null);
      stripLabel.setToolTipText(null);
    }
    else {
      stripLabel.setIcon(newDailyStrip.getIcon());
        if (newDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP)) {
            stripLabel.setToolTipText(ResourceBundleManager.getLocalizedString(
                    DailyStripPanel.class, "panel.icon-missing.tooltip"));
        } else {
            final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            final long lastModified = newDailyStrip.getLastModified();
            stripLabel.setToolTipText(dateFormat.format(new Date(lastModified)));
        }
    }

    dailyStrip = newDailyStrip;
  }
}