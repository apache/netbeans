# Change Log

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
## Version 12.5.301
* Number of refactorings added
* Attach to Native Image launch config added for Native Image Debugger
* Native Image debugger visually distinguishes when Java code was not included in binary by native-image tool.
* Groovy Support for Spock tests enabled by default
* Java 8+ launch config accepts arguments for ENV and CWD
* JDK17 support
* Supporting VS Code 1.59 official API and UI for running tests

## Version 12.4.301

* New Project action added
* New From Template action added
* Debugger attach supported
* Number of bug fixes

## Version 12.4

* Numerous fixes in Code Completion and Code navigation, like Go To Super implementation, Find all implementations,...
* Go To Definition works for Gradle projects 
* GDB version check for proper version usable in native-image debugger
* mx project fixes

## Version 12.4 Beta

* Test results UI added using Test Explorer UI extension
* Micronaut projects support added
  * YAML configuration files code completion
* Maven multi-project projects support
* Gradle projects improvements
  * Multi-project projects supported
  * Priming build is run and project remembered as trusted
  * Support for explicit parameters for the application and its VM
* GDB debugging using CppLite for C/C++ and Native-Image
* Number of bug fixes

## Version 12.3

* LSP codelens for Run and Debug of main and test methods
* NETBEANS-5319 - Always do save modified files when used through LSP
* Project problems and improvements in headless environment for VSNetBeans
  * Showing completions returned by annotation Processors in VS Code
  * Project problems are resolved in headless environment
  * Restart NBLS when an extension providing some NB cluster is installed/uninstalled
  * Enable full VSNetBeans Maven build test
  * Don't show reload/save dialogs in VSNetBeans
  * Properly stop Maven execution from LSP/DAP 

## Version 12.2.1

* Numerous refactorings for Java source code editing added
* Progress bar for long running operation like build actions
* Ability to cancel selected operations
* Maven projects: Go to Definition source code action downloads and open Java source
* JavaDoc is shown when mouse hoovers over symbol
