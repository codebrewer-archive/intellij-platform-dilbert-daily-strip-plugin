/*
 * Copyright 2005, 2007 Mark Scott
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
package org.codebrewer.idea.dilbert;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ArrayUtil;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Encapsulates information about a daily cartoon strip from the dilbert.com
 * website.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class DilbertDailyStrip
{
  /**
   * A constant that can be used to represent a 'missing' strip (e.g. when a
   * network problem prevents a cartoon from being fetched).
   */
  public static final DilbertDailyStrip MISSING_STRIP =
      new DilbertDailyStrip(IconLoader.getIcon("/no-dilbert.png"), null, Long.MIN_VALUE);

  /**
   * A regular expression that, when applied to the dilbert.com homepage,
   * identifies the URL of the current cartoon image.
   */
  public static final String IMAGE_URL_REGEX = ".*IMG SRC=\"(.*[gif|jpg])\" ALT=\"Today's Comic.*";

  /**
   * The URL of the dilbert.com website.
   */
  public static final String DILBERT_DOT_COM_URL = "http://www.dilbert.com/";

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
   * The dilbert.com site URL's "Last-Modified" HTTP header value.
   */
  private final long lastModified;

  /**
   * Constructs a daily strip object from a cartoon image, its URL and the time
   * at which the URL for the dilbert.com site was last modified.
   *
   * @param image a cartoon image.
   * @param uri the image's URI.
   * @param lastModified the time at which the dilbert.com site's homepage was
   * last modified.
   */
  private DilbertDailyStrip(final Icon image, final String uri, final long lastModified)
  {
    this.image = image;
    this.uri = uri;
    this.lastModified = lastModified;
  }

  /**
   * Constructs a daily strip object from a cartoon image, its URL and the time
   * at which the URL for the dilbert.com site was last modified.
   *
   * @param imageBytes the bytes comprising a cartoon image.
   * @param uri the image's URI.
   * @param lastModified the time at which the dilbert.com site's homepage was
   * last modified.
   */
  public DilbertDailyStrip(final byte[] imageBytes, final String uri, final long lastModified)
  {
    this(new ImageIcon(imageBytes), uri, lastModified);
    this.imageBytes = new byte[imageBytes.length];
    System.arraycopy(imageBytes, 0, this.imageBytes, 0, imageBytes.length);
  }

  /**
   * Gets the cartoon image.
   *
   * @return the cartoon image.
   */
  public Icon getIcon()
  {
    return image;
  }

  /**
   * Gets the bytes that comprise the cartoon image.
   *
   * @return the bytes that comprise the cartoon image.
   */
  public byte[] getImageBytes()
  {
    final byte[] imageCopy = new byte[imageBytes.length];
    System.arraycopy(imageBytes, 0, imageCopy, 0, imageBytes.length);

    return imageCopy;
  }

  /**
   * Gets the time at which the <em>homepage</em> URL was last modified.
   *
   * @return the time at which the <em>homepage</em> URL was last modified.
   */
  public long getLastModified()
  {
    return lastModified;
  }

  /**
   * Gets the URI from which the cartoon image was retrieved.
   *
   * @return the URI from which the cartoon image was retrieved.
   */
  public String getUri()
  {
    return uri;
  }

  public boolean equals(final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final DilbertDailyStrip that = (DilbertDailyStrip) obj;

    if (lastModified != that.lastModified) {
      return false;
    }
    if (image != null ? !image.equals(that.image) : that.image != null) {
      return false;
    }
    //noinspection RedundantIfStatement
    if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result = image != null ? image.hashCode() : 0;
    result = 29 * result + (uri != null ? uri.hashCode() : 0);
    result = 29 * result + (int) (lastModified ^ lastModified >>> 32);
    return result;
  }
}
