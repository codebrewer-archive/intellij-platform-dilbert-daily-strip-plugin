/*
 *  Copyright 2005, 2018, 2023 Mark Scott
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

package org.codebrewer.intellijplatform.plugin.dilbert.util;

import com.intellij.openapi.diagnostic.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
 */
public final class VersionInfo {
  private static final String BUILD_DATE_KEY = "build.date";
  private static final String BUILD_NUMBER_KEY = "build.number";
  private static final String BUILD_VERSION_MAJOR_KEY = "build.version.major";
  private static final String BUILD_VERSION_MINOR_KEY = "build.version.minor";
  private static final String BUILD_VERSION_REVISION_KEY = "build.version.revision";

  private static final Map<String, Object> BUILD_PROPERTIES_MAP = new HashMap<>();

  private static final Logger LOGGER = Logger.getInstance(VersionInfo.class.getName());

  private static final String BUILD_PROPERTIES_FILE =
      "/org/codebrewer/intellijplatform/plugin/dilbert/build/build.properties";

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
  public static String getBuildDate() {
    return (String) BUILD_PROPERTIES_MAP.get(BUILD_DATE_KEY);
  }

  /**
   * Gets the plugin's build number (the fourth component of the version
   * number).
   *
   * @return the plugin's build number.
   */
  public static int getBuildNumber() {
    return (Integer) BUILD_PROPERTIES_MAP.get(BUILD_NUMBER_KEY);
  }

  /**
   * Gets the plugin's major version number (the first component of the version
   * number).
   *
   * @return the plugin's major version number.
   */
  public static int getVersionMajor() {
    return (Integer) BUILD_PROPERTIES_MAP.get(BUILD_VERSION_MAJOR_KEY);
  }

  /**
   * Gets the plugin's minor version number (the second component of the version
   * number).
   *
   * @return the plugin's minor version number.
   */
  public static int getVersionMinor() {
    return (Integer) BUILD_PROPERTIES_MAP.get(BUILD_VERSION_MINOR_KEY);
  }

  /**
   * Gets the plugin's revision number (the third component of the version
   * number).
   *
   * @return the plugin's revision number.
   */
  public static int getVersionRevision() {
    return (Integer) BUILD_PROPERTIES_MAP.get(BUILD_VERSION_REVISION_KEY);
  }

  private static void loadBuildInfo() {
    final InputStream is = VersionInfo.class.getResourceAsStream(BUILD_PROPERTIES_FILE);

    if (is != null) {
      try {
        BUILD_PROPERTIES.load(is);
        final String defaultValue = "";
        final int missingValue = 0;

        // Iterate over the 4 integers that comprise the version number.
        //
        for (String key : BUILD_PROPERTIES_MAP.keySet()) {
          final Integer value =
              getIntegerOrDefaultValue(
                  BUILD_PROPERTIES.getProperty(key, defaultValue)
              );
          BUILD_PROPERTIES_MAP.put(key, value);
        }

        //  Get the build date.
        //
        final String buildDate = BUILD_PROPERTIES.getProperty(BUILD_DATE_KEY);
        if (buildDate != null) {
          BUILD_PROPERTIES_MAP.put(BUILD_DATE_KEY, buildDate);
        } else {
          LOGGER.error("No value for key " + BUILD_DATE_KEY);
          BUILD_PROPERTIES_MAP.put(BUILD_DATE_KEY, missingValue);
        }
      } catch (IOException e) {
        LOGGER.error("Couldn't load build information", e);
      }
    }
  }

  private static int getIntegerOrDefaultValue(final String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException nfe) {
      LOGGER.error("Couldn't parse int from " + s);
      return 0;
    }
  }

  private VersionInfo() {
    // Private constructor to prevent instantiation (never called, even from
    // this class).
  }
}
