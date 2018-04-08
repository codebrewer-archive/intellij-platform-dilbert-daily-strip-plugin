/*
 * Copyright 2006, 2009, 2013, 2018 Mark Scott
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

import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to aid in loading resource bundles and finding resources for
 * keys.
 *
 * @author Mark Scott
 */
public final class ResourceBundleManager {
  /**
   * Gets a localized mnemonic for a given key.  If the string associated with
   * the key contains more than one character then the first character is
   * returned.
   *
   * @param clazz the class for which a mnemonic resource is required.
   * @param key the key identifying the required resource.
   *
   * @return the localized mnemonic char for the given key or <code>'\0'</code>
   * if no such resource is found.
   *
   * @throws NullPointerException if either parameter is <code>null</code>.
   */
  public static char getLocalizedMnemonic(final Class clazz, final String key) {
    final String localizedString = getLocalizedString(clazz, key);
    final char mnemonic;

    if (localizedString.length() > 0) {
      mnemonic = localizedString.charAt(0);
    } else {
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
   * such resource is found.
   *
   * @throws NullPointerException if either parameter is <code>null</code>.
   * @throws MissingResourceException if no resource is found.
   */
  @NotNull
  public static String getLocalizedString(final Class clazz, final String key) {
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(key);

    return getResourceBundle(clazz).getString(key);
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
   * resource bundle could be loaded.
   *
   * @throws NullPointerException if the given class is <code>null</code>.
   * @throws MissingResourceException if the requested bundle is not found.
   */
  @NotNull
  public static ResourceBundle getResourceBundle(final Class clazz) {
    Objects.requireNonNull(clazz);

    return ResourceBundle.getBundle(clazz.getName() + "Resources");
  }

  /**
   * Private constructor that prevents instantiation from outside this class
   * and is never called.
   */
  private ResourceBundleManager() {
  }
}
