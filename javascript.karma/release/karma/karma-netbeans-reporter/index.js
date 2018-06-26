/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
