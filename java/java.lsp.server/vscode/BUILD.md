# Developing NetBeans based Extension for VS Code

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
## Prerequisities
It is necessary to have installed:
- Ant, latest version
- Maven, latest version
- node.js, latest LTS (to build VSIX)

It is recommended to build using JDK 8.

## Getting the Code

```bash
$ git clone https://github.com/apache/netbeans.git
$ cd netbeans/
```

## Building

To build the VS Code extension invoke:

```bash
netbeans$ ant build
netbeans$ cd java/java.lsp.server
java.lsp.server$ ant build-vscode-ext
```
The resulting extension is then in the `build` directory, with the `.vsix` extension.
#### Build Options
- `-Dvsix.version=x.y.z`can be used to set release version. E.g. set this option to `12.3.0` to get proper NetBeans release version for extension.
- `-D3rdparty.modules=` property can be set to different value than `.*nbjavac.*` to not inlcude nb-javac which allows extension to run out of the box on JDK8.

The build of NetBeans VSCode extension with nb-javac included, for version 12.6.0 then looks like this:
```bash
netbeans$ ant build
netbeans$ cd java/java.lsp.server
java.lsp.server$ ant build-vscode-ext -Dvsix.version=12.6.0
```


### Building for Development

If you want to develop the extension, use these steps for building instead:

```bash
netbeans$ cd java/java.lsp.server
java.lsp.server$ ant build-lsp-server
java.lsp.server$ cd vscode
vscode$ npm install
vscode$ npm run watch
```

This target is faster than building the `.vsix` file. Find the instructions
for running and debugging below.

### Cleaning

Often it is also important to properly clean everything. Use:

```bash
java.lsp.server$ ant clean-vscode-ext
java.lsp.server$ cd ../..
netbeans$ ant clean
```

### Testing

The `java.lsp.server` module has classical (as other NetBeans modules) tests.
The most important one is [ServerTest](https://github.com/apache/netbeans/blob/master/java/java.lsp.server/test/unit/src/org/netbeans/modules/java/lsp/server/protocol/ServerTest.java)
which simulates LSP communication and checks expected replies. In addition to
that there are VS Code integration tests - those launch VS Code with the
VSNetBeans extension and check behavior of the TypeScript integration code:

```bash
java.lsp.server$ ant build-vscode-ext # first and then
java.lsp.server$ ant test-vscode-ext
```

In case you are behind a proxy, you may want to run the tests with

```bash
java.lsp.server$ npm_config_https_proxy=http://your.proxy.com:port ant test-vscode-ext
```

when executing the tests for the first time. That shall overcome the proxy
and download an instance of `code` execute the tests on.

### Eating our own Dog Food

Using the application yourself is the best way of testing! If you want to
_edit/compile/debug_ Apache NetBeans sources, there is a way. After building
the project, execute:

```bash
vscode$ npm run apisupport
```

the system connects to associated autoupdate center and downloads, installs
and enables `org.netbeans.modules.apisupport.ant` module. With such module installed
one can open Apache NetBeans projects and work with them directly from VSCode.

## Running and Debugging

Have a sample Maven project, open it in NetBeans first and select the main file for both
the Run and Debug actions.

To use the extension created for developement you can run VS Code with
following parameter:

```bash
vscode$ code --extensionDevelopmentPath=`pwd` path_to_the_maven_project
```

Or you can open the `vscode` folder in `code` directly and use **F5** to
debug the extension's *typescript code*.

To debug the *Java code*, launch the NetBeans part of the VS Code system first
and specify suitable debug arguments:

```bash
vscode$ npm run nbcode -- --jdkhome /jdk-14/ -J-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
```

Connect to the process with Java debugger, setup all breakpoints. Then launch
and connect from the VS Code extension:

```bash
vscode$ code --extensionDevelopmentPath=`pwd` path_to_the_maven_project
```

