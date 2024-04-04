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

## Version 21.9.9
* This is Early Access of 22.0.0 version (this version used due to VSCode versioning)
* Simplified LSP server startup
* Project and priming build fixes
* LSP: Do not compute text edits in source actions on `resolve` call
* Micronaut:
   * Micronaut PUT/POST Data Endpoints method generation
   * Code completion for Repository finder methods enhanced
   * Code completion for Java static members
   * Plain and Data Controllers have separate templates

## Version 21.0.0
* Improved vulnerability audit results and display 
* Number of fixes in Maven projects processing 
* Java TextMate grammar used

## Version 20.0.301
* Micronaut: 
  * Micronaut Expression Language added - Syntax highlighting, Code Completion, Go to Declaration
  * Micronaut Controllers can be based on Repository interfaces to allow data access
  * Pageable data Repositories added to New from Template multistep wizard
* Database and OCI:
  * OCI Vault support for Database applications run in OKE
  * Copy OCID for OCI resources
  * Number of bug fixes & improvements
* Numerous LSP improvements & bug fixes
* Number of fixes and improvements in project management
  * Priming build reload issues fixed
  * Using selected launch config for F5 and code lenses
  * and others...

## Version 19.0.301
* LSP: Micronaut Symbols recognized and shown in Find Symbol in VSCode
  * Open sources from Jar files as read-only from their true locations.
  * Store database connection properties in temporary file existing only during Debug/Run session of given application
* Micronaut: project improvements and bug fixes including Micronaut 4 project and annotations processing
  * Entity classes generation improvements
* JDK 21 javac support

## Version 19.0.0
* Performance improvements in Maven projects loading and priming build
* Navigation for Micronaut URI and Beans in Find Symbol
* Allow for lazy computation of CodeActions 
* Database support improvements:
  * Generating Entity classes from database improved
  * JDBC properties editing
  * Display database schema tree improvements
  * Adding OCI Autonomous database simplified
  * Allow to reenter DB username and password

## Version 18.0.0
* Java 8+ launch config renamed to Java+

## Version 17.0.301
* nb-javac 20 support
* Gradle support to work on JDK 20
* Various bug fixes

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