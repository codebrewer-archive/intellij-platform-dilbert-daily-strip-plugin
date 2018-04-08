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

package org.codebrewer.idea.dilbert.ui;

import com.intellij.util.ui.JBUI;
import java.awt.Color;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.codebrewer.intellijplatform.plugin.util.l10n.ResourceBundleManager;

/**
 * @author Mark Scott
 */
class BasicSettingsPanel extends JPanel {
  static final String PANEL_TITLE_KEY = "panel.title";
  private static final String TITLED_BORDER_TITLE_COLOR_KEY = "TitledBorder.titleColor";
  private static final String LABEL_DISABLED_FOREGROUND_KEY = "Label.disabledForeground";

  private static final Color LABEL_DISABLED_FOREGROUND_COLOR =
      UIManager.getColor(LABEL_DISABLED_FOREGROUND_KEY);
  private static final Color TITLED_BORDER_TITLE_COLOR =
      UIManager.getColor(TITLED_BORDER_TITLE_COLOR_KEY);

  private static final String NULL_MESSAGE_KEY_MESSAGE = "null messageKey";

  void build() {
    setLayout(new GridBagLayout());

    final Border border =
        new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), JBUI.Borders.empty(3, 5));
    final String panelTitle = getPanelTitle();
    setBorder(new TitledBorder(border, panelTitle));
  }

  char getLocalizedMnemonic(final String messageKey) {
    if (messageKey == null) {
      throw new IllegalArgumentException(NULL_MESSAGE_KEY_MESSAGE);
    }

    return ResourceBundleManager.getLocalizedMnemonic(getClass(), messageKey);
  }

  String getLocalizedString(final String messageKey) {
    if (messageKey == null) {
      throw new IllegalArgumentException(NULL_MESSAGE_KEY_MESSAGE);
    }

    return ResourceBundleManager.getLocalizedString(getClass(), messageKey);
  }

  private String getPanelTitle() {
    return ResourceBundleManager.getLocalizedString(getClass(), PANEL_TITLE_KEY);
  }

  void setBorderEnabled(final boolean enabled) {
    final TitledBorder currentBorder = (TitledBorder) getBorder();
    final Border innerBorder = currentBorder.getBorder();
    final TitledBorder newBorder = new TitledBorder(innerBorder, currentBorder.getTitle());
    newBorder.setTitleColor(enabled ? TITLED_BORDER_TITLE_COLOR : LABEL_DISABLED_FOREGROUND_COLOR);
    setBorder(newBorder);
  }
}
