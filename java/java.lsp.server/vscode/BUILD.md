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

