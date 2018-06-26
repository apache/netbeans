/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

(function() {
    if (! jasmine) {
        throw new Exception("jasmine library does not exist in global namespace!");
    }

    /**
     * Usage:
     *
     * jasmine.getEnv().addReporter(new jasmine.NetbeansReporter());
     * jasmine.getEnv().execute();
     */
    var NetbeansReporter = function() {
        this.started = false;
        this.finished = false;
        this.REPORTER_MESSAGE = 'jasmine-netbeans-reporter ';
    };

    NetbeansReporter.prototype = {

        reportRunnerStarting: function(runner) {
            this.started = true;
            this.start_time = (new Date()).getTime();
            this.n = 0;
            this.passes = 0;
            this.failures = 0;
            this.skipped = 0;
            // should have at least 1 spec, otherwise it's considered a failure
            this.log(this.REPORTER_MESSAGE + '1..'+ Math.max(runner.specs().length, 1));
        },

        reportSpecStarting: function(spec) {
            this.start_time_spec = (new Date()).getTime();
            this.n++;
        },

        reportSpecResults: function(spec) {
            var dur = (new Date()).getTime() - this.start_time_spec;

            var results = spec.results();

            if (results.passed()) {
                this.passes++;
                this.log(this.REPORTER_MESSAGE + "ok " + this.n + " " + spec.suite.description + " " + spec.description + ", suite=" + spec.suite.description + ", testcase=" + spec.description + ", duration=" + dur);
            } else if (results.skipped) {
                this.skipped++;
                this.log(this.REPORTER_MESSAGE + "ok " + this.n + " " + spec.suite.description + " " + spec.description + " # SKIP -, suite=" + spec.suite.description + ", testcase=" + spec.description);
            } else {
                this.failures++;
                this.log(this.REPORTER_MESSAGE + "not ok " + this.n + " " + spec.suite.description + " " + spec.description + ", suite=" + spec.suite.description + ", testcase=" + spec.description + ", duration=" + dur);
                var items = results.getItems();
                var i = 0;
                var errorMessage = '';
                var expectationResult, stackMessage;
                while (expectationResult = items[i++]) {
                    if (expectationResult.trace) {
                        stackMessage = expectationResult.trace.stack ? expectationResult.trace.stack : expectationResult.message;
                        errorMessage += '\n  '+ stackMessage;
                    }
                }
                // add REPORTER_MESSAGE in the beginning of each stacktrace line
                // so that they can be filtered out in TestRunnerReporter
                errorMessage = errorMessage.replace(/\n/g, '\n' + this.REPORTER_MESSAGE);
                this.log(errorMessage);
            }
        },

        reportRunnerResults: function(runner) {
            this.log(this.REPORTER_MESSAGE + 'tests ' + (this.passes + this.failures)+ ', pass ' + this.passes + ', fail ' + this.failures + ', skip ' + this.skipped);
            this.finished = true;
        },

        log: function(str) {
            var console = jasmine.getGlobal().console;
            if (console && console.log) {
                console.log(str);
            }
        }
    };

    // export public
    jasmine.NetbeansReporter = NetbeansReporter;
})();
