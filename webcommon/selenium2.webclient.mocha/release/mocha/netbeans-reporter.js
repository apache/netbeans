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

/**
 * Module dependencies.
 */

var MOCHA_DIR = process.env.MOCHA_DIR;

var Base = require(MOCHA_DIR + '/lib/reporters/base')
  , cursor = Base.cursor
  , color = Base.color;

/**
 * Expose `NetbeansReporter`.
 */

exports = module.exports = NetbeansReporter;

/**
 * Initialize a new `NetbeansReporter` reporter.
 *
 * @param {Runner} runner
 * @api public
 */

function NetbeansReporter(runner) {
  Base.call(this, runner);

  var self = this
    , stats = this.stats
    , n = 1
    , passes = 0
    , failures = 0
    , skipped = 0
    , REPORTER_MESSAGE = 'mocha-netbeans-reporter ';

  runner.on('start', function(){
    var total = runner.grepTotal(runner.suite);
    console.log(REPORTER_MESSAGE + '%d..%d', 1, total);
  });

  runner.on('test end', function(){
    ++n;
  });

  runner.on('pending', function(test){
    skipped++;
    console.log(REPORTER_MESSAGE + 'ok %d %s # SKIP -, suite=%s, testcase=%s', n, title(test), test.parent.fullTitle(), test.title);
  });

  runner.on('pass', function(test){
    passes++;
    console.log(REPORTER_MESSAGE + 'ok %d %s, suite=%s, testcase=%s, duration=%s', n, title(test), test.parent.fullTitle(), test.title, test.duration);
  });

  runner.on('fail', function(test, err){
    failures++;
    console.log(REPORTER_MESSAGE + 'not ok %d %s, suite=%s, testcase=%s, duration=%s', n, title(test), test.parent.fullTitle(), test.title, test.duration);
    if (err.stack) console.log(err.stack.replace(/^/gm, '  '));
  });

  runner.on('end', function(){
    console.log(REPORTER_MESSAGE + 'tests ' + (passes + failures)+ ', pass ' + passes + ', fail ' + failures + ', skip ' + skipped);
  });
}

/**
 * Return a safe title of `test`
 *
 * @param {Object} test
 * @return {String}
 * @api private
 */

function title(test) {
  return test.fullTitle().replace(/#/g, '');
}
