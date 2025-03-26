<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# Apache NetBeans

Apache NetBeans is an open source development environment, tooling platform, and application framework.

### Build status
   * GitHub actions
     * [![Build Status](https://github.com/apache/netbeans/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/apache/netbeans/actions/workflows/main.yml)
     * [![Profiler Lib Native Binaries](https://github.com/apache/netbeans/actions/workflows/native-binary-build-lib.profiler.yml/badge.svg?branch=master)](https://github.com/apache/netbeans/actions/workflows/native-binary-build-lib.profiler.yml)
     * [![NetBeans Native Execution Libraries](https://github.com/apache/netbeans/actions/workflows/native-binary-build-dlight.nativeexecution.yml/badge.svg?branch=master)](https://github.com/apache/netbeans/actions/workflows/native-binary-build-dlight.nativeexecution.yml)
   * Apache Jenkins:
     * Linux: [![Build Status](https://ci-builds.apache.org/job/Netbeans/job/netbeans-linux/badge/icon)](https://ci-builds.apache.org/job/Netbeans/job/netbeans-linux/)
     * Windows: [![Build Status](https://ci-builds.apache.org/job/Netbeans/job/netbeans-windows/badge/icon)](https://ci-builds.apache.org/job/Netbeans/job/netbeans-windows) 
   * License Status ( Apache Rat and ant verify-libs-and-licenses )
     * [![Build Status](https://ci-builds.apache.org/job/Netbeans/job/netbeans-license/badge/icon)](https://ci-builds.apache.org/job/Netbeans/job/netbeans-license/)

### Requirements

  * Git
  * Ant
  * JDK 17 or above (to build and run NetBeans)

#### Notes:

* NetBeans license violation checks are managed via the [rat-exclusions.txt](https://github.com/apache/netbeans/blob/master/nbbuild/rat-exclusions.txt) file.
* Set JAVA_HOME and ANT_HOME appropriately or leave them undefined.

### Building NetBeans

Build the default `release` config (See the [cluster.config](https://github.com/apache/netbeans/blob/ab66c7fdfdcbf0bde67b96ddb075c83451cdd1a6/nbbuild/cluster.properties#L19) property.)
```
$ ant build
```
Build the basic project (mainly Java features):
```
$ ant -Dcluster.config=basic build
```
Build the full project (may include clusters which are not be in the release):
```
$ ant -Dcluster.config=full build
```
Build the NetBeans Platform:
```
$ ant -Dcluster.config=platform build
```
Cleanup:
```
$ ant -q clean
```

#### Notes:
* You can also use `php`, `enterprise`, etc. See the [cluster.properties](https://github.com/apache/netbeans/blob/master/nbbuild/cluster.properties) file.
* Once built, you can simply open individual modules of interest with NetBeans and run/rebuild/debug them like any other project

#### Generating Javadoc

Build javadoc:
```
$ ant build javadoc
```

**Note** Run `javadoc-nb` task in Netbeans to run the javadoc build and display it in a web browser.

### Running NetBeans

Run the build:
```
$ ant tryme
```

**Note:** Look in nbbuild/netbeans for the NetBeans installation created by the build process.

### Get In Touch

 * [Subscribe](mailto:users-subscribe@netbeans.apache.org) or [mail](mailto:users@netbeans.apache.org) the [users@netbeans.apache.org](mailto:users@netbeans.apache.org) list - Ask questions, find answers, and also help other users.
 * [Subscribe](mailto:dev-subscribe@netbeans.apache.org) or [mail](mailto:dev@netbeans.apache.org) the [dev@netbeans.apache.org](mailto:dev@netbeans.apache.org) list - Join development discussions, propose new ideas and connect with contributors.

### Download

 * [Developer builds](https://ci-builds.apache.org/job/Netbeans/job/netbeans-linux/lastSuccessfulBuild/artifact/nbbuild/) on Jenkins (NetBeans-dev-xxx.zip).
 * [Latest release](https://netbeans.apache.org/download) (convenience binary of released source artifacts).

### Reporting Bugs

 * [How to report bugs](https://netbeans.apache.org/participate/report-issue)

### Log, Config and Cache Locations

 * start config (JVM settings, default JDK, userdir, cachedir location and more):  
   `netbeans/etc/netbeans.conf`
 * user settings storage (preferences, installed plugins, logs):  
   system dependent, see `Help -> About` for concrete location
 * cache files (maven index, search index etc):  
   system dependent, see `Help -> About` for concrete location
 * default log location (tip: can be inspected via `View -> IDE Log`):  
   `$DEFAULT_USERDIR_ROOT/var/log/messages.log`

**Note:** removing/changing the user settings directory will reset NetBeans to first launch defaults

### Other Repositories

 * [NetBeans website repos](https://github.com/apache/netbeans-antora?tab=readme-ov-file#basic-structure)
 * [NBPackage](https://github.com/apache/netbeans-nbpackage)
 * [NetBeans maven utilities](https://github.com/apache/netbeans-mavenutils-nbm-maven-plugin)
 * [NetBeans maven archetypes](https://github.com/apache/netbeans-mavenutils-archetypes)

### Full History

The origins of the code in this repository are older than its Apache existence.
As such significant part of the history (before the code was donated to Apache)
is kept in an independent repository. To fully understand the code
you may want to merge the modern and ancient versions together:

```bash
$ git clone https://github.com/apache/netbeans.git
$ cd netbeans
$ git log platform/uihandler/arch.xml
```

This gives you just few log entries including the initial checkin and
change of the file headers to Apache. But then the magic comes:

```bash
$ git remote add emilian https://github.com/emilianbold/netbeans-releases.git
$ git fetch emilian # this takes a while, the history is huge!
$ git replace 6daa72c98 32042637 # the 1st donation
$ git replace 6035076ee 32042637 # the 2nd donation
```

When you search the log, or use the blame tool, the full history is available:

```bash
$ git log platform/uihandler/arch.xml
$ git blame platform/uihandler/arch.xml
```

Many thanks to Emilian Bold who converted the ancient history to his
[Git repository](https://github.com/emilianbold/netbeans-releases)
and made the magic possible!
