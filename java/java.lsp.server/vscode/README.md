# NBCode: A NetBeans based Extension for VSCode

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


Get all the goodies of NetBeans via the VSCode user interface!

## Building

To build the VS Code extension invoke:

```bash
netbeans$ ant build
netbeans$ cd java/java.lsp.server
java.lsp.server$ ant build-vscode-ext -D3rdparty.modules=.*nbjavac.*
```

The `3rdparty.modules` property doesn't have to be set at all.
The resulting extension is then in the `build` directory, with the `.vsix` extension.

### Building for Development

If you want to develop the extension, use these steps for building instead:

```bash
netbeans$ cd java/java.lsp.server
java.lsp.server$ ant build-lsp-server -D3rdparty.modules=.*nbjavac.*
java.lsp.server$ cd vscode
vscode$ npm install
vscode$ npm run watch
```

The `3rdparty.modules` property doesn't have to be set at all.
This target is faster than building the `.vsix` file. Find the instructions
for running and debugging below.

### Cleaning

Often it is also important to properly clean everything. Use:

```bash
java.lsp.server$ ant clean-vscode-server
java.lsp.server$ cd ../..
netbeans$ ant clean
```

## Running and Debugging

Have a sample Maven project, open it in NetBeans first and select the main file for both
the Run and Debug actions.

To use the extension created for developement you can run VSCode with
following parameter:

```bash
vscode$ code --extensionDevelopmentPath=`pwd` path_to_the_maven_project
```

Or you can open the `vscode` folder in `code` directly and use **F5** to
debug the extension's *typescript code*.

To debug the *Java code*, launch the NetBeans part of the VSCode system first
and specify suitable debug arguments:

```bash
vscode$ npm run nbcode -- --jdkhome /jdk-14/ -J-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
```

Connect to the process with Java debugger, setup all breakpoints. Then launch 
and connect from the VSCode extension:

```bash
vscode$ code --extensionDevelopmentPath=`pwd` path_to_the_maven_project
```

## Selecting the JDK

The NbCode Java part needs to run on a JDK. The JDK is being searched in
following locations:

- `netbeans.jdkhome` setting (workspace then user settings)
- `java.home` setting (workspace then user settings)
- `JDK_HOME` environment variable
- `JAVA_HOME` environment variable
- current system path

As soon as one of the settings is changed, the NbCode Java part is restarted.

## Conflicts with other Java Extensions

Apache NetBeans Language Server extension isn't the only Java supporting
extension. To avoid duplicated code completion and other misleading clashes
the extension disables certain functionality known to cause problems. This
behavior can be disabled by setting `netbeans.conflict.check` setting to `false`.
