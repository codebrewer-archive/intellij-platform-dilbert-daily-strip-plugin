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
package org.codebrewer.idea.dilbert.util;

/**
 * A time-related utility class.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public class TimeUtils
{
  /**
   * The number of milliseconds in a second.
   */
  public static final int MILLIS_PER_SECOND = 1000;

  /**
   * The number of seconds in a minute.
   */
  public static final int SECONDS_PER_MINUTE = 60;

  /**
   * The number of minutes in an hour.
   */
  public static final int MINUTES_PER_HOUR = 60;

  /**
   * The number of hours in a day.
   */
  public static final int HOURS_PER_DAY = 24;

  /**
   * The number of milliseconds in a day.
   */
  public static final int MILLIS_PER_DAY = MILLIS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY;

  /**
   * The number of minutes in a day.
   */
  public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;

  private TimeUtils()
  {
    // Utility class
  }
}
