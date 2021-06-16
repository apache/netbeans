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

# Apache NetBeans Launchers

This directory allows to build and release NetBeans' Windows launchers on the Maven central.

To produce a source release:

```
$ ant -DVERSION=<VERSION> build-source-zip
```

To build binaries, only Linux OS is known to work, using the MinGW project. On Ubuntu,
this may work to install suitable MinGW:

```
$ apt install mingw-w64 mingw-w64-tools
```

The command to build the launchers:

```
$ ant -DVERSION=<VERSION> build-launchers
```

To publish the launchers to a Maven repository:

```
$ ant -DVERSION=<VERSION> build-and-publish
```
