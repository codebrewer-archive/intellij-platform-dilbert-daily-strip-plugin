/*
 *  Copyright 2007, 2018, 2022 Mark Scott
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

package org.codebrewer.intellijplatform.plugin.dilbert.ui;

import com.intellij.util.ui.JBUI;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import org.codebrewer.intellijplatform.plugin.dilbert.settings.UnattendedDownloadSettings;

/**
 * A panel that provides a UI for the user to configure the settings related to
 * unattended downloading.
 *
 * @author Mark Scott
 */
public final class UnattendedDownloadSettingsPanel extends BasicSettingsPanel {
  /**
   * Pattern used in a spinner control for choosing a time.
   */
  private static final String DATE_EDITOR_FORMAT_PATTERN = "HH:mm";

  /**
   * Indicates whether strips should be fetched automatically.
   */
  private JCheckBox fetchStripsAutomaticallyCheckbox;

  /**
   * Resets the spinner controls to their default values.
   */
  private JButton useDefaultFetchProfile;

  /**
   * Describes the default download settings
   */
  private JLabel defaultFetchProfileDescription;

  /**
   * Used to enter the maximum number of fetch attempts that should be made.
   */
  private JSpinner localDownloadTimeSpinner;

  /**
   * Used to enter the maximum number of fetch attempts that should be made.
   */
  private JSpinner maxFetchAttemptsSpinner;

  /**
   * Used to enter the maximum number of fetch attempts that should be made.
   */
  private JSpinner fetchIntervalSpinner;

  /**
   * A list for holding the controls used to configure custom fetch settings,
   * to permit easy dis/enabling.
   */
  private final List<JComponent> customFetchSettingsControls;

  /**
   * Constructs a settings panel that has its UI initialized from the given
   * <code>ApplicationSettings</code> object.
   *
   * @param settings the state from which to initialize the new panel.
   *
   * @throws IllegalArgumentException if settings is <code>null</code>.
   */
  public UnattendedDownloadSettingsPanel(final UnattendedDownloadSettings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("null settings");
    }

    customFetchSettingsControls = new ArrayList<>(10);
    build();
    addListeners();
    setDisplayedSettings(settings);
  }

  protected void build() {
    super.build();

    // Labeled checkbox for toggling unattended daily strip fetching
    //
    fetchStripsAutomaticallyCheckbox =
        new JCheckBox(getLocalizedString("button.fetchdailystrips.text"));
    final char fetchDailyStripsMnemonic =
        getLocalizedMnemonic("button.fetchdailystrips.text.mnemonic");
    fetchStripsAutomaticallyCheckbox.setMnemonic(fetchDailyStripsMnemonic);
    final Insets emptyInsets = JBUI.emptyInsets();
    add(fetchStripsAutomaticallyCheckbox,
        new GridBagConstraints(
            0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            emptyInsets, 0, 0));

    final Insets nestedInsets = JBUI.insetsLeft(new JCheckBox().getPreferredSize().width - 1);

    // Label for the timezone offset chooser widget
    //
    final JLabel timezoneOffsetChooserLabel =
        new JLabel(getLocalizedString("label.spinner.tzoffset.text"));
    timezoneOffsetChooserLabel
        .setDisplayedMnemonic(getLocalizedMnemonic("label.spinner.tzoffset.text.mnemonic"));
    timezoneOffsetChooserLabel.setAlignmentX(RIGHT_ALIGNMENT);
    customFetchSettingsControls.add(timezoneOffsetChooserLabel);
    final Insets spinnerInsets = JBUI.insets(5, 5, 0, 0);
    add(timezoneOffsetChooserLabel,
        new GridBagConstraints(
            0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            nestedInsets, 0, 0));

    // Spinner that selects the offset from Dilbert's timezone
    //
    localDownloadTimeSpinner = new JSpinner(new SpinnerHalfHourModel());
    final JSpinner.DefaultEditor localDownloadTimeSpinnerEditor =
        new JSpinner.DateEditor(localDownloadTimeSpinner, DATE_EDITOR_FORMAT_PATTERN);
    localDownloadTimeSpinner.setEditor(localDownloadTimeSpinnerEditor);
    localDownloadTimeSpinnerEditor.getTextField().setColumns(5);
    localDownloadTimeSpinnerEditor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
    timezoneOffsetChooserLabel.setLabelFor(localDownloadTimeSpinner);
    customFetchSettingsControls.add(localDownloadTimeSpinner);
    add(localDownloadTimeSpinner,
        new GridBagConstraints(
            2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            spinnerInsets, 0, 0));

    // Label for the max fetch attempts chooser widget
    //
    final JLabel maxFetchAttemptsSpinnerLabel =
        new JLabel(getLocalizedString("label.spinner.maxfetchattempts.text"));
    maxFetchAttemptsSpinnerLabel.setDisplayedMnemonic(
        getLocalizedMnemonic("label.spinner.maxfetchattempts.text.mnemonic"));
    maxFetchAttemptsSpinnerLabel.setAlignmentX(RIGHT_ALIGNMENT);
    customFetchSettingsControls.add(maxFetchAttemptsSpinnerLabel);
    add(maxFetchAttemptsSpinnerLabel,
        new GridBagConstraints(
            0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            nestedInsets, 0, 0));

    // Spinner that limits the number of fetch attempts to be made
    //
    maxFetchAttemptsSpinner = new JSpinner(new SpinnerNumberModel(
        Integer.valueOf(UnattendedDownloadSettings.DEFAULT_MAX_FETCH_ATTEMPTS),
        Integer.valueOf(UnattendedDownloadSettings.MIN_MAX_FETCH_ATTEMPTS),
        Integer.valueOf(UnattendedDownloadSettings.MAX_MAX_FETCH_ATTEMPTS),
        Integer.valueOf(1)));
    ((JSpinner.DefaultEditor) maxFetchAttemptsSpinner.getEditor()).getTextField().setColumns(5);
    maxFetchAttemptsSpinnerLabel.setLabelFor(maxFetchAttemptsSpinner);
    customFetchSettingsControls.add(maxFetchAttemptsSpinner);
    add(maxFetchAttemptsSpinner,
        new GridBagConstraints(
            2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            spinnerInsets, 0, 0));

    // Label for the fetch attempt interval chooser widget
    //
    final JLabel fetchIntervalSpinnerLabel =
        new JLabel(getLocalizedString("label.spinner.fetchinterval.text"));
    fetchIntervalSpinnerLabel
        .setDisplayedMnemonic(getLocalizedMnemonic("label.spinner.fetchinterval.text.mnemonic"));
    fetchIntervalSpinnerLabel.setAlignmentX(RIGHT_ALIGNMENT);
    customFetchSettingsControls.add(fetchIntervalSpinnerLabel);
    add(fetchIntervalSpinnerLabel,
        new GridBagConstraints(
            0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            nestedInsets, 0, 0));

    // Spinner that limits the number of fetch attempts to be made
    //
    fetchIntervalSpinner = new JSpinner(new SpinnerNumberModel(
        Integer.valueOf(UnattendedDownloadSettings.DEFAULT_FETCH_INTERVAL),
        Integer.valueOf(UnattendedDownloadSettings.MIN_FETCH_INTERVAL),
        Integer.valueOf(UnattendedDownloadSettings.MAX_FETCH_INTERVAL),
        Integer.valueOf(1)));
    ((JSpinner.DefaultEditor) fetchIntervalSpinner.getEditor()).getTextField().setColumns(5);
    fetchIntervalSpinnerLabel.setLabelFor(fetchIntervalSpinner);
    customFetchSettingsControls.add(fetchIntervalSpinner);
    add(fetchIntervalSpinner,
        new GridBagConstraints(
            2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            spinnerInsets, 0, 0));

    // A button for resetting other controls to their default values
    //
    useDefaultFetchProfile =
        new JButton(getLocalizedString("button.fetchdailystrips.default.text"));
    useDefaultFetchProfile
        .setMnemonic(getLocalizedMnemonic("button.fetchdailystrips.default.text.mnemonic"));
    final Insets resetInsets =
        JBUI.insets(5, nestedInsets.left, nestedInsets.bottom, nestedInsets.right);
    add(useDefaultFetchProfile,
        new GridBagConstraints(
            0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            resetInsets, 0, 0));

    // A label describing the default settings
    //
    defaultFetchProfileDescription = new JLabel(
        MessageFormat.format(getLocalizedString("label.fetchdailystrips.default.description"),
            UnattendedDownloadSettings.DEFAULT_DOWNLOAD_SETTINGS.getMaxFetchAttempts(),
            UnattendedDownloadSettings.DEFAULT_DOWNLOAD_SETTINGS.getFetchInterval()));
    add(defaultFetchProfileDescription,
        new GridBagConstraints(
            1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
            spinnerInsets, 0, 0));
  }

  private void addListeners() {
    // Only enable controls if the user has opted to fetch strips automatically
    //
    fetchStripsAutomaticallyCheckbox.addItemListener(itemEvent -> setComponentsEnabled());

    // React to the user pressing the Defaults button
    //
    useDefaultFetchProfile.addActionListener(
        e -> setDisplayedSettings(UnattendedDownloadSettings.DEFAULT_DOWNLOAD_SETTINGS));

    // Listen for changes in any of the spinner controls so that the reset
    // button and description label can be enabled/disabled as appropriate
    //
    final ChangeListener spinnerListener = e -> setComponentsEnabled();

    localDownloadTimeSpinner.addChangeListener(spinnerListener);
    maxFetchAttemptsSpinner.addChangeListener(spinnerListener);
    fetchIntervalSpinner.addChangeListener(spinnerListener);
  }

  /**
   * Sets the enabled state of the UI's controls to reflect the current settings
   * state.
   */
  private void setComponentsEnabled() {
    final boolean panelEnabled = isEnabled();

    if (panelEnabled) {
      fetchStripsAutomaticallyCheckbox.setEnabled(true);
      final UnattendedDownloadSettings currentSettings = getDisplayedSettings();
      final boolean fetchStripsAutomatically = currentSettings.isFetchStripAutomatically();
      final boolean useDefaultSettings =
          UnattendedDownloadSettings.DEFAULT_DOWNLOAD_SETTINGS
              .equalsIgnoreFetchAutomatically(currentSettings);

      setCustomProfileControlsEnabled(fetchStripsAutomatically);
      setDefaultProfileControlsEnabled(fetchStripsAutomatically && !useDefaultSettings);
    } else {
      fetchStripsAutomaticallyCheckbox.setEnabled(false);
      setCustomProfileControlsEnabled(false);
      setDefaultProfileControlsEnabled(false);
    }
  }

  /**
   * Sets the enabled state of the spinner controls (and their labels) that
   * configure the download settings.  These are only available when
   * {@link UnattendedDownloadSettingsPanel#fetchStripsAutomaticallyCheckbox}
   * has been checked.
   *
   * @param enabled <code>true</code> when unattended downloads have been
   * selected, otherwise <code>false</code>.
   */
  private void setCustomProfileControlsEnabled(final boolean enabled) {
    for (JComponent customFetchSettingsControl : customFetchSettingsControls) {
      customFetchSettingsControl.setEnabled(enabled);
    }
  }

  /**
   * Sets the enabled state of {@link #useDefaultFetchProfile} and
   * {@link #defaultFetchProfileDescription}.  These controls are enabled only
   * when the user has configured a non-default download profile.
   *
   * @param enabled <code>true</code> when a non-default download profile has
   * been configured, otherwise <code>false</code>.
   */
  private void setDefaultProfileControlsEnabled(final boolean enabled) {
    useDefaultFetchProfile.setEnabled(enabled);
    defaultFetchProfileDescription.setEnabled(enabled);
  }

  /**
   * Gets the current state of the user's settings.  The object returned
   * reflects the UI state at the time this method is called.
   *
   * @return the user's current settings as reflected by the UI.
   */
  public UnattendedDownloadSettings getDisplayedSettings() {
    final Date localDownloadTimeDate = (Date) localDownloadTimeSpinner.getValue();
    final Calendar cal = Calendar.getInstance();
    cal.setTime(localDownloadTimeDate);
    final int localDownloadTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

    return new UnattendedDownloadSettings(
        fetchStripsAutomaticallyCheckbox.isSelected(),
        localDownloadTime,
        ((Number) maxFetchAttemptsSpinner.getValue()).intValue(),
        ((Number) fetchIntervalSpinner.getValue()).intValue());
  }

  /**
   * Indicates whether the user's current settings differ from the given
   * settings.  This method is called to see if the user has modified the saved
   * settings since the settings UI was displayed.
   *
   * @param settings the settings with which the given settings should be
   * compared.
   *
   * @return <code>true</code> if the given settings differ from the current
   * settings, otherwise <code>false</code>.
   */
  public boolean isModified(final UnattendedDownloadSettings settings) {
    return !getDisplayedSettings().equals(settings);
  }

  /**
   * Reinitializes the settings panel's UI from the given
   * <code>ApplicationSettings</code> object.
   *
   * @param settings the state from which to reinitialize the new panel.
   *
   * @throws IllegalArgumentException if settings is <code>null</code>.
   */
  public void setDisplayedSettings(final UnattendedDownloadSettings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("null settings");
    }

    final boolean fetchStripsAutomatically = settings.isFetchStripAutomatically();
    fetchStripsAutomaticallyCheckbox.setSelected(fetchStripsAutomatically);
    setComponentsEnabled();

    final int localDownloadTime = settings.getLocalDownloadTime();
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MINUTE, localDownloadTime % 60);
    cal.set(Calendar.HOUR_OF_DAY, localDownloadTime / 60);
    localDownloadTimeSpinner.setValue(cal.getTime());
    maxFetchAttemptsSpinner.setValue(settings.getMaxFetchAttempts());
    fetchIntervalSpinner.setValue(settings.getFetchInterval());
  }

  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
    setBorderEnabled(enabled);
    setComponentsEnabled();
  }

  private static class SpinnerHalfHourModel extends SpinnerDateModel {
    private final Calendar currentValue;

    SpinnerHalfHourModel() {
      this(new Date());
    }

    SpinnerHalfHourModel(final Date value) {
      if (value == null) {
        throw new IllegalArgumentException("value cannot be null");
      }

      currentValue = Calendar.getInstance();
      currentValue.setTime(value);
      roundMinutesDown();
    }

    private void roundMinutesDown() {
      int minutes = currentValue.get(Calendar.MINUTE);
      minutes = minutes >= 30 ? 30 : 0;
      currentValue.set(Calendar.MINUTE, minutes);
    }

    public Object getValue() {
      return currentValue.getTime();
    }

    public void setValue(final Object value) {
      if (!(value instanceof Date)) {
        throw new IllegalArgumentException("value must be a Date");
      }

      if (!value.equals(currentValue.getTime())) {
        currentValue.setTime((Date) value);
        roundMinutesDown();
        fireStateChanged();
      }
    }

    public Object getNextValue() {
      final Calendar calendar = Calendar.getInstance();
      calendar.setTime(currentValue.getTime());
      calendar.add(Calendar.MINUTE, 30);
      return calendar.getTime();
    }

    public Object getPreviousValue() {
      final Calendar calendar = Calendar.getInstance();
      calendar.setTime(currentValue.getTime());
      calendar.add(Calendar.MINUTE, -30);
      return calendar.getTime();
    }
  }
}
