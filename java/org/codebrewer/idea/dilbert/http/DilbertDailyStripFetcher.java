/*
 *  Copyright 2007, 2008, 2015 Mark Scott
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
package org.codebrewer.idea.dilbert.http;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.HttpConfigurable;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codebrewer.idea.dilbert.DilbertDailyStrip;
import org.codebrewer.idea.dilbert.DilbertDailyStripPlugin;
import org.codebrewer.idea.dilbert.util.ImageFileType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that knows how to fetch the latest daily strip from the dilbert.com
 * website.
 *
 * @author Mark Scott
 */
public class DilbertDailyStripFetcher
{
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER = Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * The number of milliseconds to wait for establishment of a connection
   * to the dilbert.com website.
   */
  private static final int CONNECTION_TIMEOUT = 20000;

  /**
   * HTTP header used to return the type of content contained in a response.
   */
  private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

  /**
   * HTTP header used to specify that an SC_NOT_MODIFIED response should be sent
   * if a requested resource has not changed version since a specified ETag
   * value.
   */
  private static final String HTTP_HEADER_IF_NONE_MATCH = "If-None-Match";

  /**
   * HTTP header used to return the entity tag for a particular version of a
   * resource.
   */
  private static final String HTTP_HEADER_ETAG = "ETag";

  /**
   * Template used when a status code other than SC_OK is being reported.
   */
  private static final String HTTP_NOT_SC_OK_MESSAGE = "Got HTTP status code {0} when fetching {1}";

  /**
   * The number of milliseconds to block waiting for data when reading
   * from TCP sockets.
   */
  private static final int SO_TIMEOUT = 5000;

  /**
   * Configures an <code>HttpClient</code> with IDEA's HTTP proxy settings if
   * the user has configured their use.
   *
   * @param client the non-<code>null</code> HTTP client connection being
   * prepared.
   *
   * @throws IOException if use of a proxy has been configured but an error
   * occurs if the user is asked for their proxy server credentials.
   */
  private static void adaptForHttpProxy(final HttpClient client) throws IOException
  {
    assert client != null;

    // Use IDEA's HTTP proxy settings, if configured
    //
    final HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();

    if (httpConfigurable != null && httpConfigurable.USE_HTTP_PROXY) {
      final HostConfiguration hostConfiguration = client.getHostConfiguration();
      hostConfiguration.setProxy(httpConfigurable.PROXY_HOST, httpConfigurable.PROXY_PORT);

      if (httpConfigurable.PROXY_AUTHENTICATION) {
        // Use IDEA's support for showing the proxy credentials dialog (if
        // necessary), but note that <http://www.jetbrains.net/jira/browse/IDEABKL-1509>
        // may bite on IDEA 5.x
        //
        httpConfigurable.prepareURL(DilbertDailyStrip.DILBERT_DOT_COM_URL);

        // Copy the credentials into our HTTP client, ensuring they're applied
        // to any authentication realm
        //
        final UsernamePasswordCredentials credentials =
            new UsernamePasswordCredentials(httpConfigurable.PROXY_LOGIN, httpConfigurable.getPlainProxyPassword());
        client.getState().setProxyCredentials(AuthScope.ANY, credentials);

        // Handle proxy servers that don't send HTTP code 407 when
        // authentication is required...
        //
        client.getParams().setAuthenticationPreemptive(true);
      }
    }
  }

  /**
   * Fetches the current daily strip using its URL.
   *
   * @param client a non-<code>null</code> HTTP client object that should be
   * used to fetch the daily strip.
   * @param stripURL the non-<code>null</code> URL of the current daily strip.
   *
   * @return the current daily strip.
   *
   * @throws IOException if there is a problem fetching the current daily strip.
   */
  private static DilbertDailyStrip fetchDailyStrip(
      final HttpClient client, final HttpURL stripURL, final String checksum) throws IOException
  {
    assert client != null;
    assert stripURL != null;

    LOGGER.debug(stripURL.toString());

    final GetMethod stripURLMethod = new GetMethod(stripURL.getURI());

    // Set a default return value
    //
    DilbertDailyStrip result = null;

    // Try to retrieve the daily strip image and use its bytes to create a daily
    // strip object
    //
    try {
      final int statusCode = client.executeMethod(stripURLMethod);

      if (statusCode == HttpStatus.SC_OK) {
        final byte[] responseBody = getStripBytes(stripURLMethod);
        result = new DilbertDailyStrip(responseBody, checksum, stripURL.getURI(), System.currentTimeMillis());
      }
      else {
        final String message = MessageFormat.format(HTTP_NOT_SC_OK_MESSAGE, statusCode, stripURL.getURI());
        LOGGER.info(message);
        throw new IOException(message);
      }
    }
    finally {
      stripURLMethod.releaseConnection();
    }

    return result;
  }

  /**
   * Retrieves the bytes comprising the current daily strip image given an
   * <code>HttpMethod</code> that has already fetched the current daily strip
   * URL.
   *
   * @param stripURLMethod a non-<code>null</code> <code>HttpMethod</code> that
   * has already executed a request to fetch the current daily strip.
   *
   * @return a non-<code>null</code> array of bytes comprising the current daily
   *         strip image.
   *
   * @throws IOException if there is a problem retrieving the image bytes from
   * the response body or if the bytes do not represent a recognized image type.
   */
  private static byte[] getStripBytes(final HttpMethod stripURLMethod) throws IOException
  {
    assert stripURLMethod != null && stripURLMethod.hasBeenUsed();

    final byte[] result;

    final Header contentTypeHeader = stripURLMethod.getResponseHeader(HTTP_HEADER_CONTENT_TYPE);

    if (isRecognizedImageContentType(contentTypeHeader)) {
      final byte[] responseBody = stripURLMethod.getResponseBody();

      if (ImageFileType.getImageFileType(responseBody) == null) {
        final String message = "Response body does not appear to be an image"; // NON-NLS
        LOGGER.info(message);
        throw new IOException(message);
      }
      else {
        result = responseBody;
      }
    }
    else {
      // NON-NLS
      final String message = MessageFormat.format("Unexpected content type for daily strip image: {0}", contentTypeHeader);
      LOGGER.info(message);
      throw new IOException(message);
    }

    return result;
  }

  /**
   * Tries to find the URL of the current daily strip given an
   * <code>HttpMethod</code> that has already fetched the dilbert.com homepage.
   *
   * @param homepageURLMethod a non-<code>null</code> <code>HttpMethod</code>
   * that has already executed a request to fetch the dilbert.com homepage.
   *
   * @return a non-<code>null</code> URL for the current daily strip.
   *
   * @throws IOException if there is a problem identifying the URL for the
   * current daily strip.
   */
  private static HttpURL getStripURL(final HttpMethod homepageURLMethod) throws IOException
  {
    assert homepageURLMethod != null && homepageURLMethod.hasBeenUsed();

    HttpURL result = null;

    // Read the homepage body line by line, looking for a match on the regex
    // that identifies the daily strip image
    //
    BufferedReader br = null;

    try {
      br = new BufferedReader(new InputStreamReader(homepageURLMethod.getResponseBodyAsStream()));
      final Pattern p = Pattern.compile(DilbertDailyStrip.IMAGE_URL_REGEX);
      String line;
      do {
        line = br.readLine();
        if (line != null) {
          final Matcher matcher = p.matcher(line);
          if (matcher.matches()) {
            final String spec = matcher.group(1);

            // Try to form the URL for the daily strip image from the homepage
            // URL and the path to the daily strip image
            //
            result = new HttpURL(spec);
            break;
          }
        }
      }
      while (line != null);

      if (result == null) {
        // NON-NLS
        final String message =
            MessageFormat.format(
                "Didn't match regular expression {0}  to any line in the homepage content", DilbertDailyStrip.IMAGE_URL_REGEX);
        LOGGER.info(message);
        throw new IOException(message);
      }
    }
    finally {
      if (br != null) {
        br.close();
      }
    }

    return result;
  }

  /**
   * Indicates whether or not the given HTTP content-type header identifies an
   * image type supported by the plug-in.
   *
   * @param contentTypeHeader a possibly-<code>null</code> HTTP header
   * indicating a content type.
   *
   * @return <code>true</code> if the content type is recognized, otherwise
   *         <code>false</code>.
   */
  private static boolean isRecognizedImageContentType(final Header contentTypeHeader)
  {
    boolean result = false;

    if (contentTypeHeader != null) {
      final String contentType = contentTypeHeader.getValue();
      result = ImageFileType.CONTENT_TYPE_IMAGE_GIF.equalsIgnoreCase(contentType) ||
          ImageFileType.CONTENT_TYPE_IMAGE_JPEG.equalsIgnoreCase(contentType);
    }

    return result;
  }

  /**
   * Fetches the current daily strip from the dilbert.com website if the site's
   * homepage has been modified since the state represented by a particular hash
   * value (the page's <em>ETag</em>).
   *
   * @param md5Hash a 32-character MD5 checksum value.
   *
   * @return the current daily strip or <code>null</code> if the dilbert.com
   *         homepage has not been modified since the state represented by
   *         <code>md5Hash</code>.
   *
   * @throws IOException if an error occurs fetching the strip.
   */
  public DilbertDailyStrip fetchDailyStrip(final String md5Hash) throws IOException
  {
    LOGGER.info("Looking for strip with from a homepage with an ETag different from " + md5Hash); // NON-NLS
    final GetMethod homepageURLMethod = new GetMethod(DilbertDailyStrip.DILBERT_DOT_COM_URL);

    if (md5Hash != null) {
      homepageURLMethod.setRequestHeader(HTTP_HEADER_IF_NONE_MATCH, String.format("\"%s\"", md5Hash));
    }

    // Set a default return value
    //
    DilbertDailyStrip result = null;

    try {
      final HttpClient client = new HttpClient();
      client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);
      client.getHttpConnectionManager().getParams().setSoTimeout(SO_TIMEOUT);

      // Adapt the client's settings to use IDEA's proxy settings, if they're
      // active
      //
      adaptForHttpProxy(client);

      // Fetch the dilbert.com homepage
      //
      final int statusCode = client.executeMethod(homepageURLMethod);

      // If we got SC_NOT_MODIFIED (code 304) then the homepage hasn't been
      // modified
      if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
        LOGGER.info("Homepage ETag still " + md5Hash); // NON-NLS
        return null;
      }

      // If we get SC_OK (code 200) then we found the homepage OK and can
      // proceed
      //
      if (statusCode == HttpStatus.SC_OK) {
        final HttpURL stripURL = getStripURL(homepageURLMethod);

        if (stripURL == null) {
          final String message = "Couldn't determine URL for daily strip"; // NON-NLS
          LOGGER.info(message);
          throw new IOException(message);
        }

        final Header currentETagHeader = homepageURLMethod.getResponseHeader(HTTP_HEADER_ETAG);
        String eTag = null;

        if (currentETagHeader != null) {
          final String quotedETag = currentETagHeader.getValue();

          if (quotedETag != null && quotedETag.matches("^\"\\p{Alnum}{32}?\"$")) {
            eTag = quotedETag.substring(1, 33);
          }
        }

        if (eTag == null || !eTag.equals(md5Hash)) {
          result = fetchDailyStrip(client, stripURL, eTag);
        }
      }
      else {
        final String message =
            MessageFormat.format(HTTP_NOT_SC_OK_MESSAGE, statusCode, DilbertDailyStrip.DILBERT_DOT_COM_URL);
        LOGGER.info(message);
        throw new IOException(message);
      }
    }
    finally {
      homepageURLMethod.releaseConnection();
    }

    return result;
  }
}
