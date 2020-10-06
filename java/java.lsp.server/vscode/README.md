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

This is an extension for VS Code. Based on "lsp-sample" from:
https://github.com/microsoft/vscode-extension-samples

# Building

To build the VS Code extension do:

```bash
netbeans$ cd java/java.lsp.server
java.lsp.server$ ant build-vscode-ext
```

The resulting extension is then in the `build` directory, with the `.vsix` extension.

# Building for Development

If you want to develop the extension, use these steps for building instead:

```bash
netbeans$ cd java/java.lsp.server
java.lsp.server$ ant build-lsp-server
java.lsp.server$ cd vscode
vscode$ npm install
vscode$ npm run compile
```

# Running and Debugging

To use the extension created for developement you can run VSCode with
following parameter:

```bash
vscode$ code --extensionDevelopmentPath=`pwd` name_of_folder_to_open
```

Or you can open the `vscode` folder in `code` directly and use **F5** to
debug the extension's typescript code.

The idea when debugging Java code is to launch the NetBeans part of the LSP
system first, provide suitable debug arguments:

```bash
vscode$ ./nb-java-lsp-server/bin/nb-java-lsp-server -J-agentlib:jdwp=transport=dt_socket,server=y,address=8000
```

and then connect to with debugger setup all breakpoints and then also connect
from the VSCode extension:

```bash
vscode$ code --extensionDevelopmentPath=`pwd` name_of_folder_to_open
```
