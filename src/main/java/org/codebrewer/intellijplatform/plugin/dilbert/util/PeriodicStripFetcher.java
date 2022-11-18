/*
 *  Copyright 2007, 2008, 2018, 2022 Mark Scott
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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.codebrewer.intellijplatform.plugin.dilbert.DailyStripEvent;
import org.codebrewer.intellijplatform.plugin.dilbert.DailyStripListener;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStrip;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStripPluginService;
import org.codebrewer.intellijplatform.plugin.dilbert.settings.UnattendedDownloadSettings;

/**
 * A class that periodically triggers fetching of a cartoon strip.
 *
 * @author Mark Scott
 */
public final class PeriodicStripFetcher {
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER =
      Logger.getInstance(DilbertDailyStripPluginService.class.getName());

  /**
   * Interval - in minutes - at which the plug-in will check whether it's time
   * to schedule the fetching of the daily strip.
   */
  private static final int STRIP_FETCH_DUE_CHECK_INTERVAL_MINUTES = 10;

  /**
   * Interval - in minutes - at which the plug-in will check whether it's time
   * to schedule the fetching of the daily strip, as a {@code Duration}.
   */
  private static final Duration STRIP_FETCH_DUE_CHECK_INTERVAL =
      Duration.of(STRIP_FETCH_DUE_CHECK_INTERVAL_MINUTES, ChronoUnit.MINUTES);

  /**
   * Gets the number of milliseconds to wait until the next strip download is
   * due according to the user's current preference settings..
   *
   * @param minutesPastMidnight the time (in minutes) after midnight local time
   * at which the strip should be fetched.
   *
   * @return the number of milliseconds before the next download is due.
   */
  private static long getDelayBeforeNextDownload(final int minutesPastMidnight) {
    assert minutesPastMidnight >= 0 && minutesPastMidnight < TimeUtils.MINUTES_PER_DAY;

    final long now = System.currentTimeMillis();

    // Start with the current local time
    //
    final Calendar nextDownloadTime = Calendar.getInstance();

    // Reset ms, s, m & h to get local time when it was last midnight
    //
    nextDownloadTime.set(Calendar.MILLISECOND, 0);
    nextDownloadTime.set(Calendar.SECOND, 0);
    nextDownloadTime.set(Calendar.MINUTE, 0);
    nextDownloadTime.set(Calendar.HOUR_OF_DAY, 0);

    // Account for the user's preferred download time
    //
    nextDownloadTime.add(Calendar.MINUTE, minutesPastMidnight);

    // If we've already passed today's download time then add a day to get
    // tomorrow's
    //
    if (nextDownloadTime.getTimeInMillis() < now) {
      nextDownloadTime.add(Calendar.DAY_OF_MONTH, 1);
    }

    return nextDownloadTime.getTimeInMillis() - now;
  }

  private ScheduledFuture<?> dailyCheckFuture;
  private ScheduledFuture<?> dailyFetchFuture;

  private void cancelPeriodicTasks() {
    if (dailyCheckFuture == null) {
      LOGGER.info("No need to cancel daily check task");
    } else {
      LOGGER.info("Canceled daily check task: " + dailyCheckFuture.cancel(true));
    }

    if (dailyFetchFuture == null) {
      LOGGER.info("No need to cancel daily fetch task");
    } else {
      LOGGER.info("Canceled daily fetch task: " + dailyFetchFuture.cancel(true));
    }
  }

  /**
   * Starts fetching daily strips once per day at the given number of minutes
   * past midnight local time.
   *
   * @param settings a configuration object that specifies the user's preferred
   * download settings.
   *
   * @throws NullPointerException if <code>settings</code> is null.
   */
  public void startPeriodicFetching(final UnattendedDownloadSettings settings) {
    Objects.requireNonNull(settings);
    cancelPeriodicTasks();

    if (settings.isFetchStripAutomatically()) {
      LOGGER.info("Automatic strip fetching is enabled");

      dailyCheckFuture =
          AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            final long initialDelay = getDelayBeforeNextDownload(settings.getLocalDownloadTime());

            LOGGER.info(
                MessageFormat.format(
                    "The next download is scheduled to occur in {0}ms", initialDelay));

            if (initialDelay < STRIP_FETCH_DUE_CHECK_INTERVAL.toMillis()) {
              LOGGER.info("Scheduling download attempt...");
              dailyFetchFuture =
                  AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
                      new SelfCancellingStripFetcherTask(settings.getMaxFetchAttempts()),
                      initialDelay,
                      Duration.ofMinutes(settings.getFetchInterval()).toMillis(),
                      TimeUnit.MILLISECONDS);
            }
          }, 0, STRIP_FETCH_DUE_CHECK_INTERVAL.toMinutes(), TimeUnit.MINUTES);
    } else {
      LOGGER.info("Automatic strip fetching is not enabled");
    }
  }

  public void stopPeriodicFetching() {
    cancelPeriodicTasks();
  }

  /**
   * A task that will request fetching of the current daily strip a limited
   * number of times, cancelling itself if the strip is fetched before the
   * limit is reached.
   */
  private class SelfCancellingStripFetcherTask implements DailyStripListener, Runnable {
    // The maximum number of times this task will permit its run() method to be
    // invoked
    //
    private final int runLimit;

    // The remaining number of times this task has run
    //
    private int runCount;

    // The object that can retrieve a daily strip
    //
    private final DilbertDailyStripPluginService dilbertPluginService;

    /**
     * Creates a task that will run at most <code>runLimit</code> times.
     *
     * @param runLimit the maximum number of times that this task will run.
     */
    private SelfCancellingStripFetcherTask(final int runLimit) {
      assert runLimit > 0;

      this.runLimit = runLimit;
      runCount = 0;
      dilbertPluginService =
          ApplicationManager.getApplication().getService(DilbertDailyStripPluginService.class);
    }

    private void cancel() {
      dilbertPluginService.removeDailyStripListener(this);
      dailyFetchFuture.cancel(true);
    }

    public void run() {
      dilbertPluginService.addDailyStripListener(this);

      if (runCount < runLimit) {
        runCount += 1;
        LOGGER.info(
            MessageFormat.format("Making fetch attempt number {0}", runCount));

        final DilbertDailyStrip dilbertDailyStrip = dilbertPluginService.getCachedDailyStrip();

        if (dilbertDailyStrip == null) {
          dilbertPluginService.fetchDailyStrip();
        } else {
          dilbertPluginService.fetchDailyStrip(dilbertDailyStrip.getImageChecksum());
        }
      } else {
        LOGGER.info("Cancelling fetches because run limit reached");
        cancel();
      }
    }

    // Implement DailyStripListener

    public void dailyStripUpdated(final DailyStripEvent e) {
      if (e == null) {
        LOGGER.info("Listener received null DailyStripEvent!");
      } else {
        final DilbertDailyStrip newDailyStrip = e.getDilbertDailyStrip();

        // We shouldn't ever receive a null strip but test for that and the
        // 'missing' strip and only cancel the fetch attempts if we received
        // something else
        //
        if (newDailyStrip != null && !newDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP)) {
          cancel();
          LOGGER.info(
              MessageFormat.format(
                  "Got daily strip with homepage ETag {0}",
                  newDailyStrip.getImageChecksum()));
          LOGGER.info(
              MessageFormat.format("Cancelled fetches after successful fetch #", runCount));
        } else {
          LOGGER.info("PeriodicStripFetcher got null daily strip");
        }
      }
    }
  }
}
