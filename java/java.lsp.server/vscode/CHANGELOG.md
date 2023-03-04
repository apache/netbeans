# Change Log

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
## Version 17.0.0
* Various bug fixes

## Version 16.0.301
* Multistep wizard added to connect to Autonomous DB 
* Fixes for obtaining projects information
* Temporary fix: Support extra cluster directories.
* Support for GraalVM MX projects updated

## Version 16.0.1
* Gradle 7.6 support to work on JDK 19
* Proxy autodection and configuration for Maven & Gradle

## Version 15.0.301
* Native image CE debugger works on aarch64
* Organize Imports fixed for clashing star imports
* Auto import of types in code completion
* Added inline redundant variable hint
* Improvements to Maven and Gradle projects to report project artifacts
* Return all dependencies for Maven and Gradle projects
* Support Maven and Gradle vulnerability audits in OCI a proxy to nvd.nist.gov
* NetBeans should not auto-insert `\n\` in Groovy triple quoted strings 
* nb-javac 19 support

## Version 15.0
* Open Type command added
* Present project view files as tree leaves
* Minor improvements in OCI ui and VSCode icon mapping

## Version 14.0.301
* Settings: `NetBeans:UserDir` is set to `Local` as default value. This means each instance of VSCode runs own VSNetBeans LS
* Format Document and Format Selection added 
* External formatters using Eclipse, Google and Spring added.
* JavaDoc completion added
* Groovy Go To Symbol added
* Several refactorings added
* Settings are Remote-SSH aware
* Native Image debugging provided by GraalVM works now for GraalVM CE and EE
* Number of bug fixes and improvements in Java, projects and databases support

## Version 14.0
* Workaround for VSCode 1.67 error which breaks Projects explorer icon
* Remove HTML tags from project problem messages
* Fixes for Gradle projects and LSP

## Version 13.0.301
* Added base code completion for Spock test framework
  * Spock Block Names are offered inside methods if the class extends Spock Specification
  * Parameter names are offered, if the parameters are defined in a method name that is annotated with @Unroll
* Added Outline view for Groovy files
* Significant improvements & bug fixes working with Gradle projects 
* Info on project problems shown in VSCode 
* Move refactoring updated to use rich form style
* Oracle Cloud Explorer added
* Database Explorer added for JDBC databases including wallet authentication style
* Micronaut and JPA Entity classes supported together with Repository classes
* GraalVM native image debugging fixes and improvements
* Java Call Hierarchy added

## Version 12.6.301
* Project Explorer to display logical project structure, dependencies and project files introduced
* Dedicated form for Change method parameters refactoring 
* Organize Imports added, see `Netbeans > Java > Imports:` _settings_ for customizations
* Setting `NetBeans: Userdir = global | local` added to run Language Server per workspace or globally for user
* Run Configurations panel added to allow intuitive configuration of Run & Debug
* Java Outline view provides all details for selected file
* Numerous bug fixes and stability improvements in all areas

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
