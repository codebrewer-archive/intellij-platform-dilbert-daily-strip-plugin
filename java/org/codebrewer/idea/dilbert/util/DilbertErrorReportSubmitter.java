/*
 *  Copyright 2005 Mark Scott
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

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codebrewer.idea.dilbert.DilbertDailyStripPlugin;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * A class that can report plugin errors to the author.  This class requires
 * HTTP access to the Internet in order to submit a form to a CGI server
 * and receive a response in return.
 *
 * @author Mark Scott
 * @version $Revision$ $Date$
 */
public final class DilbertErrorReportSubmitter extends ErrorReportSubmitter
{
  static {
    // Set the system property that the Jakarta Commons HttpClient uses for its
    // user-agent string
    //
    System.getProperties().setProperty("httpclient.useragent",
        ResourceBundleManager.getLocalizedString(DilbertDailyStripPlugin.class, "plugin.name"));
  }

  /**
   * Regular expression pattern that makes a reasonable effort at matching an
   * email address.
   */
  private static final String EMAIL_ADDRESS_PATTERN =
      "^[\\p{Alnum}._%-]+@[\\p{Alnum}.-]+\\.\\p{Alpha}{2,4}$";

  /**
   * URI of CGI script that accepts error reports for this plugin.
   */
  private static final String ERROR_SUBMISSION_URI =
      "http://ccgi.codebrewer.com/cgi-bin/idea-single-error-handler.cgi";

  /**
   * A logger for sending output to IDEA's log file.
   */
  private static final Logger LOGGER =
      Logger.getInstance(DilbertDailyStripPlugin.class.getName());

  /**
   * Constant that can be used when an error report submission fails.
   * @noinspection NonFinalStaticVariableUsedInClassInitialization
   */
  private static final SubmittedReportInfo FAILED_SUBMISSION_INFO =
      new SubmittedReportInfo(null,
          null,
          SubmittedReportInfo.SubmissionStatus.FAILED);

  /**
   * Constant that can be used when an error report submission succeeds.
   * @noinspection NonFinalStaticVariableUsedInClassInitialization
   */
  private static final SubmittedReportInfo SUCCEEDED_SUBMISSION_INFO =
      new SubmittedReportInfo(null,
          null,
          SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);

  /**
   * The email address of the user submitting the error report.
   */
  private static String userEmailAddress;

  /**
   * A guess at the upper bound for the length of this class's
   * {@link #toString()} method.
   */
  private static final int TO_STRING_SIZE_ESTIMATE = 128;

  /**
   * Gets the email address, if any, entered by the user when dismissing the
   * error submission confirmation dialog.  If a non-null, non-zero-length
   * string is returned then it will match the regular expression pattern given
   * by {@link #EMAIL_ADDRESS_PATTERN}.
   *
   * @return <code>null</code> if the user cancelled the dialog when it was
   *         last shown, a zero-length string if the user chose not to enter an
   *         email address or an email address that matches the pattern
   *         {@link #EMAIL_ADDRESS_PATTERN}.
   */
  private static synchronized String getUserEmailAddress()
  {
    return userEmailAddress;
  }

  private static synchronized void setUserEmailAddress(final String newUserEmailAddress)
  {
    assert newUserEmailAddress.matches(EMAIL_ADDRESS_PATTERN);
    DilbertErrorReportSubmitter.userEmailAddress = newUserEmailAddress;
  }

  public String getReportActionText()
  {
    return ResourceBundleManager.getLocalizedString(DilbertErrorReportSubmitter.class, "error.idea_statusbar.text");
  }

  public SubmittedReportInfo submit(final IdeaLoggingEvent[] events,
                                    final Component parentComponent)
  {
    String newUserEmailAddress = getUserEmailAddress();
    newUserEmailAddress = Messages.showInputDialog(parentComponent,
        ResourceBundleManager.getLocalizedString(DilbertErrorReportSubmitter.class, "error.submission_dialog.message"),
        ResourceBundleManager.getLocalizedString(DilbertErrorReportSubmitter.class, "error.submission_dialog.title"),
        null,
        newUserEmailAddress,
        new InputValidator()
        {
          // a reasonable check for a valid email address
          private boolean isEmailAddress(final String inputString)
          {
            return inputString.matches(EMAIL_ADDRESS_PATTERN);
          }

          // true if the input is valid
          public boolean canClose(final String inputString)
          {
            return checkInput(inputString);
          }

          // input is OK if it's zero-length or an email address
          public boolean checkInput(final String inputString)
          {
            return "".equals(inputString) || isEmailAddress(inputString);
          }
        });

    // store whatever was entered so that the same value can be used when the
    // dialog is next shown
    //
    setUserEmailAddress(newUserEmailAddress);

    // this method's return value, possibly set non-null below
    //
    SubmittedReportInfo info = null;

    if (newUserEmailAddress != null) {
      // User didn't cancel the dialog (but the email address could be "",
      // indicating that the user doesn't wish to submit an email address)
      //
      LOGGER.info("Submitting plug-in error report from <" + newUserEmailAddress + '>');

      final PostMethod method = new PostMethod(ERROR_SUBMISSION_URI);
      method.addParameter("sender", newUserEmailAddress);
      final IdeaLoggingEvent event = events[0];
      method.addParameter("message", event.getMessage());
      method.addParameter("error", event.getThrowableText());

      try {
        final HttpClient client = new HttpClient();
        client.executeMethod(method);
        method.releaseConnection();
        final int statusCode = method.getStatusCode();

        // The CGI script that accepts error report submissions will
        // return a response having one of two status codes :
        //
        //   SC_NO_CONTENT  - to indicate that the report was submitted
        //                    successfully
        //   SC_BAD_REQUEST - to indicate that there was an error submitting
        //                    the report
        //
        // Of course, the remote server could send a status code independently
        // of the CGI script.
        //
        LOGGER.info("Plug-in error submission HTTP status code: " + statusCode);

        switch (statusCode) {
          case HttpStatus.SC_NO_CONTENT:
            info = SUCCEEDED_SUBMISSION_INFO;
            break;
          case HttpStatus.SC_BAD_REQUEST:
            // fall through
          default:
            final Object[] messageArgs = new Object[]{
                HttpStatus.getStatusText(statusCode),
                new Integer(statusCode) };
            final String messageTemplate = new StringBuffer(
                ResourceBundleManager.getLocalizedString(
                    DilbertErrorReportSubmitter.class, "error.error_dialog.message.1"))
                .append("\n\n")
                .append(ResourceBundleManager.getLocalizedString(
                    DilbertErrorReportSubmitter.class, "error.error_dialog.message.2")).toString();
            Messages.showErrorDialog(parentComponent,
                MessageFormat.format(messageTemplate, messageArgs),
                ResourceBundleManager.getLocalizedString(
                    DilbertErrorReportSubmitter.class, "error.error_dialog.title"));
            info = FAILED_SUBMISSION_INFO;
        }
      }
      catch (HttpException he){
        LOGGER.debug("HttpException submitting error report: " + he.getMessage());

        Messages.showErrorDialog(parentComponent,
            ResourceBundleManager.getLocalizedString(DilbertErrorReportSubmitter.class, "error.error_dialog.message.1"),
            ResourceBundleManager.getLocalizedString(DilbertErrorReportSubmitter.class, "error.error_dialog.title"));
        info = FAILED_SUBMISSION_INFO;
      }
      catch (IOException e) {
        LOGGER.debug("IOException submitting error report: " + e.getMessage());

        Messages.showErrorDialog(parentComponent,
            ResourceBundleManager.getLocalizedString(DilbertErrorReportSubmitter.class, "error.error_dialog.message.1"),
            ResourceBundleManager.getLocalizedString(DilbertErrorReportSubmitter.class, "error.error_dialog.title"));
        info = FAILED_SUBMISSION_INFO;
      }
    }
    else {
      LOGGER.info("Plug-in error submission cancelled by user");
    }

    return info;
  }

  public String toString()
  {
    return new StringBuffer(TO_STRING_SIZE_ESTIMATE)
        .append(DilbertErrorReportSubmitter.class.getName())
        .append("[email=<")
        .append(getUserEmailAddress())
        .append(">]")
        .toString();
  }
}