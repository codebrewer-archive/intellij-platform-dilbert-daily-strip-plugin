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

import com.intellij.openapi.util.IconLoader;
import org.codebrewer.idea.dilbert.util.VersionInfo;
import org.codebrewer.idea.util.l10n.ResourceBundleManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A singelton class that implements an 'about' screen.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
final class AboutWindow extends JDialog
{
  private static final Icon ICON_ABOUT = IconLoader.getIcon("/dilbert-splash.png");

  private static final AboutWindow INSTANCE = new AboutWindow();

  private static final int WIDTH = 400;

  private static final int HEIGHT = 300;

  private static final int BOTTOM_BORDER = 0;

  private static final int LEFT_BORDER = 17;

  private static final int RIGHT_BORDER = 0;

  private static final int TOP_BORDER = 10;

  private static final int VERSION_STRING_LENGTH_GUESS = 128;

  /**
   * Returns the singleton instance of this class.
   *
   * @return the singleton instance of this class.
   */
  public static AboutWindow getInstance()
  {
    return INSTANCE;
  }

  private AboutWindow()
  {
    build();
  }

  private void build()
  {
    setModal(true);
    setSize(WIDTH, HEIGHT);
    setUndecorated(true);

    final JPanel contentPane = (JPanel) getContentPane();

    // Decorate the window and give version information
    //
    contentPane.setBackground(Color.WHITE);
    contentPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

    // A line giving the plugin version number
    //
    final String versionLine = MessageFormat.format(
        ResourceBundleManager.getLocalizedString(AboutWindow.class, "about.template.version"),
        new Object[]{
            new Integer(VersionInfo.getVersionMajor()),
            new Integer(VersionInfo.getVersionMinor()),
            new Integer(VersionInfo.getVersionRevision()) });

    // A line giving the plugin build number
    //
    final String buildLine = MessageFormat.format(
        ResourceBundleManager.getLocalizedString(AboutWindow.class, "about.template.build"),
        new Object[]{ new Integer(VersionInfo.getBuildNumber()) });

    // A line giving the plugin build date
    //
    final String builtLine = MessageFormat.format(
        ResourceBundleManager.getLocalizedString(AboutWindow.class, "about.template.built"),
        new Object[]{ VersionInfo.getBuildDate() });

    // A label that uses html to format the lines of info
    //
    final StringBuffer sb = new StringBuffer(VERSION_STRING_LENGTH_GUESS);
    sb.append("<html><b>")
        .append(versionLine)
        .append("</b><br>")
        .append(buildLine)
        .append("<br>")
        .append(builtLine)
        .append("</html>");
    final String versionString = sb.toString();
    final JLabel versionLabel = new JLabel(versionString, JLabel.LEADING);
    versionLabel.setBorder(BorderFactory.createEmptyBorder(TOP_BORDER, LEFT_BORDER, BOTTOM_BORDER, RIGHT_BORDER));
    final Font labelFont = new Font("Sans-Serif", Font.PLAIN, 10);
    versionLabel.setFont(labelFont);
    contentPane.add(versionLabel, BorderLayout.CENTER);

    // A label containing copyright information
    //
    final String copyrightString = ResourceBundleManager.getLocalizedString(AboutWindow.class, "about.copyright");
    final JLabel copyrightLabel = new JLabel(copyrightString, JLabel.TRAILING);
    copyrightLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
    copyrightLabel.setFont(labelFont);
    contentPane.add(copyrightLabel, BorderLayout.SOUTH);

    // Add an image to the window's glass pane
    //
    final JPanel glassPanel = (JPanel) getGlassPane();
    final JLabel splashLabel = new JLabel(ICON_ABOUT);
    splashLabel.setBorder(BorderFactory.createEmptyBorder());
    splashLabel.setVerticalAlignment(SwingConstants.TOP);

    glassPanel.add(splashLabel);
    glassPanel.addMouseListener(new MouseAdapter()
    {
      // Clicking on the panel hides it...
      //
      public void mouseClicked(final MouseEvent mouseEvent)
      {
        setVisible(false);
      }

      // ...as does the mouse leaving it...
      //
      public void mouseExited(final MouseEvent mouseEvent)
      {
        setVisible(false);
      }
    });
    glassPanel.setVisible(true);
    setGlassPane(glassPanel);

    // ...or losing focus...
    //
    addFocusListener(new FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
        setVisible(false);
      }
    });

    // ...or typing Esc while while we have focus
    //
    addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent keyEvent)
      {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
          setVisible(false);
        }
      }
    });
  }
}