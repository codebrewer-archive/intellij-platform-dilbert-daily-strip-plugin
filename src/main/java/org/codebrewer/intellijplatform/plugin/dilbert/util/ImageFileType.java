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

package org.codebrewer.intellijplatform.plugin.dilbert.util;

import com.siyeh.ig.portability.mediatype.ImageMediaType;
import java.io.File;

/**
 * Enumeration of image file types used for Dilbert cartoons.
 *
 * @author Mark Scott
 */
public enum ImageFileType {
  /**
   * Constant representing the GIF file type.
   */
  GIF(ImageMediaType.GIF) {
    boolean accepts(final byte[] bytes) {
      return ImageFileUtils.mayBeGIF(bytes);
    }

    protected boolean accepts(final File file) {
      return ImageFileUtils.mayBeGIF(file);
    }
  },

  /**
   * Constant representing the JFIF file type.
   */
  JFIF(ImageMediaType.JPEG) {
    boolean accepts(final byte[] bytes) {
      return ImageFileUtils.mayBeJFIF(bytes);
    }

    protected boolean accepts(final File file) {
      return ImageFileUtils.mayBeJFIF(file);
    }
  };

  /**
   * Searches for the constant value of this class that matches image data that
   * may be contained in a byte array.
   *
   * @param bytes an array that may contain image data.
   *
   * @return a constant of this class that represents the image type contained
   * in <code>bytes</code>, or <code>null</code> if no match is found.
   *
   * @see #GIF
   * @see #JFIF
   */
  public static ImageFileType getImageFileType(final byte[] bytes) {
    for (final ImageFileType imageFileType : ImageFileType.values()) {
      if (imageFileType.accepts(bytes)) {
        return imageFileType;
      }
    }

    return null;
  }

  /**
   * Searches for the constant value of this class that matches image data that
   * may be contained in a file.
   *
   * @param file a file that may contain image data.
   *
   * @return a constant of this class that represents the image type contained
   * in <code>file</code>, or <code>null</code> if no match is found.
   *
   * @see #GIF
   * @see #JFIF
   */
  public static ImageFileType getImageFileType(final File file) {
    for (final ImageFileType imageFileType : ImageFileType.values()) {
      if (imageFileType.accepts(file)) {
        return imageFileType;
      }
    }

    return null;
  }

  private final ImageMediaType imageMediaType;

  ImageFileType(final ImageMediaType imageMediaType) {
    this.imageMediaType = imageMediaType;
  }

  /**
   * Indicates whether or not the given byte array may contain image data
   * represented by this instance.
   *
   * @param bytes an array that may contain image data.
   *
   * @return <code>true</code> if <code>bytes</code> may contain image data
   * represented by this instance, otherwise <code>false</code>.
   */
  abstract boolean accepts(byte[] bytes);

  /**
   * Indicates whether or not the given file may contain image data represented
   * by this instance.
   *
   * @param file a file that may contain image data.
   *
   * @return <code>true</code> if <code>file</code> may contain image data
   * represented by this instance, otherwise <code>false</code>.
   */
  abstract boolean accepts(File file);

  public String toString() {
    return ImageFileType.class.getName() + "[imageMediaType='" + imageMediaType.toString() + "']";
  }
}
