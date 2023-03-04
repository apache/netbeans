/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var jshint = require('gulp-jshint');
var gulp = require('gulp');

gulp.task('jshint:client', function () {
    return gulp.src('./client/*.js')
            .pipe(jshint({
                "strict": true,
                "lookup": false,
                "curly": true,
                "eqnull": true,
                "unused": true,
                "eqeqeq": true,
                "undef": true,
                "camelcase": true,
                "forin": true,
                "immed": true,
                "latedef": true,
                "newcap": true,
                "quotmark": "double",
                "trailing": true,
                "globals": {"angular": true, "window": true, "XMLHttpRequest": true},
                '-W097': true
            }
            ))
            .pipe(jshint.reporter('jshint-stylish'));
});

gulp.task('jshint:server', function () {
    return gulp.src('./server/**/*.js')
            .pipe(jshint({
                "lookup": false,
                "strict": true,
                "curly": true,
                "eqnull": true,
                "unused": true,
                "eqeqeq": true,
                "undef": true,
                "camelcase": true,
                "forin": true,
                "immed": true,
                "node": true,
                "latedef": true,
                "newcap": true,
                "quotmark": "double",
                "trailing": true,
                "globals": {exports: true, require: true},
                '-W097': true
            }
            ))
            .pipe(jshint.reporter('jshint-stylish'));
});

gulp.task("jshint", ["jshint:client", "jshint:server"]);
gulp.task("default", ["jshint:client", "jshint:server"]);
