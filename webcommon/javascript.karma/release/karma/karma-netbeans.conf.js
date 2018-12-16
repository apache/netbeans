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

var FILE_SEPARATOR = process.env.FILE_SEPARATOR;
var PROJECT_CONFIG = process.env.PROJECT_CONFIG;
var BASE_DIR = process.env.BASE_DIR;
var AUTOWATCH = Boolean(process.env.AUTOWATCH);
var KARMA_NETBEANS_REPORTER = process.env.KARMA_NETBEANS_REPORTER;
var COVERAGE = Boolean(process.env.COVERAGE);
var COVERAGE_DIR = process.env.COVERAGE_DIR;
var DEBUG = Boolean(process.env.DEBUG);

var BROWSERS_MESSAGE = '$NB$netbeans browsers %s';

var util = require('util');
var projectConf = require(PROJECT_CONFIG);

var printMessage = function() {
    var args = Array.prototype.slice.call(arguments);
    process.stdout.write(util.format.apply(null, args) + '\n');
};
var arrayUnique = function(input) {
    return input.filter(function (e, i, arr) {
        return arr.lastIndexOf(e) === i;
    });
};
var arrayRemove = function(input, value) {
    var index = input.indexOf(value);
    if (index > -1) {
        input.splice(index, 1);
    }
};

module.exports = function(config) {
    projectConf(config);

    if (DEBUG) {
        printMessage('NetBeans: Coverage is automatically disabled in Karma Debug mode.');
    }

    // base path
    if (config.basePath) {
        if (config.basePath.substr(0, 1) === '/' // unix
                || config.basePath.substr(1, 2) === ':\\') { // windows
            // noop
        } else {
            config.basePath = BASE_DIR + FILE_SEPARATOR + config.basePath;
        }
    } else {
        config.basePath = BASE_DIR + FILE_SEPARATOR;
    }

    config.reporters = config.reporters || [];
    config.reporters.push('netbeans');
    if (COVERAGE) {
        config.reporters.push('coverage');
    } else if (DEBUG) {
        arrayRemove(config.reporters, 'coverage');
    }
    config.reporters = arrayUnique(config.reporters);

    config.plugins = config.plugins || [];
    config.plugins.push(KARMA_NETBEANS_REPORTER);
    if (COVERAGE) {
        config.plugins.push('karma-coverage');
    }
    config.plugins = arrayUnique(config.plugins);

    printMessage(BROWSERS_MESSAGE, config.browsers.join(','));

    config.colors = true;

    config.autoWatch = AUTOWATCH;

    config.singleRun = false;

    if (COVERAGE) {
        var nbCoverageReporter = {
            type: 'clover',
            dir: COVERAGE_DIR + FILE_SEPARATOR,
            file: 'clover.xml'
        };
        if (config.coverageReporter) {
            if (config.coverageReporter.reporters) {
                config.coverageReporter.reporters.push(nbCoverageReporter);
            } else {
                config.coverageReporter = {
                    reporters: [
                        nbCoverageReporter,
                        config.coverageReporter
                    ]
                };
            }
        } else {
            config.coverageReporter = nbCoverageReporter;
        }
    } else if (DEBUG
            && config.preprocessors) {
        for (var property in config.preprocessors) {
            var prep = config.preprocessors[property];
            if (typeof prep === 'object'
                    && prep.constructor === Array) {
                arrayRemove(prep, 'coverage');
            }
        }
    }
};
