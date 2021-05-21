/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var path = require('path');
var SPECS = process.env.SPECS.split(', ');
var JASMINE_NB_REPORTER = process.env.JASMINE_NB_REPORTER;
var USER_CONFIG_FILE = process.env.USER_CONFIGURATION_FILE;
var USER_CONFIG = require(USER_CONFIG_FILE).config;

var SELENIUM_SERVER_JAR = null;
if(USER_CONFIG.seleniumServerJar) {
    SELENIUM_SERVER_JAR = path.join(path.dirname(USER_CONFIG_FILE), USER_CONFIG.seleniumServerJar);
}

var CHROME_DRIVER = null;
if(USER_CONFIG.chromeDriver) {
    CHROME_DRIVER = path.join(path.dirname(USER_CONFIG_FILE), USER_CONFIG.chromeDriver);
}

exports.config = {

  seleniumServerJar: SELENIUM_SERVER_JAR,

  seleniumPort: USER_CONFIG.seleniumPort || null,

  seleniumArgs: USER_CONFIG.seleniumArgs || [],

  chromeDriver: CHROME_DRIVER,

  seleniumAddress: USER_CONFIG.seleniumAddress || null,

  sauceUser: USER_CONFIG.sauceUser || null,
  sauceKey: USER_CONFIG.sauceKey || null,

  sauceSeleniumAddress: USER_CONFIG.sauceSeleniumAddress || null,

  directConnect: USER_CONFIG.directConnect || false,

  firefoxPath: USER_CONFIG.firefoxPath || null,

  chromeOnly: USER_CONFIG.chromeOnly || false,

  specs: SPECS,

  exclude: USER_CONFIG.exclude || [],

  suites: USER_CONFIG.suites,

  capabilities: USER_CONFIG.capabilities || {

    browserName: 'chrome',

    count: 1,

    shardTestFiles: false,

    maxInstances: 1,

    specs: ['spec/chromeOnlySpec.js'],

    exclude: ['spec/doNotRunInChromeSpec.js']

  },

  multiCapabilities: USER_CONFIG.multiCapabilities || [],

  maxSessions: USER_CONFIG.maxSessions || -1,

  baseUrl: USER_CONFIG.baseUrl || 'http://localhost:9876',

  rootElement: USER_CONFIG.rootElement || 'body',

  allScriptsTimeout: USER_CONFIG.allScriptsTimeout || 11000,

  getPageTimeout: USER_CONFIG.getPageTimeout || 10000,

  beforeLaunch: function() {
    if(typeof USER_CONFIG.beforeLaunch === 'function') {
        USER_CONFIG.beforeLaunch();
    }
  },

  onPrepare: function() {
    if(typeof USER_CONFIG.onPrepare === 'function') {
      USER_CONFIG.onPrepare();
    }
    require(JASMINE_NB_REPORTER);
    jasmine.getEnv().addReporter(new jasmine.NetbeansReporter());
  },

  onComplete: function() {
    if(typeof USER_CONFIG.onComplete === 'function') {
      USER_CONFIG.onComplete();
    }
  },

  onCleanUp: function(exitCode) {
    if(typeof USER_CONFIG.onCleanUp === 'function') {
      USER_CONFIG.onCleanUp(exitCode);
    }
  },

  afterLaunch: function() {
    if(typeof USER_CONFIG.afterLaunch === 'function') {
      USER_CONFIG.afterLaunch();
    }
  },

  params: USER_CONFIG.params || {
    login: {
      user: 'Jane',
      password: '1234'
    }
  },

  resultJsonOutputFile: USER_CONFIG.resultJsonOutputFile || null,

  restartBrowserBetweenTests: USER_CONFIG.restartBrowserBetweenTests || false,

  framework: USER_CONFIG.framework || 'jasmine',

  jasmineNodeOpts: USER_CONFIG.jasmineNodeOpts || {
    isVerbose: true,
    showColors: true,
    includeStackTrace: true,
    defaultTimeoutInterval: 30000
  },

  mochaOpts: USER_CONFIG.mochaOpts || {
    ui: 'bdd',
    reporter: 'list'
  },

  cucumberOpts: USER_CONFIG.cucumberOpts || {
    require: 'cucumber/stepDefinitions.js',
    tags: '@dev',
    format: 'summary'
  }
};

