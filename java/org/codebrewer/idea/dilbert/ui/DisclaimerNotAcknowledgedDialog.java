/*
 *  Copyright 2005 Mark Scott
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

import com.intellij.openapi.ui.DialogWrapper;
import org.codebrewer.idea.dilbert.util.ResourceBundleManager;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A <code>DialogWrapper</code> that can be used to inform the user that the
 * plugin's disclaimer has not been acknowledged.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
final class DisclaimerNotAcknowledgedDialog extends DialogWrapper
{
  private final JCheckBox isOpenSettings;

  DisclaimerNotAcknowledgedDialog()
  {
    super(false);
    final String label = ResourceBundleManager.getLocalizedString(
        DisclaimerNotAcknowledgedDialog.class, "dialog.acknowledge.checkbox.label");
    isOpenSettings = new JCheckBox(label, true);
    final String title = ResourceBundleManager.getLocalizedString(
        DisclaimerNotAcknowledgedDialog.class, "dialog.acknowledge.title");
    setTitle(title);
    getContentPane().setLayout(new BorderLayout());
    init();
  }

  boolean isOpenSettings()
  {
    return isOpenSettings.isSelected();
  }

  protected JComponent createCenterPanel()
  {
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    final String label = ResourceBundleManager.getLocalizedString(
        DisclaimerNotAcknowledgedDialog.class, "dialog.acknowledge.label");
    panel.add(new JLabel(label));
    panel.add(isOpenSettings);

    return panel;
  }

  public String toString()
  {
    return getClass().getName() + "[isOpenSettings=" + isOpenSettings + "]";
  }
}