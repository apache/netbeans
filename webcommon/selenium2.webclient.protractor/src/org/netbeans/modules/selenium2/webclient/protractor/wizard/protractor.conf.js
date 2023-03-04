<#--

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

<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

exports.config = {
  
  seleniumServerJar: null,

  seleniumPort: null,
  
  seleniumArgs: [],
  
  chromeDriver: null,
 
  seleniumAddress: null,

  sauceUser: null,
  sauceKey: null,
 
  sauceSeleniumAddress: null,

  directConnect: false,
  
  firefoxPath: null,

  chromeOnly: false,

  exclude: [],

  capabilities: {
    browserName: 'chrome',

    count: 1,

    shardTestFiles: false,

    maxInstances: 1,

    specs: [],

    exclude: []

  },

  multiCapabilities: [],

  maxSessions: -1,

  baseUrl: 'http://localhost:9876',

  rootElement: 'body',

  allScriptsTimeout: 11000,

  getPageTimeout: 10000,

  beforeLaunch: function() {
  },

  onPrepare: function() {
  },

  onComplete: function() {
  },

  onCleanUp: function(exitCode) {},

  afterLaunch: function() {},

  params: {
    login: {
      user: 'Jane',
      password: '1234'
    }
  },

  resultJsonOutputFile: null,

  restartBrowserBetweenTests: false,

  framework: 'jasmine',

  jasmineNodeOpts: {
    isVerbose: false,
    showColors: true,
    includeStackTrace: true,
    defaultTimeoutInterval: 30000
  },

  mochaOpts: {
    ui: 'bdd',
    reporter: 'list'
  },

  // Options to be passed to Cucumber.
  cucumberOpts: {
    require: 'cucumber/stepDefinitions.js',
    tags: '@dev',
    format: 'summary'
  }
};
