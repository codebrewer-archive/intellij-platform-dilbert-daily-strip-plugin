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

package org.codebrewer.intellijplatform.plugin.dilbert.util;

import java.time.Duration;

/**
 * A time-related utility class.
 *
 * @author Mark Scott
 */
public class TimeUtils {
  /**
   * The number of milliseconds in a minute.
   */
  static final int MILLIS_PER_MINUTE = (int) Duration.ofMinutes(1).toMillis();

  /**
   * The number of milliseconds in a day.
   */
  static final int MILLIS_PER_DAY = (int) Duration.ofDays(1).toMillis();

  /**
   * The number of seconds in a day.
   */
  public static final int SECONDS_PER_DAY = (int) Duration.ofDays(1).getSeconds();

  /**
   * The number of minutes in a day.
   */
  public static final int MINUTES_PER_DAY = (int) Duration.ofDays(1).toMinutes();

  /**
   * Gets the smallest number of whole minutes represented by a number of
   * seconds, preserving sign.
   *
   * @param seconds a number of seconds
   *
   * @return the number of whole minutes represented by {@code seconds}
   */
  public static int secondsToMinutes(int seconds) {
    return (int) Duration.ofSeconds(seconds).toMinutes();
  }

  private TimeUtils() {
    // Utility class
  }
}
