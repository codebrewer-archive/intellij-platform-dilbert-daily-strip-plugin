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

package org.codebrewer.intellijplatform.plugin.dilbert;

/**
 * An interface for things which may be presented to a human user.
 *
 * @author Mark Scott
 */
public interface Presentable {
  /**
   * Gets a description that may be used in a UI to provide a description for
   * an end-user.
   *
   * @return a description of the provider.
   */
  String getDescription();

  /**
   * Gets a user-presentable name.
   *
   * @return the name a the provider.
   */
  String getDisplayName();
}
