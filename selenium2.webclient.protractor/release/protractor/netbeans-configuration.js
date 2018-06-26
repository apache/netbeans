/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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

