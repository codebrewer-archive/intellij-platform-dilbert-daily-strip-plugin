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
package org.codebrewer.idea.dilbert.strategy;

import com.intellij.openapi.options.Configurable;
import org.codebrewer.idea.dilbert.Identifiable;
import org.codebrewer.idea.dilbert.Presentable;
import org.codebrewer.idea.dilbert.ui.DailyStripPresenter;

import javax.swing.JComponent;

/**
 * <p>
 * A <em>strategy</em> interface for providers of daily strips.  The lifecycle
 * of provision is controlled by {@link #start()}, {@link #pause()},
 * {@link #resume()} and {@link #stop()}.  A provider may be paused and resumed
 * multiple times after starting.  Once stopped, a provider may lose any state
 * information but should be capable of being restarted.
 * </p>
 * <p>
 * Implementations may provide a configuration UI by implementing the
 * {@link Configurable Configurable} interface and returning a component from
 * its {@link Configurable#createComponent() createComponent()} method.
 * </p>
 * <p>
 * Since implementations are instantiated once for each open project, regardless
 * of whether or not they are currently selected by the user, it is suggested
 * that any expensive operations are deferred until {@link #start()} is invoked.
 * This permits the inexpensive operations inherited from {@link Identifiable}
 * and {@link Presentable} to be invoked without incurring any real penalty.
 * </p>
 *
 * @author Mark Scott
 */
public interface DailyStripProvider extends Identifiable, Presentable
{
  /**
   * Gets a UI component that an end-user can use to control the provider.
   *
   * @return a controller UI component or <code>null</code> if the provider
   *         is not controllable.
   */
  JComponent getControlPanel();

  /**
   * Gets the client (the <em>context</em>) of this provider (<em>strategy</em>).
   *
   * @return the non-<code>null</code> <code>DailyStripPresenter</code> context
   *         in which this provider operates.
   */
  DailyStripPresenter getDailyStripPresenter();

  /**
   * Requests that the provider temporarily stops supplying daily strips while
   * retaining its state so that it may later continue as if it had not paused.
   */
  void pause();

  /**
   * Requests that the provider resumes supplying daily strips from the point at
   * which it was previously paused.
   */
  void resume();

  /**
   * Sets the client (the <em>context</em>) of this provider
   * (<em>strategy</em>).  This method is guaranteed to be called with a
   * non-<code>null</code> parameter value after an implementing class is
   * instantiated for use by the plug-in and before any of the lifecycle events
   * is called.
   *
   * @param context the non-<code>null</code> context in which this provider
   * operates.
   */
  void setDailyStripPresenter(DailyStripPresenter context);

  /**
   * Requests that the provider starts supplying daily strips.
   */
  void start();

  /**
   * Requests that the provider stops supplying daily strips and forgets any
   * state information (<em>e.g.</em> position in a sequence of daily strips
   * being provided).
   */
  void stop();
}
