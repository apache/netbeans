<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# CPPLite

A lightweight C/C++ support for NetBeans.

This project currently adds a simple C/C++ project, C/C++ syntax highlighting and support for [the _ccls_ language server protocol server](https://github.com/MaskRay/ccls) to get editing features like code completion.

## Opening a C/C++ project

When opening a C/C++ project for the first time, one needs to choose C/C++ category, Lightweight C/C++ Project. Then select the directory where project exist, on the next wizard page, provide the location of
[compile_commands.json](https://sarcasm.github.io/notes/dev/compilation-database.html), and on the next wizard page, it is possible to specify commands to build the project.

The ccls location needs to be specified in Tools/Options, C/C++ tab.

