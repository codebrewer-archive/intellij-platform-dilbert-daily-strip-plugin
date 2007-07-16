/*
 *  Copyright 2007 Mark Scott
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
package org.codebrewer.idea.dilbert.strategy;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import org.codebrewer.idea.dilbert.DailyStripEvent;
import org.codebrewer.idea.dilbert.DailyStripListener;
import org.codebrewer.idea.dilbert.DilbertDailyStrip;
import org.codebrewer.idea.dilbert.DilbertDailyStripPlugin;
import org.codebrewer.idea.dilbert.ui.DailyStripPresenter;
import org.codebrewer.idea.dilbert.ui.DisclaimerNotAcknowledgedDialog;
import org.codebrewer.idea.util.l10n.ResourceBundleManager;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;

/**
 * A strategy that keeps its client updated with the current daily strip.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public class CurrentDailyStripProvider extends LocalizableDailyStripProvider implements DailyStripListener
{
  private static final String PROVIDER_ID = "currentStrip";
  private static final Icon REFRESH_ICON = IconLoader.getIcon("/vcs/refresh.png");

  private final JComponent controlPanel;
  private JProgressBar progressBar;

  public CurrentDailyStripProvider(final DailyStripPresenter presenter)
  {
    super(presenter);
    controlPanel = buildControlPanel();
  }

  private JComponent buildControlPanel()
  {
    final DefaultActionGroup dag = new DefaultActionGroup();
    dag.add(new RefreshDailyStripAction());

    final ActionManager actionManager = ActionManager.getInstance();
    final ActionToolbar actionToolbar = actionManager.createActionToolbar("strategy.currentStrip", dag, true);
    actionToolbar.getComponent().setMaximumSize(actionToolbar.getComponent().getPreferredSize());
    final JPanel actionsPanel = new JPanel();
    actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.LINE_AXIS));
    actionsPanel.add(actionToolbar.getComponent());
    actionsPanel.add(Box.createHorizontalStrut(2));
    final Dimension preferredSize = actionsPanel.getPreferredSize();
    actionsPanel.setPreferredSize(new Dimension(150, preferredSize.height));

    return actionsPanel;
  }

  private void setProgressBarVisible(final boolean visible)
  {
    if (visible && progressBar == null) {
      progressBar = new JProgressBar();
      progressBar.setIndeterminate(true);
      final Dimension preferredSize = progressBar.getPreferredSize();
      progressBar.setPreferredSize(new Dimension(100, preferredSize.height));
      progressBar.setMaximumSize(new Dimension(100, preferredSize.height));
      controlPanel.add(progressBar);
    }
    else if (progressBar != null) {
      controlPanel.remove(progressBar);
      progressBar = null;
    }

    controlPanel.validate();
    controlPanel.repaint();
  }

  // Implement/override AbstractDailyStripProvider

  protected void doPause()
  {
    // This strategy doesn't really have any state so pausing is effectively the
    // same as stopping
    //
    doStop();
  }

  protected void doResume()
  {
    // This strategy doesn't really have any state so resuming is effectively
    // the same as starting
    //
    doStart();
  }

  protected void doStart()
  {
    final DilbertDailyStripPlugin dilbertPlugin =
        (DilbertDailyStripPlugin) ApplicationManager.getApplication().getComponent(DilbertDailyStripPlugin.class);
    dilbertPlugin.addDailyStripListener(this);
    final DilbertDailyStrip dilbertDailyStrip = dilbertPlugin.getCachedDailyStrip();

    if (dilbertDailyStrip == null && dilbertPlugin.isDisclaimerAcknowledged()) {
      setProgressBarVisible(true);
      dilbertPlugin.fetchDailyStrip();
    }
    else {
      getDailyStripPresenter().setDailyStrip(dilbertDailyStrip);
    }
  }

  protected void doStop()
  {
    final DilbertDailyStripPlugin dilbertPlugin =
        (DilbertDailyStripPlugin) ApplicationManager.getApplication().getComponent(DilbertDailyStripPlugin.class);
    dilbertPlugin.removeDailyStripListener(this);
    setProgressBarVisible(false);
  }

  public JComponent getControlPanel()
  {
    return controlPanel;
  }

  public String getId()
  {
    return PROVIDER_ID;
  }

  // Implement DailyStripListener

  public void dailyStripUpdated(final DailyStripEvent e)
  {
    setProgressBarVisible(false);

    if (e == null) {
      LOGGER.info("Listener received null DailyStripEvent!"); // NON-NLS
    }
    else {
      final DilbertDailyStrip newDailyStrip = e.getDilbertDailyStrip();

      if (newDailyStrip != null) {
        final DilbertDailyStrip currentDailyStrip = getDailyStripPresenter().getDilbertDailyStrip();

        if (currentDailyStrip == null ||
            newDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP) ||
            newDailyStrip.getLastModified() > currentDailyStrip.getLastModified()) {
          getDailyStripPresenter().setDailyStrip(newDailyStrip);
        }
      }
    }
  }

  /**
   * An action that refreshes the cartoon strip displayed.
   */
  private final class RefreshDailyStripAction extends AnAction
  {
    private static final long EPOCH = 0L;

    private final DilbertDailyStripPlugin dilbertPlugin;

    private RefreshDailyStripAction()
    {
      super(ResourceBundleManager.getLocalizedString(CurrentDailyStripProvider.class, "button.refresh.tooltip"),
          ResourceBundleManager.getLocalizedString(CurrentDailyStripProvider.class, "button.refresh.statusbartext"),
          REFRESH_ICON);
      dilbertPlugin =
          (DilbertDailyStripPlugin) ApplicationManager.getApplication().getComponent(DilbertDailyStripPlugin.class);

      if (getDailyStripPresenter() instanceof JComponent) {
        final int modifiers = SystemInfo.isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
        final KeyStroke keyStroke =
            KeyStroke.getKeyStroke(KeyEvent.VK_R, modifiers);
        final CustomShortcutSet shortcutSet = new CustomShortcutSet(keyStroke);
        registerCustomShortcutSet(shortcutSet, (JComponent) getDailyStripPresenter());
      }
    }

    public void actionPerformed(final AnActionEvent e)
    {
      // The strip is only fetched if the user has acknowledged the plug-in's
      // disclaimer
      //
      if (dilbertPlugin.isDisclaimerAcknowledged()) {
        fetchDailyStrip();
      }
      // The user hasn't acknowledged the plug-in's disclaimer so notify them
      // and ask if they'd like to open the plug-in settings dialog now
      //
      else {
        final DisclaimerNotAcknowledgedDialog dlg = new DisclaimerNotAcknowledgedDialog();
        dlg.show();
        if (dlg.getExitCode() == DialogWrapper.OK_EXIT_CODE && dlg.isOpenSettings()) {
          ShowSettingsUtil.getInstance().editConfigurable(getControlPanel().getTopLevelAncestor(), dilbertPlugin);

          // If the disclaimer has now been acknowledged then fetch the strip
          //
          if (dilbertPlugin.isDisclaimerAcknowledged()) {
            fetchDailyStrip();
          }
        }
      }
    }

    private void fetchDailyStrip()
    {
      setProgressBarVisible(true);
      getDailyStripPresenter().setDailyStrip(null);

      // Request an update with the latest strip, taking account of the
      // modification time of the currently displayed strip (if any)
      //
      if (getDailyStripPresenter().getDilbertDailyStrip() != null) {
        dilbertPlugin.fetchDailyStrip(getDailyStripPresenter().getDilbertDailyStrip().getLastModified());
      }
      else {
        dilbertPlugin.fetchDailyStrip(EPOCH);
      }
    }
  }
}
