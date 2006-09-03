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

import org.codebrewer.idea.dilbert.settings.ApplicationSettings;
import org.codebrewer.idea.util.l10n.ResourceBundleManager;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A panel that provides a UI for the user to configure the plugin's settings.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class SettingsPanel extends JPanel
{
  /**
   * Used to acknowledge the plugin's disclaimer.
   */
  private JCheckBox disclaimerCheckBox;

  /**
   * Indicates whether or not the latest strip be loaded when a project is
   * opened.
   */
  private JCheckBox loadStripOnStartupCheckBox;

  /**
   * Indicates whether or not all open projects be refreshed when a new strip is
   * downloaded.
   */
  private JCheckBox refreshAllOpenProjectsCheckBox;

  /**
   * Contains those controls that should only be available if the plugin's
   * disclaimer has been acknowledged.
   */
  private final List dependentControls;

  /**
   * Constructs a settings panel that has its UI intialized from the given
   * <code>ApplicationSettings</code> object.
   *
   * @param settings the state from which to intialize the new panel.
   * @throws IllegalArgumentException if settings is <code>null</code>.
   */
  public SettingsPanel(final ApplicationSettings settings)
  {
    if (settings == null) {
      throw new IllegalArgumentException("ApplicationSettings cannot be null");
    }

    dependentControls = new ArrayList();
    build();
  }

  /**
   * Gets the current state of the user's settings.  The object returned
   * reflects the UI state at the time this method is called.
   *
   * @return the user's current settings as reflected by the UI.
   */
  public ApplicationSettings getCurrentSettings()
  {
    final ApplicationSettings currentSettings =
        new ApplicationSettings(disclaimerCheckBox.isSelected(),
            loadStripOnStartupCheckBox.isSelected(),
            refreshAllOpenProjectsCheckBox.isSelected());

    return currentSettings;
  }

  /**
   * Indicates whether or not the user's current settings differ from the given
   * settings.  This method is called to see if the user has modified the saved
   * settings since the settings UI was displayed.
   *
   * @param settings the settings with which the given settings should be
   *                 compared.
   * @return <code>true</code> if the given settings differ from the current
   *         settings, otherwise <code>false</code>.
   */
  public boolean isModified(final ApplicationSettings settings)
  {
    return !getCurrentSettings().equals(settings);
  }

  /**
   * Reinitializes the settings panel's UI from the given
   * <code>ApplicationSettings</code> object.
   *
   * @param settings the state from which to reintialize the new panel.
   * @throws IllegalArgumentException if settings is <code>null</code>.
   */
  public void setSettings(final ApplicationSettings settings)
  {
    if (settings == null) {
      throw new IllegalArgumentException("ApplicationSettings cannot be null");
    }

    final boolean disclaimerAcknowledged = settings.isDisclaimerAcknowledged();
    final boolean loadStripOnStartup = settings.isLoadStripOnStartup();
    final boolean refreshAllOpenProjects = settings.isRefreshAllOpenProjects();

    disclaimerCheckBox.setSelected(disclaimerAcknowledged);
    loadStripOnStartupCheckBox.setSelected(loadStripOnStartup);
    refreshAllOpenProjectsCheckBox.setSelected(refreshAllOpenProjects);

    setDependentsEnabled(disclaimerAcknowledged);
  }

  private void build()
  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    final Border border = new CompoundBorder(
        new EtchedBorder(EtchedBorder.LOWERED),
        new EmptyBorder(3, 5, 3, 5));
    setBorder(new TitledBorder(border, ResourceBundleManager.getLocalizedString(
        SettingsPanel.class, "panel.title")));

    disclaimerCheckBox = new JCheckBox(ResourceBundleManager.getLocalizedString(
        SettingsPanel.class, "button.disclaimer.text"), false);
    disclaimerCheckBox.addChangeListener(new ChangeListener()
    {
      public void stateChanged(final ChangeEvent changeEvent)
      {
        setDependentsEnabled(disclaimerCheckBox.isSelected());
      }
    });
    add(disclaimerCheckBox);

    loadStripOnStartupCheckBox = new JCheckBox(ResourceBundleManager.getLocalizedString(
        SettingsPanel.class, "button.loadstriponstartup.text"), false);
    add(loadStripOnStartupCheckBox);
    dependentControls.add(loadStripOnStartupCheckBox);

    refreshAllOpenProjectsCheckBox = new JCheckBox(ResourceBundleManager.getLocalizedString(
        SettingsPanel.class, "button.refreshallopenprojects.text"));
    add(refreshAllOpenProjectsCheckBox);
    dependentControls.add(refreshAllOpenProjectsCheckBox);
  }

  private void setDependentsEnabled(final boolean state)
  {
    for (int i = 0; i < dependentControls.size(); i++) {
      ((Component) dependentControls.get(i)).setEnabled(state);
    }
  }
}