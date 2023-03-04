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

var test = require('selenium-webdriver/testing');
var until = require('selenium-webdriver').until;
var browser = require('./browser');

test.describe('Download page test', function () {
    var driver;

    test.before(function () {
        driver = browser.get();
    });

    test.it('should find correct title', function () {
        driver.get("https://netbeans.org/downloads");
        // checking that page title contains 'NetBeans IDE download', which is not true
        driver.wait(until.titleContains('NetBeans IDE download'), 1000);
    });
    

    test.after(function () {
        driver.quit();
    });
});
