/*
 * Copyright 2006, 2009, 2013 Mark Scott
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
package org.codebrewer.intellijplatform.plugin.util.l10n;

import com.intellij.openapi.diagnostic.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class to aid in loading resource bundles and finding resources for
 * keys.
 *
 * @author Mark Scott
 */
public final class ResourceBundleManager
{
  private static final int RESOURCE_BUNDLE_NAME_MAX_LENGTH = 128;

  /**
   * Logs to IDEA's logfile.
   */
  private static final Logger LOGGER =
      Logger.getInstance(ResourceBundleManager.class.getName());

  /**
   * A map of resource bundle names to the corresponding resource bundles, used
   * to cache loaded bundles for quick retrieval on subsequent requests for the
   * same bundle.
   */
  private static final Map<String, ResourceBundle> NAME_TO_BUNDLE_MAP = new HashMap<String, ResourceBundle>();

  /**
   * Gets a localized mnemonic for a given key.  If the string associated with
   * the key contains more than one character then the first character is
   * returned.
   *
   * @param clazz the class for which a mnemonic resource is required.
   * @param key the key identifying the required resource.
   *
   * @return the localized mnemonic char for the given key or <code>'\0'</code>
   *         if no such resource is found.
   *
   * @throws IllegalArgumentException if either parameter is <code>null</code>.
   */
  public static char getLocalizedMnemonic(final Class clazz, final String key)
  {
    final String localizedString = getLocalizedString(clazz, key);
    final char mnemonic;

    if (localizedString != null && localizedString.length() > 0) {
      mnemonic = localizedString.charAt(0);
    }
    else {
      mnemonic = '\0';
    }

    return mnemonic;
  }

  /**
   * Gets a localized string for a given key.
   *
   * @param clazz the class for which a resource is required.
   * @param key the key identifying the required resource.
   *
   * @return the localized string for the given key or <code>null</code> if no
   *         such resource is found.
   *
   * @throws IllegalArgumentException if either parameter is <code>null</code>.
   */
  public static String getLocalizedString(final Class clazz, final String key)
  {
    if (clazz == null) {
      throw new IllegalArgumentException("null class not allowed");
    }

    if (key == null) {
      throw new IllegalArgumentException("null key not allowed");
    }

    final ResourceBundle resourceBundle = getResourceBundle(clazz);
    String localizedString = null;

    if (resourceBundle != null) {
      localizedString = resourceBundle.getString(key);
    }

    return localizedString;
  }

  /**
   * Loads a resource bundle for the given class using the convention that the
   * resource bundle is located in the same package and has a name formed by
   * appending "Resources.properties" to the class's name.  The method
   * {@link java.util.ResourceBundle#getBundle(String)} is used to load the
   * resource bundle.
   *
   * @param clazz the class for which a resource bundle is to be loaded.
   *
   * @return a resource bundle for the given class or <code>null</code> if no
   *         resource bundle could be loaded.
   *
   * @throws NullPointerException if the given class is <code>null</code>.
   */
  public static ResourceBundle getResourceBundle(final Class clazz)
  {
    if (clazz == null) {
      throw new IllegalArgumentException("Class object cannot be null");
    }

    final String bundleName = new StringBuilder(RESOURCE_BUNDLE_NAME_MAX_LENGTH)
        .append(clazz.getName())
        .append("Resources")
        .toString();
    ResourceBundle bundle = NAME_TO_BUNDLE_MAP.get(bundleName);

    if (bundle == null && !NAME_TO_BUNDLE_MAP.containsKey(bundleName)) {
      LOGGER.debug("Cache miss for ResourceBundle: " + bundleName);
      try {
        bundle = ResourceBundle.getBundle(bundleName);
        LOGGER.debug("Loaded ResourceBundle: " + bundleName);
      }
      catch (MissingResourceException mre) {
        LOGGER.error("Missing ResourceBundle: " + bundleName);
      }
      NAME_TO_BUNDLE_MAP.put(bundleName, bundle);
    }
    else if (bundle != null) {
      LOGGER.debug("Cache hit for ResourceBundle: " + bundleName);
    }

    return bundle;
  }

  /**
   * Private constructor that prevents instantiation from outside this class
   * and is never called.
   */
  private ResourceBundleManager()
  {
  }
}
