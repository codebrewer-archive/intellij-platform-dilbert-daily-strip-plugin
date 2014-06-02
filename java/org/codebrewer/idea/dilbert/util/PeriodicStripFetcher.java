/*
 *  Copyright 2007, 2008 Mark Scott
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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.codebrewer.idea.dilbert.DailyStripEvent;
import org.codebrewer.idea.dilbert.DailyStripListener;
import org.codebrewer.idea.dilbert.DilbertDailyStrip;
import org.codebrewer.idea.dilbert.DilbertDailyStripPlugin;
import org.codebrewer.idea.dilbert.settings.UnattendedDownloadSettings;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A class that periodically triggers fetching of a cartoon strip.
 *
 * @author Mark Scott
 */
public final class PeriodicStripFetcher
{
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * Gets the number of milliseconds to wait until the next strip download is
   * due according to the user's current preference settings..
   *
   * @param minutesPastMidnight the time (in minutes) after midnight local time
   * at which the strip should be fetched.
   *
   * @return the number of milliseconds before the next download is due.
   */
  private static long getDelayBeforeNextDownload(final int minutesPastMidnight)
  {
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

    final long delay = nextDownloadTime.getTimeInMillis() - now;
    return delay;
  }

  /**
   * Used to schedule daily fetching of the strip.
   */
  private Timer timer;

  /**
   * Starts fetching daily strips once per day at the given number of minutes
   * past midnight local time.
   *
   * @param settings a configuration object that specifies the user's preferred
   * download settings.
   *
   * @throws IllegalArgumentException if <code>localDownloadTime</code> is
   * negative or greater than or equal to {@link TimeUtils#MINUTES_PER_DAY}.
   */
  public void startPeriodicFetching(final UnattendedDownloadSettings settings)
  {
    if (settings == null) {
      throw new IllegalArgumentException("settings cannot be null");
    }

    if (timer != null) {
      timer.cancel();
    }

    if (settings.isFetchStripAutomatically()) {
      final long delay = getDelayBeforeNextDownload(settings.getLocalDownloadTime());
      final int fetchInterval = settings.getFetchInterval();
      final int maxFetchAttempts = settings.getMaxFetchAttempts();

      timer = new Timer(true);
      LOGGER.info(MessageFormat.format("The next download is scheduled to occur in {0}ms", // NON-NLS
          new Object[]{ new Long(delay) }));
      timer.scheduleAtFixedRate(new TimerTask()
      {
        public void run()
        {
          LOGGER.info("It's time to see if there's a new Dilbert strip available..."); // NON-NLS
          timer.schedule(new SelfCancellingStripFetcherTask(maxFetchAttempts),
              0,
              new Integer(TimeUtils.MILLIS_PER_SECOND * TimeUtils.SECONDS_PER_MINUTE * fetchInterval).longValue());
        }
      }, delay, TimeUtils.MILLIS_PER_DAY);
    }
  }

  public void stopPeriodicFetching()
  {
    if (timer != null) {
      timer.cancel();
    }
  }

  /**
   * A {@link TimerTask TimerTask} that will request fetching of the current
   * daily strip a limited number of times, cancelling itself if the strip is
   * fetched before the limit is reached.
   */
  private static class SelfCancellingStripFetcherTask extends TimerTask implements DailyStripListener
  {
    // The maximum number of times this TimerTask will permit its run() method
    // to be invoked
    //
    private final int runLimit;

    // The remaining number of times this timer will run
    //
    private int timeToLive;

    // The object that can retrieve a daily strip
    //
    private final DilbertDailyStripPlugin dilbertPlugin;

    /**
     * Creates a task that will run at most <code>runLimit</code> times.
     *
     * @param runLimit the maximum number of times that this task will run.
     */
    private SelfCancellingStripFetcherTask(final int runLimit)
    {
      assert runLimit > 0;

      this.runLimit = runLimit;
      timeToLive = runLimit;
      dilbertPlugin =
          (DilbertDailyStripPlugin) ApplicationManager.getApplication().getComponent(DilbertDailyStripPlugin.class);
      dilbertPlugin.addDailyStripListener(this);
    }

    public boolean cancel()
    {
      dilbertPlugin.removeDailyStripListener(this);

      return super.cancel();
    }

    public void run()
    {
      --timeToLive;

      if (timeToLive >= 0) {
        LOGGER.debug(MessageFormat.format("Making fetch attempt number {0}", // NON-NLS
            new Object[]{ new Integer(runLimit - timeToLive) }));

        final DilbertDailyStrip dilbertDailyStrip = dilbertPlugin.getCachedDailyStrip();

        if (dilbertDailyStrip == null) {
          dilbertPlugin.fetchDailyStrip();
        }
        else {
          dilbertPlugin.fetchDailyStrip(dilbertDailyStrip.getImageChecksum());
        }
      }
      else {
        LOGGER.debug("Cancelling fetches because TTL reached zero"); // NON-NLS
        cancel();
      }
    }

    // Implement DailyStripListener

    public void dailyStripUpdated(final DailyStripEvent e)
    {
      if (e == null) {
        LOGGER.info("Listener received null DailyStripEvent!"); // NON-NLS
      }
      else {
        final DilbertDailyStrip newDailyStrip = e.getDilbertDailyStrip();

        // We shouldn't ever receive a null strip but test for that and the
        // 'missing' strip and only cancel the fetch attempts if we received
        // something else
        //
        if (newDailyStrip != null && !newDailyStrip.equals(DilbertDailyStrip.MISSING_STRIP)) {
          cancel();
          LOGGER.debug(MessageFormat.format("Got daily strip with last modified time of {0}", // NON-NLS
              new Object[]{ new Long(newDailyStrip.getRetrievalTime()) }));
          LOGGER.debug(MessageFormat.format("Cancelled fetches with TTL = {0}", // NON-NLS
              new Object[]{ new Integer(timeToLive) }));
        }
        else {
          LOGGER.info("PeriodicStripFetcher got null daily strip"); // NON-NLS
        }
      }
    }
  }
}
