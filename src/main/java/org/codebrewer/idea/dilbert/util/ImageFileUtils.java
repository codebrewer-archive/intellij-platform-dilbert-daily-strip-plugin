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

package org.codebrewer.idea.dilbert.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Utility class providing image-related methods.
 *
 * @author Mark Scott
 */
class ImageFileUtils {
  /**
   * The string at the beginning of a GIF87a image if its bytes are interpreted
   * in the ASCII character set.
   */
  private static final String GIF87A_IDENTIFIER = "GIF87a";

  /**
   * The string at the beginning of a GIF89a image if its bytes are interpreted
   * in the ASCII character set.
   */
  private static final String GIF89A_IDENTIFIER = "GIF89a";

  /**
   * The bytes that appear at the start of a JFIF file (JPEG image), with the
   * two bytes that indicate the image length (indexes 4 and 5) zeroed out.
   */
  private static final byte[] JFIF_FILE_PATTERN = new byte[] {
      (byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0,
      (byte) 0x00, (byte) 0x00, (byte) 0x4a, (byte) 0x46, (byte) 0x49, (byte) 0x46, (byte) 0x00
  };

  /**
   * The identifier used for the ASCII character set by the <code>String</code>
   * class.
   */
  private static final String US_ASCII_CHARSET_NAME = "US-ASCII";

  /**
   * Attempts to read a requested number of bytes from the beginning of the
   * specified file.  The array returned may contain fewer than the requested
   * number.
   *
   * @param bytesNeeded the (positive) number of bytes to read.
   * @param file the non-<code>null</code> file from which to read.
   *
   * @return bytes read from the beginning of <code>file</code>.
   *
   * @throws IOException if <code>file</code> cannot be found or an error occurs
   * reading from it.
   */
  private static byte[] getLeadingBytes(final int bytesNeeded, final File file) throws IOException {
    assert bytesNeeded > 0;
    assert file != null;

    final byte[] result;
    final byte[] buffer = new byte[bytesNeeded];

    try (final FileInputStream fis = new FileInputStream(file)) {
      final int bytesRead = fis.read(buffer);
      result = new byte[bytesRead];
      System.arraycopy(buffer, 0, result, 0, bytesRead);
    }

    return result;
  }

  /**
   * Checks to see if a file is non-<code>null</code> and readable.
   *
   * @param file a file to be tested.
   *
   * @return <code>true</code> if <code>file</code> is non-<code>null</code>
   * and readable, otherwise <code>false</code>.
   */
  private static boolean isCandidateFile(final File file) {
    return file != null && file.canRead();
  }

  /**
   * Determines whether or not the given array may contain GIF data.
   *
   * @param bytes data to be tested.
   *
   * @return <code>true</code> if <code>bytes</code> may contain GIF data,
   * otherwise <code>false</code>.
   */
  static boolean mayBeGIF(final byte[] bytes) {
    assert GIF87A_IDENTIFIER.length() == GIF89A_IDENTIFIER.length();

    // Assume it's not a GIF header...
    //
    boolean result = false;

    if (bytes != null && bytes.length >= GIF87A_IDENTIFIER.length()) {

      // We're going to construct a String from the array so limit the size to
      // the maximum length we need to check against
      //
      final byte[] leadingBytes = new byte[Math.min(bytes.length, GIF87A_IDENTIFIER.length())];
      System.arraycopy(bytes, 0, leadingBytes, 0, leadingBytes.length);

      try {
        final String leadingBytesAsString = new String(leadingBytes, US_ASCII_CHARSET_NAME);
        result = GIF87A_IDENTIFIER.equals(leadingBytesAsString) ||
                 GIF89A_IDENTIFIER.equals(leadingBytesAsString);
      } catch (UnsupportedEncodingException ignored) {
        // Should never happen...?
        //
        result = false;
      }
    }

    return result;
  }

  /**
   * Determines whether or not the given file may contain GIF data.
   *
   * @param file a file to be tested.
   *
   * @return <code>true</code> if <code>file</code> may contain GIF data,
   * otherwise <code>false</code>.
   */
  static boolean mayBeGIF(final File file) {
    assert GIF87A_IDENTIFIER.length() == GIF89A_IDENTIFIER.length();

    // Assume it's not a GIF header...
    //
    boolean result = false;

    if (isCandidateFile(file)) {
      try {
        final byte[] imageBytes = getLeadingBytes(GIF87A_IDENTIFIER.length(), file);
        result = mayBeGIF(imageBytes);
      } catch (IOException e) {
        // Ignore - default will stand...
      }
    }

    return result;
  }

  /**
   * Determines whether or not the given array may contain JFIF data.
   *
   * @param bytes data to be tested.
   *
   * @return <code>true</code> if <code>bytes</code> may contain JFIF data,
   * otherwise <code>false</code>.
   */
  static boolean mayBeJFIF(final byte[] bytes) {
    // Assume it's a JFIF header...
    //
    boolean result = true;

    if (bytes.length >= JFIF_FILE_PATTERN.length) {
      // Bytes 5 and 6 hold a length value that differs between files of
      // different length so we ignore it by setting those values to zero
      // (to match the pattern)
      //
      bytes[4] = (byte) 0x00;
      bytes[5] = (byte) 0x00;

      // Compare the given array with the known JFIF header, returning false
      // if any difference is found
      //
      for (int i = 0; i < JFIF_FILE_PATTERN.length; i++) {
        if (JFIF_FILE_PATTERN[i] != bytes[i]) {
          result = false;
          break;
        }
      }
    } else {
      result = false;
    }

    return result;
  }

  /**
   * Determines whether or not the given file may contain JFIF data.
   *
   * @param file a file to be tested.
   *
   * @return <code>true</code> if <code>file</code> may contain JFIF data,
   * otherwise <code>false</code>.
   */
  static boolean mayBeJFIF(final File file) {
    // Assume it's not a JFIF header...
    //
    boolean result = false;

    if (isCandidateFile(file)) {
      try {
        final byte[] imageBytes = getLeadingBytes(JFIF_FILE_PATTERN.length, file);
        result = mayBeJFIF(imageBytes);
      } catch (IOException e) {
        // Ignore - default will stand...
      }
    }

    return result;
  }

  private ImageFileUtils() {
    // Utility class
  }
}
