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
package org.codebrewer.idea.dilbert.util;

import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * A utility class that provides version information about the plugin.
 * </p>
 * <p>
 * The information comprises four numerical components:
 * <ul>
 * <li>major number</li>
 * <li>minor number</li>
 * <li>revision number</li>
 * <li>build number</li>
 * </ul>
 * and a build date.
 * </p>
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class VersionInfo
{
  private static final String BUILD_DATE_KEY = "build.date";
  private static final String BUILD_NUMBER_KEY = "build.number";
  private static final String BUILD_VERSION_MAJOR_KEY = "build.version.major";
  private static final String BUILD_VERSION_MINOR_KEY = "build.version.minor";
  private static final String BUILD_VERSION_REVISION_KEY = "build.version.revision";

  private static final Map BUILD_PROPERTIES_MAP = new HashMap();

  private static final Logger LOGGER = Logger.getInstance(VersionInfo.class.getName());

  private static final String BUILD_PROPERTIES_FILE =
      "/org/codebrewer/idea/dilbert/build/build.properties";

  private static final Properties BUILD_PROPERTIES = new Properties();

  static {
    BUILD_PROPERTIES_MAP.put(BUILD_VERSION_MAJOR_KEY, null);
    BUILD_PROPERTIES_MAP.put(BUILD_VERSION_MINOR_KEY, null);
    BUILD_PROPERTIES_MAP.put(BUILD_VERSION_REVISION_KEY, null);
    BUILD_PROPERTIES_MAP.put(BUILD_NUMBER_KEY, null);

    loadBuildInfo();
  }

  /**
   * Gets the date the plugin was built, in a short format.
   *
   * @return the date the plugin was built.
   */
  public static String getBuildDate()
  {
    final String buildDate = (String) BUILD_PROPERTIES_MAP.get(BUILD_DATE_KEY);
    return buildDate;
  }

  /**
   * Gets the plugin's build number (the fourth component of the version
   * number).
   *
   * @return the plugin's build number.
   */
  public static int getBuildNumber()
  {
    final Integer buildNumber = (Integer) BUILD_PROPERTIES_MAP.get(BUILD_NUMBER_KEY);
    return buildNumber.intValue();
  }

  /**
   * Gets the plugin's major version number (the first component of the version
   * number).
   *
   * @return the plugin's major version number.
   */
  public static int getVersionMajor()
  {
    final Integer versionMajor = (Integer) BUILD_PROPERTIES_MAP.get(BUILD_VERSION_MAJOR_KEY);
    return versionMajor.intValue();
  }

  /**
   * Gets the plugin's minor version number (the second component of the version
   * number).
   *
   * @return the plugin's minor version number.
   */
  public static int getVersionMinor()
  {
    final Integer versionMinor = (Integer) BUILD_PROPERTIES_MAP.get(BUILD_VERSION_MINOR_KEY);
    return versionMinor.intValue();
  }

  /**
   * Gets the plugin's revision number (the third component of the version
   * number).
   *
   * @return the plugin's revision number.
   */
  public static int getVersionRevision()
  {
    final Integer versionRevision = (Integer) BUILD_PROPERTIES_MAP.get(BUILD_VERSION_REVISION_KEY);
    return versionRevision.intValue();
  }

  /**
   * Gets the plugin's version information in an aggregate form by combining
   * major, minor, revision and build numbers into a period-separated string.
   *
   * @return an aggregate form of the plugin's version information.
   */
  public static String getVersionString()
  {
    final char dot = '.';
    final StringBuffer sb = new StringBuffer()
        .append(getVersionMajor())
        .append(dot)
        .append(getVersionMinor())
        .append(dot)
        .append(getVersionRevision())
        .append(dot)
        .append(getBuildNumber());
    return sb.toString();
  }

  private static void loadBuildInfo()
  {
    final String defaultValue = "";
    final Integer missingValue = new Integer(0);
    final InputStream is = VersionInfo.class.getResourceAsStream(BUILD_PROPERTIES_FILE);

    if (is != null) {
      try {
        BUILD_PROPERTIES.load(is);

        // Iterate over the 4 integers that comprise the version number.
        //
        final Iterator iterator = BUILD_PROPERTIES_MAP.keySet().iterator();
        while (iterator.hasNext()) {
          final String key = (String) iterator.next();
          final Integer value = getInteger(BUILD_PROPERTIES.getProperty(key, defaultValue));
          if (value != null) {
            BUILD_PROPERTIES_MAP.put(key, value);
          }
          else {
            LOGGER.error("No value for key " + key);
            BUILD_PROPERTIES_MAP.put(key, missingValue);
          }
        }

        //  Get the build date.
        //
        final String buildDate = BUILD_PROPERTIES.getProperty(BUILD_DATE_KEY);
        if (buildDate != null) {
          BUILD_PROPERTIES_MAP.put(BUILD_DATE_KEY, buildDate);
        }
        else {
          LOGGER.error("No value for key " + BUILD_DATE_KEY);
          BUILD_PROPERTIES_MAP.put(BUILD_DATE_KEY, missingValue);
        }
      }
      catch (IOException e) {
        LOGGER.error("Couldn't load build information", e);
      }
    }
  }

  private static Integer getInteger(final String s)
  {
    Integer value = null;

    try {
      value = new Integer(Integer.parseInt(s));
    }
    catch (NumberFormatException nfe) {
      LOGGER.error("Couldn't parse int from " + s);
    }

    return value;
  }

  private VersionInfo()
  {
    // Private constructor to prevent instantiation (never called, even from
    // this class).
  }
}