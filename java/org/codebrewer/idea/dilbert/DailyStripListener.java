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

import java.util.EventListener;

/**
 * The listener interface for receiving daily strip events.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public interface DailyStripListener extends EventListener
{
  /**
   * Invoked when a new daily strip is available.
   *
   * @param e an event object encapsulating the new daily strip.
   */
  void dailyStripUpdated(DailyStripEvent e);
}
