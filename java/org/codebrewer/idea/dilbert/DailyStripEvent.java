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
package org.codebrewer.idea.dilbert;

import java.util.EventObject;

/**
 * An event that indicates that a new daily strip is available.
 *
 * @author Mark Scott
 */
public class DailyStripEvent extends EventObject
{
  private final DilbertDailyStrip dailyStrip;

  public DailyStripEvent(final Object source, final DilbertDailyStrip dailyStrip)
  {
    super(source);

    if (dailyStrip == null) {
      throw new IllegalArgumentException("dailyStrip cannot be null");
    }

    this.dailyStrip = dailyStrip;
  }

  /**
   * Gets the newly available daily strip.
   *
   * @return a non-<code>null</code> daily strip.
   */
  public DilbertDailyStrip getDilbertDailyStrip()
  {
    return dailyStrip;
  }

  public String toString()
  {
    final StringBuffer sb = new StringBuffer(100);

    sb.append(DailyStripEvent.class.getName());
    sb.append("[dailyStrip=").append(dailyStrip);
    //noinspection MagicCharacter
    sb.append(']');
    sb.append(super.toString());

    return sb.toString();
  }
}
