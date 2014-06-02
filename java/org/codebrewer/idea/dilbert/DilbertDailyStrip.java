/*
 * Copyright 2005, 2007, 2008 Mark Scott
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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ArrayUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Encapsulates information about a daily cartoon strip from the dilbert.com
 * website.
 *
 * @author Mark Scott
 */
public final class DilbertDailyStrip
{
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * A constant that can be used to represent a 'missing' strip (e.g. when a
   * network problem prevents a cartoon from being fetched).
   */
  public static final DilbertDailyStrip MISSING_STRIP =
      new DilbertDailyStrip(IconLoader.getIcon("/no-dilbert.png"), "8b0e7fc225a524a0fc3ab826f15dc2b9", null, Long.MIN_VALUE);

  /**
   * A regular expression that, when applied to the dilbert.com homepage,
   * identifies the URL of the current cartoon image.
   */
  public static final String IMAGE_URL_REGEX = "^<img src=\"(.*\\.strip\\.print\\.gif)\" />$";

  /**
   * The URL of the dilbert.com website.
   */
  public static final String DILBERT_DOT_COM_URL = "http://www.dilbert.com/fast/";

  /**
   * Generates a 32-character MD5 checksum value for an array of data.
   *
   * @param data the (image) data to be checksummed.
   *
   * @return a 32-character MD5 checksum string.
   */
  private static String generateMd5Hash(final byte[] data)
  {
    BufferedInputStream bis = null;

    try {
      final MessageDigest md = MessageDigest.getInstance("MD5");
      bis = new BufferedInputStream(new ByteArrayInputStream(data), 4096);

      while (true) {
        final int datum = bis.read();
        if (datum == -1) {
          break;
        }
        else {
          md.update((byte) datum);
        }
      }
      final BigInteger hash = new BigInteger(1, md.digest());
      String hexHash = hash.toString(16);

      // MD5 hashes are 16 bytes so check the hex representation is 32
      // characters, padding with leading zeroes if necessary
      int l = hexHash.length();
      if (l < 32) {
        final StringBuffer sb = new StringBuffer(16);
        while (l++ < 32) {
          sb.append('0');
        }
        hexHash = sb.append(hexHash).toString();
      }

      return hexHash;
    }
    catch (NoSuchAlgorithmException e) {
      LOGGER.error("Couldn't generate MD5 hash for image", e);
      return null;
    }
    catch (IOException e) {
      LOGGER.error("Couldn't generate MD5 hash for image", e);
      return null;
    }
    finally {
      if (bis != null) {
        try {
          bis.close();
        }
        catch (IOException e) {
          LOGGER.error("Couldnt' close image array input stream", e);
        }
      }
    }
  }

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
   * The 32-character MD5 checksum hash for the image.
   */
  private final String md5Hash;

  /**
   * Constructs a daily strip object from a cartoon image, its URL and the time
   * at which the URL for the dilbert.com site was last modified.
   *
   * @param image a cartoon image.
   * @param md5Hash the image's MD5 checksum hash.
   * @param uri the image's URI.
   * @param retrievalTime the time at which the dilbert.com site's homepage was
   * last modified.
   */
  private DilbertDailyStrip(final Icon image, final String md5Hash, final String uri, final long retrievalTime)
  {
    this.image = image;
    this.md5Hash = md5Hash;
    this.uri = uri;
    this.retrievalTime = retrievalTime;
  }

  /**
   * Constructs a daily strip object from a cartoon image, its URL and the time
   * at which the URL for the dilbert.com site was last modified.
   *
   * @param imageBytes the bytes comprising a cartoon image.
   * @param uri the image's URI.
   * @param retrievalTime the time at which the dilbert.com site's homepage was
   * last modified.
   */
  public DilbertDailyStrip(final byte[] imageBytes, final String uri, final long retrievalTime)
  {
    this(new ImageIcon(imageBytes), generateMd5Hash(imageBytes), uri, retrievalTime);
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

  public String getImageChecksum()
  {
    return md5Hash;
  }

  /**
   * Gets the time at which the strip was fetched.
   *
   * @return the time at which the strip was fetched.
   */
  public long getRetrievalTime()
  {
    return retrievalTime;
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

    if (!md5Hash.equals(that.md5Hash)) {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    return md5Hash.hashCode();
  }
}
