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

package org.codebrewer.intellijplatform.plugin.dilbert.strategy;

import com.intellij.openapi.diagnostic.Logger;
import javax.swing.JComponent;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStripPlugin;
import org.codebrewer.intellijplatform.plugin.dilbert.ui.DailyStripPresenter;

/**
 * @author Mark Scott
 */
abstract class AbstractDailyStripProvider implements DailyStripProvider {
  /**
   * For logging messages to IDEA's log.
   */
  protected static final Logger LOGGER =
      Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * Has {@link #pause()} been called without (yet) a corresponding call to
   * {@link #resume()}?
   */
  private boolean isPaused;

  /**
   * Has {@link #start()} been called without (yet) a corresponding call to
   * {@link #stop()}?
   */
  private boolean isRunning;

  /**
   * Our client (the <em>context</em> for our <em>strategy</em>).
   */
  private DailyStripPresenter presenter;

  protected AbstractDailyStripProvider() {
    isPaused = false;
    isRunning = false;
  }

  protected abstract void doPause();

  protected abstract void doResume();

  protected abstract void doStart();

  protected abstract void doStop();

  public JComponent getControlPanel() {
    return null;
  }

  public DailyStripPresenter getDailyStripPresenter() {
    return presenter;
  }

  public synchronized void pause() {
    if (isRunning && !isPaused) {
      doPause();
      isPaused = true;
    }
  }

  public synchronized void resume() {
    if (isPaused) {
      doResume();
      isPaused = false;
    }
  }

  public void setDailyStripPresenter(final DailyStripPresenter context) {
    presenter = context;
  }

  public synchronized void start() {
    if (!isRunning) {
      doStart();
      isPaused = false;
      isRunning = true;
    }
  }

  public synchronized void stop() {
    if (isRunning) {
      doStop();
      isPaused = false;
      isRunning = false;
    }
  }
}
