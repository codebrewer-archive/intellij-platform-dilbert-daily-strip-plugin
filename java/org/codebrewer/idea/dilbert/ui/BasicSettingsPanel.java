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
package org.codebrewer.idea.dilbert.ui;

import org.codebrewer.idea.util.l10n.ResourceBundleManager;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * @author Mark Scott
 */
class BasicSettingsPanel extends JPanel
{
  protected static final String PANEL_TITLE_KEY = "panel.title";
  protected static final String TITLED_BORDER_TITLE_COLOR_KEY = "TitledBorder.titleColor";
  protected static final String LABEL_DISABLED_FOREGROUND_KEY = "Label.disabledForeground";

  protected static final Color LABEL_DISABLED_FOREGROUND_COLOR = UIManager.getColor(LABEL_DISABLED_FOREGROUND_KEY);
  protected static final Color TITLED_BORDER_TITLE_COLOR = UIManager.getColor(TITLED_BORDER_TITLE_COLOR_KEY);

  private static final String NULL_MESSAGE_KEY_MESSAGE = "null messageKey";

  protected void build()
  {
    setLayout(new GridBagLayout());

    final Border border = new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), new EmptyBorder(3, 5, 3, 5));
    final String panelTitle = getPanelTitle();
    setBorder(new TitledBorder(border, panelTitle));
  }

  protected char getLocalizedMnemonic(final String messageKey)
  {
    if (messageKey == null) {
      throw new IllegalArgumentException(NULL_MESSAGE_KEY_MESSAGE);
    }

    final char result = ResourceBundleManager.getLocalizedMnemonic(getClass(), messageKey);

    return result;
  }

  protected String getLocalizedString(final String messageKey)
  {
    if (messageKey == null) {
      throw new IllegalArgumentException(NULL_MESSAGE_KEY_MESSAGE);
    }

    final String result = ResourceBundleManager.getLocalizedString(getClass(), messageKey);

    return result;
  }

  protected String getPanelTitle()
  {
    return ResourceBundleManager.getLocalizedString(getClass(), PANEL_TITLE_KEY);
  }

  protected void setBorderEnabled(final boolean enabled)
  {
    final TitledBorder currentBorder = (TitledBorder) getBorder();
    final Border innerBorder = currentBorder.getBorder();
    final TitledBorder newBorder = new TitledBorder(innerBorder, currentBorder.getTitle());
    newBorder.setTitleColor(enabled ? TITLED_BORDER_TITLE_COLOR : LABEL_DISABLED_FOREGROUND_COLOR);
    setBorder(newBorder);
  }
}
