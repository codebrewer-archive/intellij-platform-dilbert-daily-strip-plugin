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
package org.codebrewer.idea.dilbert;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.net.HttpConfigurable;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codebrewer.idea.dilbert.settings.ApplicationSettings;
import org.codebrewer.idea.dilbert.ui.DailyStripPanel;
import org.codebrewer.idea.dilbert.ui.DailyStripPresenter;
import org.codebrewer.idea.dilbert.ui.SettingsPanel;
import org.codebrewer.idea.dilbert.util.ResourceBundleManager;
import org.codebrewer.idea.dilbert.util.VersionInfo;
import org.jdom.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MailDateFormat;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * <p>
 * An implementation of a plugin that fetches and displays the current daily
 * cartoon strip from the dilbert.com website.
 * </p>
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class DilbertDailyStripPluginImpl implements DilbertDailyStripPlugin
{
  /**
   * Icon for use on the plugin settings page.
   */
  private static final Icon ICON_LARGE = IconLoader.getIcon("/dilbert32x32.png");

  /**
   * Icon for use on the toolwindow button and IDEA 'welcome screen'.
   */
  private static final Icon ICON_SMALL = IconLoader.getIcon("/dilbert16x16.png");

  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * Holds every strip panel (one for each open project).
   */
  private static final Map PROJECT_TO_DAILY_STRIP_PRESENTER_MAP = new HashMap();

  /**
   * HTTP header used to specify that an SC_NOT_MODIFIED response should be sent
   * if a requested resource has not changed since a specified time.
   */
  private static final String HTTP_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

  /**
   * HTTP header used to return the time at which a resource was last modified.
   */
  private static final String HTTP_HEADER_LAST_MODIFIED = "Last-Modified";

  /**
   * The time at the Unix epoch.
   */
  private static final int EPOCH = 0;

  /**
   * The time at the Unix epoch, in a format suitable for use in an HTTP
   * If-Modified-Since header.
   */
  private static final String EPOCH_STRING = "Thu, 01 Jan 1970 00:00:00 GMT";

  /**
   * The latest daily strip fetched from www.dilbert.com.
   */
  private DilbertDailyStrip latestDailyStrip;

  /**
   * Application-level settings for the plug-in, shared by all open projects.
   */
  private ApplicationSettings settings;

  /**
   * A UI component that permits our <code>ApplicationSettings</code> to be
   * edited.
   */
  private SettingsPanel settingsPanel;

  /**
   * A map that tracks the toolwindow manager for a project, so that toolwindows
   * can be registered and unregistered when a project opens and closes.
   */
  private final Map project2ToolWindowManagerMap = new HashMap();

  /**
   * Constructs a plugin implementation.
   */
  public DilbertDailyStripPluginImpl()
  {
    LOGGER.debug("DilbertDailyStripApplicationImpl()");
    LOGGER.info("Dilbert Daily Strip Plug-in, version " + VersionInfo.getVersionString() +
        ", built " + VersionInfo.getBuildDate());
    settings = new ApplicationSettings();
  }

  private static boolean isSupportedContentType(final Header contentTypeHeader)
  {
    boolean isSupported = false;

    if (contentTypeHeader != null) {
      final String contentType = contentTypeHeader.getValue();
      isSupported = "image/gif".equalsIgnoreCase(contentType) ||
          "image/jpeg".equalsIgnoreCase(contentType) ||
          "image/jpg".equalsIgnoreCase(contentType) ||
          "image/jpe".equalsIgnoreCase(contentType);
    }

    return isSupported;
  }

  /**
   * Indicates whether the plugin should automatically download the current
   * daily strip when a project is opened or should only do so when the user
   * explicitly requests it.
   *
   * @return <code>true</code> if the current daily strip should be fetched as
   *         soon as a project is opened, <code>false</code> if not.
   */
  private boolean isLoadStripOnStartup()
  {
    return settings.isLoadStripOnStartup();
  }

  /**
   * Indicates whether refreshing the plugin's toolwindow in one project should
   * cause all other open projects to be refreshed as well.
   *
   * @return <code>true</code> if updating one project should cause all other
   *         projects to be updated, <code>false</code> if not.
   */
  private boolean isRefreshAllOpenProjects()
  {
    return settings.isRefreshAllOpenProjects();
  }

  // Implement DilbertDailyStripPlugin

  public void fetchDailyStrip(final DailyStripPresenter presenter) throws IOException
  {
    fetchDailyStrip(presenter, EPOCH);
  }

  public void fetchDailyStrip(final DailyStripPresenter presenter, final long ifModifiedSince)
      throws IOException
  {
    // Use JavaMail's MailDateFormat class to format a date suitable for use in
    // an HTTP If-Modified-Since header - the format isn't quite right at the
    // end (because it includes an offset from GHT in parentheses), but that
    // appears not to matter
    //
    final MailDateFormat mdf = new MailDateFormat();
    mdf.getCalendar().setTimeZone(TimeZone.getTimeZone("GMT"));
    final String formattedIfModifiedSince = mdf.format(new Date(ifModifiedSince));
    LOGGER.info(formattedIfModifiedSince);
    final GetMethod homepageURLMethod = new GetMethod(DilbertDailyStrip.DILBERT_DOT_COM_URL);
    homepageURLMethod.addRequestHeader(new Header(HTTP_HEADER_IF_MODIFIED_SINCE, formattedIfModifiedSince));
    DilbertDailyStrip strip = null;

    try {
      final HttpClient client = new HttpClient();
      final HttpConfigurable httpConfigurable =
          (HttpConfigurable) ApplicationManager.getApplication().getComponent("HttpConfigurable");

      // Use IDEA's HTTP proxy settings, if present (this isn't part of the
      // OpenAPI so may break)
      //
      if (httpConfigurable != null && httpConfigurable.USE_HTTP_PROXY) {
        final HostConfiguration hostConfiguration = client.getHostConfiguration();
        hostConfiguration.setProxy(httpConfigurable.PROXY_HOST, httpConfigurable.PROXY_PORT);
        client.setHostConfiguration(hostConfiguration);

        if (httpConfigurable.PROXY_AUTHENTICATION) {
          // Use IDEA's support for showing the proxy credentials dialog (if
          // necessary), but note that <http://www.jetbrains.net/jira/browse/IDEABKL-1509>
          // may bite
          //
          httpConfigurable.prepareURL(DilbertDailyStrip.DILBERT_DOT_COM_URL);
          homepageURLMethod.setDoAuthentication(true);
          client.getState().setProxyCredentials(AuthScope.ANY,
              new UsernamePasswordCredentials(httpConfigurable.PROXY_LOGIN, httpConfigurable.getPlainProxyPassword()));
        }
        else {
          homepageURLMethod.setDoAuthentication(false);
        }
      }

      int statusCode = client.executeMethod(homepageURLMethod);

      // If we get SC_OK (code 200) then we found the homepage OK and can
      // proceed
      //
      if (statusCode == HttpStatus.SC_OK) {
        // Find out when the homepage was last modified, assuming the epoch if
        // the header that provides the information is missing.
        //
        final Header responseHeader = homepageURLMethod.getResponseHeader(HTTP_HEADER_LAST_MODIFIED);
        final String lastModifiedStr;
        if (responseHeader != null) {
          lastModifiedStr = responseHeader.getValue();
        }
        else {
          lastModifiedStr = EPOCH_STRING;
        }

        // Use JavaMail's MailDateFormat class again to parse the modification
        // date - we store the modification date of the *homepage*, not that of
        // the strip itself
        //
        final Date lastModifiedDate = new MailDateFormat().parse(lastModifiedStr, new ParsePosition(0));
        final long lastModified = lastModifiedDate == null ? Long.MIN_VALUE : lastModifiedDate.getTime();

        // Now read the homepage body line by line, looking for a match on the
        // regex that identifies the daily strip image
        //
        BufferedReader br = null;
        try {
          br = new BufferedReader(new InputStreamReader(homepageURLMethod.getResponseBodyAsStream()));
          final Pattern p = Pattern.compile(DilbertDailyStrip.IMAGE_URL_REGEX);
          String line;
          do {
            line = br.readLine();
            if (line != null) {
              final Matcher m = p.matcher(line);
              if (m.matches()) {
                final String spec = m.group(1);

                // Try to form the URL for the daily strip image from the homepage
                // URL and the path to the daily strip image
                //
                final HttpURL stripURL = new HttpURL(new HttpURL(DilbertDailyStrip.DILBERT_DOT_COM_URL), spec);
                LOGGER.debug(stripURL.toString());
                final GetMethod stripURLMethod = new GetMethod(stripURL.getURI());

                // Try to retrieve the daily strip image
                //
                try {
                  // Use IDEA's HTTP proxy settings, if present (this isn't part of the
                  // OpenAPI so may break)
                  //
                  if (httpConfigurable != null && httpConfigurable.USE_HTTP_PROXY) {
                    stripURLMethod.setDoAuthentication(httpConfigurable.PROXY_AUTHENTICATION);
                  }

                  statusCode = client.executeMethod(stripURLMethod);
                  if (statusCode == HttpStatus.SC_OK) {
                    final Header contentTypeHeader = stripURLMethod.getResponseHeader("Content-Type");
                    if (isSupportedContentType(contentTypeHeader)) {
                      final byte[] responseBody = stripURLMethod.getResponseBody();
                      final Icon icon = new ImageIcon(responseBody);
                      strip = new DilbertDailyStrip(icon, stripURL.getURI(), lastModified);
                      latestDailyStrip = strip;
                    }
                    else {
                      throw new IOException("Unexpected content type for daily strip image: " + contentTypeHeader);
                    }
                  }
                }
                finally {
                  stripURLMethod.releaseConnection();
                }
                break;
              }
            }
          }
          while (line != null);
        }
        finally {
          if (br != null) {
            br.close();
          }
        }
      }
    }
    finally {
      homepageURLMethod.releaseConnection();
    }

    // Only update the presenter(s) if the strip is non-null (i.e. modified
    // since the last-modified time given
    //
    if (isRefreshAllOpenProjects()) {
      final Set mapEntries = PROJECT_TO_DAILY_STRIP_PRESENTER_MAP.entrySet();
      final Iterator iterator = mapEntries.iterator();
      while (iterator.hasNext()) {
        final Map.Entry entry = (Map.Entry) iterator.next();
        final DailyStripPresenter aPresenter = (DailyStripPresenter) entry.getValue();
        final DilbertDailyStrip currentlyPresentedDailyStrip = aPresenter.getDilbertDailyStrip();
        if (currentlyPresentedDailyStrip == null ||
            currentlyPresentedDailyStrip != null &&
            currentlyPresentedDailyStrip.getLastModified() < latestDailyStrip.getLastModified()) {
          aPresenter.setDailyStrip(latestDailyStrip);
        }
      }
    }
    else {
      if (strip != null) {
        presenter.setDailyStrip(strip);
      }
    }
  }

  public boolean isDisclaimerAcknowledged()
  {
    return settings.isDisclaimerAcknowledged();
  }

  // Implement BaseComponent

  public void disposeComponent()
  {
    LOGGER.debug("DilbertDailyStripApplicationImpl.disposeComponent()");
  }

  public String getComponentName()
  {
    return DilbertDailyStripPlugin.class.getName();
  }

  public void initComponent()
  {
    LOGGER.debug("DilbertDailyStripApplicationImpl.initComponent()");
    ProjectManager.getInstance().addProjectManagerListener(this);
  }

  // Implement Configurable

  public String getDisplayName()
  {
    return ResourceBundleManager.getResourceBundle(
        DilbertDailyStripPlugin.class).getString("plugin.name.configuration");
  }

  public Icon getIcon()
  {
    return ICON_LARGE;
  }

  public String getHelpTopic()
  {
    // Return the value of the "target" attribute of a helpset "tocitem" element
    //
    return "settings";
  }

  // Implement JDOMExternalizable

  public void readExternal(final Element element)
  {
    LOGGER.debug("DilbertDailyStripApplicationImpl.readExternal(" + (element == null ? "null" : element.getName()) + ')');
    settings.readExternal(element);
  }

  public void writeExternal(final Element element)
  {
    LOGGER.debug("DilbertDailyStripApplicationImpl.writeExternal(" + (element == null ? "null" : element.getName()) + ')');
    settings.writeExternal(element);
  }

  // Implement NamedJDOMExternalizable

  /**
   * Return the root part of the name of the file to which the plugin will save
   * its configuration data.  The value returned will have the suffix .xml
   * appended to form the full filename, and the file will be created in the
   * ${idea.config.path}/options/ directory.
   *
   * @return the root part of the configuration settings filename for the
   *         plugin.
   */
  public String getExternalFileName()
  {
    return "dilbert.plugin";
  }

  // Implement ProjectManagerListener

  public void projectOpened(final Project project)
  {
    // IDEA can open a null and default project that we should ignore
    //
    if (project != null && !project.isDefault()) {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectOpened(" + project.getName() + ')');

      final DailyStripPanel dailyStripPanel = new DailyStripPanel(this);
      PROJECT_TO_DAILY_STRIP_PRESENTER_MAP.put(project, dailyStripPanel);
      final ToolWindowManager manager = ToolWindowManager.getInstance(project);
      final ToolWindow toolWindow = manager.registerToolWindow(TOOL_WINDOW_ID, dailyStripPanel, ToolWindowAnchor.BOTTOM);
      if (toolWindow != null) {
        project2ToolWindowManagerMap.put(project, manager);
        toolWindow.setIcon(ICON_SMALL);
        if (isDisclaimerAcknowledged() && isLoadStripOnStartup()) {
          try {
            fetchDailyStrip(dailyStripPanel);
          }
          catch (IOException e) {
            LOGGER.debug("IOException fetching daily strip: " + e.getMessage());
          }
        }
      }
      else {
        LOGGER.info("Got null instead of a ToolWindow!");
      }
    }
    else {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectOpened(null or default project)");
    }
  }

  public boolean canCloseProject(final Project project)
  {
    if (project != null && !project.isDefault()) {
      LOGGER.debug("DilbertDailyStripApplicationImpl.canCloseProject(" + project.getName() + ')');
    }
    else {
      LOGGER.debug("DilbertDailyStripApplicationImpl.canCloseProject(null or default project)");
    }

    return true;
  }

  public void projectClosed(final Project project)
  {
    if (project != null && !project.isDefault()) {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectClosed(" + project.getName() + ')');
      PROJECT_TO_DAILY_STRIP_PRESENTER_MAP.remove(project);
      final ToolWindowManager manager = (ToolWindowManager) project2ToolWindowManagerMap.remove(project);
      if (manager != null) {
        manager.unregisterToolWindow(TOOL_WINDOW_ID);
      }
    }
    else {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectClosed(null or default project)");
    }
  }

  public void projectClosing(final Project project)
  {
    if (project != null && !project.isDefault()) {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectClosing(" + project.getName() + ')');
    }
    else {
      LOGGER.debug("DilbertDailyStripApplicationImpl.projectClosing(null or default project)");
    }
  }

  // Implement UnnamedConfigurable

  public JComponent createComponent()
  {
    LOGGER.debug("DilbertDailyStripApplicationImpl.createComponent()");
    if (settingsPanel == null) {
      settingsPanel = new SettingsPanel(settings);
    }

    return settingsPanel;
  }

  public boolean isModified()
  {
    boolean isModified = false;

    if (settingsPanel != null) {
      isModified = settingsPanel.isModified(settings);
    }

    return isModified;
  }

  public void apply()
  {
    if (settingsPanel != null) {
      settings = settingsPanel.getCurrentSettings();
    }
  }

  public void reset()
  {
    if (settingsPanel != null) {
      settingsPanel.setSettings(settings);
    }
  }

  public void disposeUIResources()
  {
  }
}