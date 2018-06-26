/**
 * 
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
