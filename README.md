# Dilbert Daily Strip Plugin for IntelliJ Platform Products

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## About

Displays the cartoon strip of the day from the dilbert.com website in a
toolwindow in products based on the IntelliJ Platform (such as IntelliJ IDEA,
AppCode and CLion).

## History

The plugin was first released in 2005 and has periodically been updated to keep
pace with changes to the IntelliJ Platform. There was a time when there was
thought of supporting other cartoon sources and being able to display more than
just the current strip (_e.g._ the previous day's strip or a weekly digest
or...). As a result, the source code is _way_ over engineered for what it
actually does - a good example of violating the
[YAGNI](https://en.wikipedia.org/wiki/You_aren%27t_gonna_need_it) principle.
Updates often start with _"WTF was I **thinking**?!"_ as I try to get my head
around the code again.

Source code was originally tracked in a private CVS repository, migrated to Git
using `cvs2git` and hosted in a private Bitbucket repository before eventually
being moved to GitHub.

Over the years, issues have been tracked in a private Bugzilla instance, a
private JIRA instance, a private Bitbucket project and now GitHub. The chances
of issue numbers in the comment history making any sense in the context of
GitHub are slim.

## Requirements

The plugin's build script (see [build.gradle](build.gradle)) declares a
requirement for IntelliJ IDEA 2016.3 (corresponding to build 163.7743) and the
source uses `java.time` APIs introduced in Java 8.

## Installation

Pre-built binary distributions of the plugin are hosted in the JetBrains
[plugin repository](https://plugins.jetbrains.com/plugin/36-dilbert-daily-strip)
and the latest version can be installed directly from within an IntelliJ
Platform product. Alternatively, follow the instructions below to build from
source.

## Building

The plugin can be built with Gradle and the JetBrains
[gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin):

```bash
./gradlew clean buildPlugin
```

with the output being found in a zip archive in `./build/distributions/`.

The plugin can be run in a sandboxed version of IntelliJ IDEA:

```bash
./gradlew runIde
```

## Licensing

This software is licensed under the Apache License Version 2.0. Please see
[LICENSE](LICENSE) for details.
