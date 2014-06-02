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
package org.codebrewer.idea.dilbert.settings;

import com.intellij.openapi.options.UnnamedConfigurable;

/**
 * An interface that allows a configurable object to have its configuration UI
 * enabled or disabled.
 *
 * @author Mark Scott
 */
public interface Modifiable extends UnnamedConfigurable
{
  /**
   * Sets the availability of this <code>Modifiable</code>'s configuration UI.
   * A disabled UI should display no enabled components.
   *
   * @param enabled <code>true</code> to make the configuration UI available,
   * <code>false</code>.to make it unavailable.
   */
  void setConfigurationUIEnabled(boolean enabled);
}
