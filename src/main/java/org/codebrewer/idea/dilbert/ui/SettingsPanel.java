/*
 * Copyright 2005, 2007, 2018 Mark Scott
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

import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.codebrewer.idea.dilbert.settings.ApplicationSettings;
import org.codebrewer.idea.dilbert.settings.Modifiable;
import org.codebrewer.idea.dilbert.settings.UnattendedDownloadSettings;
import org.codebrewer.intellijplatform.plugin.util.l10n.ResourceBundleManager;

/**
 * A panel that provides a UI for the user to configure the plugin's
 * application-level settings.
 *
 * @author Mark Scott
 */
public final class SettingsPanel extends JPanel//BasicSettingsPanel
{
  /**
   * Used to acknowledge the plugin's disclaimer.
   */
  private JCheckBox disclaimerCheckBox;

  /**
   * Contains those controls that should only be available if the plugin's
   * disclaimer has been acknowledged (so that their enabled state can be
   * toggled with the disclaimer acknowledgment).
   */
  private final List<Modifiable> dependentControls;

  private final UnattendedDownloadSettings unattendedDownloadSettings;
  private final UnattendedDownloadSettingsPanel unattendedDownloadSettingsPanel;

  /**
   * Constructs a settings panel that has its UI intialized from the given
   * <code>ApplicationSettings</code> object.
   *
   * @param settings the state from which to intialize the new panel.
   *
   * @throws IllegalArgumentException if <code>settings</code> is
   * <code>null</code>.
   */
  public SettingsPanel(final ApplicationSettings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("ApplicationSettings cannot be null");
    }

    unattendedDownloadSettings = settings.getUnattendedDownloadSettings();

    if (unattendedDownloadSettings == null) {
      throw new IllegalArgumentException("UnattendedDownloadSettings cannot be null");
    }

    unattendedDownloadSettingsPanel =
        (UnattendedDownloadSettingsPanel) unattendedDownloadSettings.createComponent();

    dependentControls = new ArrayList<>();
    build();
  }

  /**
   * Gets the current state of the user's settings.  The object returned
   * reflects the UI state at the time this method is called.
   *
   * @return the user's current settings as reflected by the UI.
   */
  public ApplicationSettings getDisplayedSettings() {
    final UnattendedDownloadSettings unattendedDownloadSettingsNow =
        unattendedDownloadSettingsPanel.getDisplayedSettings();

    return new ApplicationSettings(disclaimerCheckBox.isSelected(), unattendedDownloadSettingsNow);
  }

  /**
   * Indicates whether or not the user's current settings differ from the given
   * settings.  This method is called to see if the user has modified the saved
   * settings since the settings UI was displayed.
   *
   * @param settings the last settings saved by the user.
   *
   * @return <code>true</code> if the saved settings differ from the settings
   * represented by the current state of the configuration UI, otherwise
   * <code>false</code>.
   */
  public boolean isModified(final ApplicationSettings settings) {
    return !getDisplayedSettings().equals(settings);
  }

  /**
   * Re-initializes the settings panel's UI from the given
   * <code>ApplicationSettings</code> object.
   *
   * @param settings the state from which to reintialize the new panel.
   *
   * @throws IllegalArgumentException if <code>settings</code> is
   * <code>null</code> or if the .
   */
  public void setSettings(final ApplicationSettings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("null settings");
    }

    final boolean disclaimerAcknowledged = settings.isDisclaimerAcknowledged();
    disclaimerCheckBox.setSelected(disclaimerAcknowledged);
    setDependentsEnabled(disclaimerAcknowledged);
  }

  private void build() {
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    final JPanel generalSettingsPanel = new JPanel(new BorderLayout());
    final Border border =
        new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), JBUI.Borders.empty(3, 5));
    final String panelTitle =
        ResourceBundleManager
            .getLocalizedString(SettingsPanel.class, BasicSettingsPanel.PANEL_TITLE_KEY);
    generalSettingsPanel.setBorder(new TitledBorder(border, panelTitle));

    disclaimerCheckBox =
        new JCheckBox(
            ResourceBundleManager.getLocalizedString(
                SettingsPanel.class, "button.disclaimer.text"), false);
    final char disclaimerMnemonic =
        ResourceBundleManager.getLocalizedMnemonic(
            SettingsPanel.class, "button.disclaimer.text.mnemonic");
    disclaimerCheckBox.setMnemonic(disclaimerMnemonic);
    disclaimerCheckBox.addItemListener(
        itemEvent -> setDependentsEnabled(disclaimerCheckBox.isSelected()));
    generalSettingsPanel.add(disclaimerCheckBox, BorderLayout.WEST);
    generalSettingsPanel.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, generalSettingsPanel.getPreferredSize().height));
    add(generalSettingsPanel);

    if (unattendedDownloadSettingsPanel != null) {
      unattendedDownloadSettingsPanel.setMaximumSize(
          new Dimension(
              Integer.MAX_VALUE, unattendedDownloadSettingsPanel.getPreferredSize().height));
      add(unattendedDownloadSettingsPanel);
      dependentControls.add(unattendedDownloadSettings);
    }
  }

  private void setDependentsEnabled(final boolean state) {
    for (final Modifiable dependentControl : dependentControls) {
      dependentControl.setConfigurationUIEnabled(state);
    }
  }
}
