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

var util = require('util');

var NetBeansReporter = function(baseReporterDecorator) {
    baseReporterDecorator(this);

    this.BROWSER_START = '$NB$netbeans browserStart name=$NB$%s$NB$';
    this.BROWSER_END = '$NB$netbeans browserEnd name=$NB$%s$NB$';
    this.BROWSER_ERROR = '$NB$netbeans browserError browser=$NB$%s$NB$ error=$NB$%s$NB$';
    this.SUITE_START = '$NB$netbeans suiteStart browser=$NB$%s$NB$ name=$NB$%s$NB$';
    this.SUITE_END = '$NB$netbeans suiteEnd browser=$NB$%s$NB$ name=$NB$%s$NB$';
    this.TEST_PASS = '$NB$netbeans testPass browser=$NB$%s$NB$ name=$NB$%s$NB$ duration=$NB$%s$NB$';
    this.TEST_IGNORE = '$NB$netbeans testIgnore browser=$NB$%s$NB$ name=$NB$%s$NB$';
    this.TEST_FAILURE = '$NB$netbeans testFailure browser=$NB$%s$NB$ name=$NB$%s$NB$ details=$NB$%s$NB$ duration=$NB$%s$NB$';

    this.browserResults = {};


    this.onRunStart = function(browsers) {
        var self = this;
        browsers.forEach(function(browser) {
            self.initializeBrowserResult(self, browser);
        });
    };

    this.onBrowserStart = function(browser) {
        this.initializeBrowserResult(this, browser);
    };

    this.onBrowserError = function(browser, error) {
        this.printMessage(this.BROWSER_ERROR, browser, JSON.stringify(error));
    };

    this.initializeBrowserResult = function(self, browser) {
        if (self.browserResults[browser.id]) {
            return;
        }
        self.browserResults[browser.id] = {
            name: browser.name,
            started: false,
            lastSuite: null
        };
    };

    this.specSuccess = function(browser, result) {
        this.checkBrowser(browser.id);
        this.checkSuite(browser, result);
        this.printMessage(this.TEST_PASS, browser.name, result.description, result.time);
    };

    this.specFailure = function(browser, result) {
        this.checkBrowser(browser.id);
        this.checkSuite(browser, result);
        this.printMessage(this.TEST_FAILURE, browser.name, result.description, JSON.stringify(result.log), result.time);
    };

    this.specSkipped = function(browser, result) {
        this.checkBrowser(browser.id);
        this.checkSuite(browser, result);
        this.printMessage(this.TEST_IGNORE, browser.name, result.description);
    };

    this.onRunComplete = function() {
        var self = this;
        Object.keys(this.browserResults).forEach(function(browserId) {
            self.checkBrowser(browserId);
            var browserResult = self.browserResults[browserId];
            if (browserResult.lastSuite) {
                self.printMessage(self.SUITE_END, browserResult.name, browserResult.lastSuite);
            }
            self.printMessage(self.BROWSER_END, browserResult.name);
        });
        self.browserResults = {};
    };

    this.checkBrowser = function(browserId) {
        var browserResult = this.browserResults[browserId];
        if (!browserResult.started) {
            this.printMessage(this.BROWSER_START, browserResult.name);
            browserResult.started = true;
        }
    };

    this.checkSuite = function(browser, result) {
        var browserResult = this.browserResults[browser.id];
        var suiteName = result.suite.join(' ');
        if (browserResult.lastSuite !== suiteName) {
            if (browserResult.lastSuite) {
                this.printMessage(this.SUITE_END, browserResult.name, browserResult.lastSuite);
            }
            browserResult.lastSuite = suiteName;
            this.printMessage(this.SUITE_START, browserResult.name, suiteName);
        }
    };

    this.printMessage = function() {
        var args = Array.prototype.slice.call(arguments);
        this.write(util.format.apply(null, args) + '\n');
    };

};

NetBeansReporter.$inject = ['baseReporterDecorator'];

module.exports = {
    'reporter:netbeans': ['type', NetBeansReporter]
};
