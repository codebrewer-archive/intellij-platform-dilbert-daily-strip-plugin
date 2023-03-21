# Dilbert Daily Strip Plugin for IntelliJ Platform Products

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## Attention!

**This plugin no longer works and is archived.**

## About

_Used_ to display the cartoon strip of the day from the dilbert.com website in a
tool window in products based on the IntelliJ Platform (such as IntelliJ IDEA,
AppCode and CLion). Stopped working in March 2023 when the cartoon strip's
author changed how the strip is made available.

## History

The plugin was first released in 2005 and was periodically updated to keep
pace with changes to the IntelliJ Platform. There was a time when there was
thought of supporting other cartoon sources and being able to display more than
just the current strip (_e.g._ the previous day's strip or a weekly digest
or...). As a result, the source code is _way_ over-engineered for what it
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

The plugin's build script (see [build.gradle.kts](build.gradle.kts)) declares a
requirement for IntelliJ IDEA 223.8836.41 Community Edition (corresponding to
build IC-223.8836.41 as of the plugin version 2.0.0 release) and the source code
is compiled for Java 17, per JetBrains requirements for a plugin targeting
2022.2 or later.

## Installation

Pre-built binary distributions of the plugin are hosted in the JetBrains
[plugin repository](https://plugins.jetbrains.com/plugin/36-dilbert-daily-strip)
and the latest version can be installed directly from within an IntelliJ
Platform product (but the plugin may have been removed by the time you are
reading this). Alternatively, follow the instructions below to build from
source.

## Building

Note: you probably want to branch from the `1.10.1` tag since `master` now
contains a stripped-down plugin that displays only a warning message that strips
can no longer be fetched.

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
