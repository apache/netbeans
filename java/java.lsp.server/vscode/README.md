# Apache NetBeans Language Server Extension for VS Code

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

This is a technology preview of [Apache NetBeans](http://netbeans.org)
based extension for VS Code. Use it to get all the _goodies of NetBeans_
via the VS Code user interface! Run on __JDK8__[*], __JDK11__, __JDK15__, etc.

[*]: <http://github.com/oracle/nb-javac> "Running on JDK8 requires additional download of GPLv2 with ClassPath Exception code"

Invoke "Open Folder" action to open project directories with `pom.xml` or `build.gradle`
build scripts. Edit, compile and debug (with the __Java 8+__ debugger configuration)
the `.java` application and test files in such projects. Debug not only Java code,
but JavaScript, Python, Ruby polyglot programs at once.

## Getting Started

Follow the
[online instructions](https://cwiki.apache.org/confluence/display/NETBEANS/Apache+NetBeans+extension+for+Visual+Studio+Code)
to set your environment up to support
[typical development use-cases](https://cwiki.apache.org/confluence/display/NETBEANS/Apache+NetBeans+extension+for+Visual+Studio+Code).

## Supported Actions

* __Java: Compile Workspace__ - invoke Maven or Gradle build
* __GraalVM: Pause in Script__ - place a breakpoint into first executed polyglot script
* debugger __Java 8+__ - start test or main class on JDK8+ in polyglot mode

## Supported Options

* __netbeans.jdkhome__ - path to the JDK, see dedicated section below
* __netbeans.verbose__ - enables verbose extension logging
* __netbeans.conflict.check__ - avoid conflicts with other Java extensions, see below

## Selecting the JDK

The user projects are built, run and debugged using the same JDK which runs the
Apache NetBeans Language Server. The JDK is being searched in
following locations:

- `netbeans.jdkhome` setting (workspace then user settings)
- `java.home` setting (workspace then user settings)
- `JDK_HOME` environment variable
- `JAVA_HOME` environment variable
- current system path

As soon as one of the settings is changed, the Language Server is restarted.

## Conflicts with other Java Extensions

Apache NetBeans Language Server extension isn't the only Java supporting
extension. To avoid duplicated code completion and other misleading clashes
the extension disables certain functionality known to cause problems. This
behavior can be disabled by setting `netbeans.conflict.check` setting to `false`.

## Contributing

Read [building instructions](BUILD.md) to help Apache community to
improve the extension.

