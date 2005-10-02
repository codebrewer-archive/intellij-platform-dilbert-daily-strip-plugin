#!/usr/bin/perl -w

#  Copyright 2005 Mark Scott
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

#
# CGI script to accept error reports submitted by, for example, an IDEA
# ErrorReportSubmitter.
#
# The script expects to be sent three request parameters :
#
#  - error   : a Java Throwable's stacktrace
#  - message : an error message (typically a one line summary)
#  - sender  : the submitter's email address
#
# All request parameters must be present (but may be empty).
#
# The 'message' parameter can be set from the result of
# IdealLoggingEvent.getMessage().
#
# The 'error' parameter can be set from the result of
# IdeaLoggingEvent.getThrowableText().
#
# No body content is returned but the success or failure of the script is
# indicated by the HTTP status code :
#
#  - 204 (No Response) indicates that the report was submitted successfully;
#  - 400 (Bad Request) indicates that one or more of the expected request
#                      parameters was missing.
#

use strict;
use CGI qw(:cgi);
use Mail::Mailer;

$CGI::POST_MAX = 1024 * 100; # max 100kB posts
$CGI::DISABLE_UPLOADS = 1;   # no file uploads

my $cgi = new CGI;
my @expected = qw(error message sender); # expected request parameters, sorted
my $expected_param_count = @expected;    # the number of expected request parameters
my $sender;                              # the sender's email address
my $message;                             # the error message
my $error;                               # the stacktrace

my @received = sort $cgi->param; # sort the received parameters alphabetically

if ((@received == @expected) && (join(" ", @received) eq join(" ", @expected))) {
  # all expected request parameters are present
  $sender = $cgi->param("sender");
  $message = $cgi->param("message");
  $error = $cgi->param("error");

  my $from;
  if ($sender eq "") {
    $from = "Anonymous IDEA Plug-in User <sender\@example.com>";
  } else {
    $from = "IDEA Plug-in User <$sender>";
  }
  my $to = "Plug-in Author <recipient\@example.com>";
  my $subject = "IDEA Plug-in Error Report: $message";

  #my $mailer = new Mail::Mailer "sendmail" or die "Unable to create new mailer object:$!\n";
  my $mailer = Mail::Mailer -> new("smtp", Server => "smtp.example.com") or die "Unable to create new mailer object:$!\n";
  $mailer -> open({From => $from, To => $to, Subject => $subject}) or die "Unable to populate mailer object:$!\n";
  print $mailer $error;
  $mailer -> close;

  # send a 204 reply so the client knows the request
  # succeeded but there's no response body coming
  print $cgi->header(-expires=>'now',
                     -status=>'204 No Response');
} else {
  # send a 400 request to indicate that one or more
  # expected parameters is missing
  print $cgi->header(-expires=>'now',
                     -status=>'400 Bad Request');
}
