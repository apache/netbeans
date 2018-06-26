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

/**
 * Path to ChromeDriver, see https://code.google.com/p/selenium/wiki/ChromeDriver
 */
var PATH_TO_CHROME_DRIVER = "";
/**
 * Path to Firefox (e.g. /usr/bin/firefox on Linux or C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe on Windows)
 */
var PATH_TO_FIREFOX = "";
var preferredBrowser = "chrome"; // or e.g. firefox

var chrome = require('selenium-webdriver/chrome');
var firefox = require('selenium-webdriver/firefox');

exports.get = function () {
    switch (preferredBrowser.toLowerCase()) {
        case "chrome":
            if(PATH_TO_CHROME_DRIVER.length < 1){
                throw new Error("\n\nPlease set path to ChromeDriver in browser.js, line 46 \n\n");
            }
            return new chrome.Driver({}, new chrome.ServiceBuilder(PATH_TO_CHROME_DRIVER).build());
        case "firefox":
            if(PATH_TO_FIREFOX.length < 1){
                throw new Error("\n\nPlease set path to Firefox in browser.js, line 50 \n\n");
            }
            return new firefox.Driver(new firefox.Options().setBinary(PATH_TO_FIREFOX));
        default:
            return null;
    }
};
