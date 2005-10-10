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
package org.codebrewer.idea.dilbert.ui;

import org.codebrewer.idea.dilbert.DilbertDailyStrip;

/**
 * Interface to be implemented by classes capable of presenting a view of a
 * Dilbert daily strip cartoon.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public interface DailyStripPresenter
{
  /**
   * Gets the possibly-null daily strip currently being presented.
   *
   * @return the possibly-null daily strip currently being presented.
   */
  DilbertDailyStrip getDilbertDailyStrip();

  /**
   * Accepts a daily strip for presentation.
   *
   * @param newDailyStrip the daily strip to be presented.
   */
  void setDailyStrip(DilbertDailyStrip newDailyStrip);
}