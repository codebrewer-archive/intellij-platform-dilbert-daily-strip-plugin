/*
 *  Copyright 2007, 2008, 2015, 2018, 2022 Mark Scott
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

package org.codebrewer.intellijplatform.plugin.dilbert.http;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.IdeHttpClientHelpers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStrip;
import org.codebrewer.intellijplatform.plugin.dilbert.DilbertDailyStripPluginService;
import org.codebrewer.intellijplatform.plugin.dilbert.util.ImageFileType;

/**
 * A class that knows how to fetch the latest daily strip from the dilbert.com
 * website.
 *
 * @author Mark Scott
 */
public class DilbertDailyStripFetcher {
  /**
   * For logging messages to IDEA's log.
   */
  private static final Logger LOGGER =
      Logger.getInstance(DilbertDailyStripPluginService.class.getName());

  /**
   * The number of milliseconds to wait for establishment of a connection
   * to the dilbert.com website.
   */
  private static final int CONNECTION_TIMEOUT = 20000;

  /**
   * A suffix seen on ETag headers set by dilbert.com <em>e.g.</em>:
   * {@code W/"bcc73e86198ecb0bdeb16541af340c7f-gzip"}.
   */
  private static final String ETAG_GZIP_SUFFIX = "-gzip";

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
   * The HTTP scheme.
   */
  private static final String HTTP_SCHEME = "http";

  /**
   * The HTTPS scheme.
   */
  private static final String HTTPS_SCHEME = "https";

  /**
   * The number of milliseconds to block waiting for data when reading
   * from TCP sockets.
   */
  private static final int SO_TIMEOUT = 5000;

  public DilbertDailyStripFetcher() {
  }

  /**
   * Fetches the current daily strip using its URL.
   *
   * @param client a non-<code>null</code> HTTP client object that should be
   * used to fetch the daily strip.
   * @param stripGet the non-<code>null</code> URL of the current daily strip.
   * @param homepageEtag the current ETag of the dilbert.com homepage.
   *
   * @return the current daily strip.
   *
   * @throws IOException if there is a problem fetching the current daily strip.
   */
  private static DilbertDailyStrip fetchDailyStrip(final CloseableHttpClient client,
                                                   final HttpGet stripGet,
                                                   final String homepageEtag) throws IOException {
    assert client != null;
    assert stripGet != null;

    // Try to retrieve the daily strip image and use its bytes to create a daily
    // strip object
    //
    LOGGER.info("Executing " + stripGet);
    try (final CloseableHttpResponse response = client.execute(stripGet)) {
      LOGGER.info("Response status: " + response.getStatusLine());

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        final byte[] responseBody = getStripBytes(response);
        return new DilbertDailyStrip(
            responseBody, homepageEtag, stripGet.getRequestLine().getUri(), System.currentTimeMillis());
      } else {
        final String message =
            MessageFormat.format(
                HTTP_NOT_SC_OK_MESSAGE, response.getStatusLine().getStatusCode(), stripGet.getURI());
        LOGGER.info(message);
        throw new IOException(message);
      }
    }
  }

  /**
   * Retrieves the bytes comprising the current daily strip image given an
   * <code>HttpMethod</code> that has already fetched the current daily strip
   * URL.
   *
   * @param stripResponse a non-<code>null</code> <code>HttpMethod</code> that
   * has already executed a request to fetch the current daily strip.
   *
   * @return a non-<code>null</code> array of bytes comprising the current daily
   * strip image.
   *
   * @throws IOException if there is a problem retrieving the image bytes from
   * the response body or if the bytes do not represent a recognized image type.
   */
  private static byte[] getStripBytes(final CloseableHttpResponse stripResponse) throws IOException {
    assert stripResponse != null;

    final byte[] result;

    final Header contentTypeHeader = stripResponse.getFirstHeader(HTTP_HEADER_CONTENT_TYPE);

    if (isRecognizedImageContentType(contentTypeHeader)) {
      final byte[] responseBody = EntityUtils.toByteArray(stripResponse.getEntity());

      if (ImageFileType.getImageFileType(responseBody) == null) {
        final String message = "Response body does not appear to be an image"; // NON-NLS
        LOGGER.info(message);
        throw new IOException(message);
      } else {
        result = responseBody;
      }
    } else {
      // NON-NLS
      final String message = MessageFormat
          .format("Unexpected content type for daily strip image: {0}", contentTypeHeader);
      LOGGER.info(message);
      throw new IOException(message);
    }

    return result;
  }

  /**
   * Tries to find the URL of the current daily strip given an
   * <code>HttpMethod</code> that has already fetched the dilbert.com homepage.
   *
   * @param homepageResponse a non-<code>null</code> <code>HttpMethod</code>
   * that has already executed a request to fetch the dilbert.com homepage.
   *
   * @return a non-<code>null</code> URL for the current daily strip.
   *
   * @throws IOException if there is a problem identifying the URL for the
   * current daily strip.
   */
  private static HttpGet getStripURL(final CloseableHttpResponse homepageResponse) throws IOException {
    assert homepageResponse != null;

    HttpGet result = null;

    // Read the homepage body line by line, looking for the first match on the
    // regex that identifies the daily strip image
    //
    try (BufferedReader br =
             new BufferedReader(
                 new InputStreamReader(homepageResponse.getEntity().getContent()))) {
      String line;

      while ((line = br.readLine()) != null) {
        final Matcher matcher = DilbertDailyStrip.IMAGE_URL_PATTERN.matcher(line);

        if (matcher.matches()) {
          final String spec = matcher.group(1);

          if (spec.startsWith("//")) {
            result = new HttpGet(String.format("%s:%s", HTTPS_SCHEME, spec));
          } else if (spec.startsWith(HTTPS_SCHEME) || spec.startsWith(HTTP_SCHEME)) {
            result = new HttpGet(spec);
          }

          break;
        }
      }

      if (result == null) {
        // NON-NLS
        final String message =
            String.format(
                "Didn't match regular expression %s to any line in the homepage content",
                DilbertDailyStrip.IMAGE_URL_PATTERN);
        LOGGER.info(message);
        throw new IOException(message);
      }
    }

    return result;
  }

  /**
   * Indicates whether the given HTTP content-type header identifies an image
   * type supported by the plug-in.
   *
   * @param contentTypeHeader a possibly-<code>null</code> HTTP header
   * indicating a content type.
   *
   * @return <code>true</code> if the content type is recognized, otherwise
   * <code>false</code>.
   */
  private static boolean isRecognizedImageContentType(final Header contentTypeHeader) {
    boolean result = false;

    if (contentTypeHeader != null) {
      final String contentType = contentTypeHeader.getValue();
      result = ImageFileType.GIF.toString().equalsIgnoreCase(contentType) ||
               ImageFileType.JFIF.toString().equalsIgnoreCase(contentType);
    }

    return result;
  }

  /**
   * Fetches the current daily strip from the dilbert.com website if the site's
   * homepage has been modified since the state represented by a particular hash
   * value (the page's <em>ETag</em>).
   *
   * @param homepageEtag a 32-character MD5 checksum value.
   *
   * @return the current daily strip or <code>null</code> if the dilbert.com
   * homepage has not been modified since the state represented by
   * <code>homepageEtag</code>.
   *
   * @throws IOException if an error occurs fetching the strip.
   */
  public DilbertDailyStrip fetchDailyStrip(final String homepageEtag) throws IOException {
    LOGGER.info(
        "Looking for strip from a homepage with an ETag different from " + homepageEtag); // NON-NLS

    // Set a default return value
    //
    DilbertDailyStrip result = null;

    try (final CloseableHttpClient client = createHttpClient()) {
      final HttpGet homepageGet = new HttpGet(DilbertDailyStrip.DILBERT_DOT_COM_URL);

      if (homepageEtag != null) {
        homepageGet
            .addHeader(HTTP_HEADER_IF_NONE_MATCH, homepageEtag);
      }

      // Fetch the dilbert.com homepage
      //
      LOGGER.info("Executing " + homepageGet);
      try (final CloseableHttpResponse response = client.execute(homepageGet)) {
        LOGGER.info("Response status: " + response.getStatusLine());

        // If we got SC_NOT_MODIFIED (code 304) then the homepage hasn't been
        // modified
        //
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
          LOGGER.info("Homepage ETag still " + homepageEtag); // NON-NLS
          return null;
        }

        // If we get SC_OK (code 200) then we found the homepage OK and can
        // proceed
        //
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          final HttpGet stripGet = getStripURL(response);

          if (stripGet == null) {
            final String message = "Couldn't determine URL for daily strip"; // NON-NLS
            LOGGER.info(message);
            throw new IOException(message);
          }

          final Header currentETagHeader = response.getFirstHeader(HTTP_HEADER_ETAG);
          LOGGER.info("ETag header: " + (currentETagHeader == null ? null : currentETagHeader.getValue()));
          String eTag = null;

          if (currentETagHeader != null) {
            final String quotedETag = currentETagHeader.getValue();

            // The ETag header value is seen to contain "-gzip" when gzip
            // encoding is accepted by the client (which appears to be default
            // behaviour for Apache HttpComponents) but needs to be stripped out
            // for sending in an "If-None-Match" header
            //
            if (quotedETag != null) {
              eTag = quotedETag.replace(ETAG_GZIP_SUFFIX, "");
            }
          }

          if (eTag == null || !eTag.equals(homepageEtag)) {
            LOGGER.info("ETag not found or different to last known value");
            result = fetchDailyStrip(client, stripGet, eTag);
          }
        } else {
          final String message =
              MessageFormat.format(
                  HTTP_NOT_SC_OK_MESSAGE,
                  response.getStatusLine().getStatusCode(),
                  DilbertDailyStrip.DILBERT_DOT_COM_URL);
          LOGGER.info(message);
          throw new IOException(message);
        }
      }
    }

    return result;
  }

  private CloseableHttpClient createHttpClient() {
    final RequestConfig.Builder requestConfigBuilder =
        RequestConfig.custom().setConnectTimeout(CONNECTION_TIMEOUT);
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(
        requestConfigBuilder, DilbertDailyStrip.DILBERT_DOT_COM_URL);
    IdeHttpClientHelpers.ApacheHttpClient4.setProxyCredentialsForUrlIfEnabled(
        credentialsProvider, DilbertDailyStrip.DILBERT_DOT_COM_URL);

    return HttpClients.custom()
                      .setDefaultCredentialsProvider(credentialsProvider)
                      .setDefaultRequestConfig(requestConfigBuilder.build())
                      .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(SO_TIMEOUT).build())
                      .build();
  }
}
