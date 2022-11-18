/*
 * Copyright 2005, 2007, 2008, 2015, 2018, 2022 Mark Scott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codebrewer.intellijplatform.plugin.dilbert;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ArrayUtil;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Encapsulates information about a daily cartoon strip from the dilbert.com
 * website.
 *
 * @author Mark Scott
 */
public final class DilbertDailyStrip {
  /**
   * A constant that can be used to represent a 'missing' strip (e.g. when a
   * network problem prevents a cartoon from being fetched).
   */
  public static final DilbertDailyStrip MISSING_STRIP =
      new DilbertDailyStrip(IconLoader.getIcon("/no-dilbert.png", DilbertDailyStrip.class), null, null, Long.MIN_VALUE);

  /**
   * A regular expression pattern that, when applied to the dilbert.com homepage
   * content, identifies the URL of a cartoon image.
   */
  public static final Pattern IMAGE_URL_PATTERN =
      Pattern.compile("^.*=\"((https?:)?//assets\\.amuniversal\\.com/\\p{Alnum}{32})\".*$");

  /**
   * The URL of the dilbert.com website.
   */
  public static final String DILBERT_DOT_COM_URL = "https://www.dilbert.com/";

  /**
   * The raw image data.
   */
  private byte[] imageBytes = ArrayUtil.EMPTY_BYTE_ARRAY;

  /**
   * A cartoon image.
   */
  private final Icon image;

  /**
   * The URL from which this cartoon was fetched.  The URL of the cartoon image
   * changes daily and is only available for a certain period of time.
   */
  private final String uri;

  /**
   * The time at which the strip was fetched.
   */
  private final long retrievalTime;

  /**
   * The ETag of the dilbert.com homepage at the time this strip was fetched.
   */
  private final String homepageEtag;

  /**
   * Constructs a daily strip object from a cartoon image, its URL and the time
   * at which the URL for the dilbert.com site was last modified.
   *
   * @param image a cartoon image.
   * @param homepageEtag the current ETag of the dilbert.com homepage.
   * @param uri the image's URI.
   * @param retrievalTime the time at which the dilbert.com site's homepage was
   * last modified.
   */
  private DilbertDailyStrip(final Icon image,
                            final String homepageEtag,
                            final String uri,
                            final long retrievalTime) {
    this.image = image;
    this.homepageEtag = homepageEtag;
    this.uri = uri;
    this.retrievalTime = retrievalTime;
  }

  /**
   * Constructs a daily strip object from a cartoon image, its URL and the time
   * at which the URL for the dilbert.com site was last modified.
   *
   * @param imageBytes the bytes comprising a cartoon image.
   * @param homepageEtag the current ETag of the dilbert.com homepage.
   * @param uri the image's URI.
   * @param retrievalTime the time at which the dilbert.com site's homepage was
   * last modified.
   */
  public DilbertDailyStrip(final byte[] imageBytes,
                           final String homepageEtag,
                           final String uri,
                           final long retrievalTime) {
    this(new ImageIcon(imageBytes), homepageEtag, uri, retrievalTime);
    this.imageBytes = new byte[imageBytes.length];
    System.arraycopy(imageBytes, 0, this.imageBytes, 0, imageBytes.length);
  }

  /**
   * Gets the cartoon image.
   *
   * @return the cartoon image.
   */
  public Icon getIcon() {
    return image;
  }

  /**
   * Gets the bytes that comprise the cartoon image.
   *
   * @return the bytes that comprise the cartoon image.
   */
  public byte[] getImageBytes() {
    final byte[] imageCopy = new byte[imageBytes.length];
    System.arraycopy(imageBytes, 0, imageCopy, 0, imageBytes.length);

    return imageCopy;
  }

  public String getImageChecksum() {
    return homepageEtag;
  }

  /**
   * Gets the time at which the strip was fetched.
   *
   * @return the time at which the strip was fetched.
   */
  public long getRetrievalTime() {
    return retrievalTime;
  }

  /**
   * Gets the URI from which the cartoon image was retrieved.
   *
   * @return the URI from which the cartoon image was retrieved.
   */
  public String getUri() {
    return uri;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof final DilbertDailyStrip that)) {
      return false;
    }

    //noinspection RedundantIfStatement
    if (!Objects.equals(homepageEtag, that.homepageEtag)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return homepageEtag == null ? 0 : homepageEtag.hashCode();
  }
}
