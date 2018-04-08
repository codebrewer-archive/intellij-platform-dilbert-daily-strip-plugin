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

package org.codebrewer.idea.dilbert.strategy;

import org.codebrewer.idea.dilbert.ui.DailyStripPresenter;
import org.codebrewer.intellijplatform.plugin.util.l10n.ResourceBundleManager;

/**
 * @author Mark Scott
 */
abstract class LocalizableDailyStripProvider extends AbstractDailyStripProvider {
  protected LocalizableDailyStripProvider(final DailyStripPresenter presenter) {
    setDailyStripPresenter(presenter);
  }

  public String getDescription() {
    return ResourceBundleManager.getLocalizedString(getClass(), "strategy.description");
  }

  public String getDisplayName() {
    return ResourceBundleManager.getLocalizedString(getClass(), "strategy.name");
  }

  public final void setDailyStripPresenter(final DailyStripPresenter context) {
    super.setDailyStripPresenter(context);
  }

  public String toString() {
    return getDisplayName();
  }
}
