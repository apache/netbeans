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

var AUTOMATIC_DOWNLOAD_MESSAGE  = "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0444\u0430\u0439\u043b\u0430 \u0434\u043e\u043b\u0436\u043d\u0430 \u043d\u0430\u0447\u0430\u0442\u044c\u0441\u044f \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438.<br>\u0415\u0441\u043b\u0438 \u044d\u0442\u043e\u0433\u043e \u043d\u0435 \u043f\u0440\u043e\u0438\u0437\u043e\u0448\u043b\u043e, <a href=\"{0}\" class=\"download_link\">\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u0435 \u0435\u0433\u043e \u0437\u0434\u0435\u0441\u044c</a>.<br>";

var PRODUCT_NAME      = "NetBeans {0}";
var INSTALLER_MESSAGE = "\u041c\u0430\u0441\u0442\u0435\u0440 \u0423\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438 {0}";
var NOFILE_MESSAGE    = "\u041d\u0435 \u0432\u044b\u0431\u0440\u0430\u043d\u043e \u043d\u0438 \u043e\u0434\u043d\u043e\u0433\u043e \u0444\u0430\u0439\u043b\u0430" ;

var NETBEANS_DOWNLOAD_STARTED_PAGE_TITLE       = "\u041d\u0430\u0447\u0430\u0442\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0441\u0440\u0435\u0434\u044b NetBeans";
var NETBEANS_DOWNLOAD_STARTED_PAGE_DESCRIPTION = "\u041d\u0430\u0447\u0430\u0442\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0441\u0440\u0435\u0434\u044b NetBeans";

var NETBEANS_DOWNLOAD_STARTED_HEADER = "\u041d\u0430\u0447\u0430\u0442\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0441\u0440\u0435\u0434\u044b NetBeans {0}";
var RELEASE_NOTES_LABEL = "\u0417\u0430\u043c\u0435\u0447\u0430\u043d\u0438\u044f \u043e \u0432\u044b\u043f\u0443\u0441\u043a\u0435";
var INSTALL_NOTES_LABEL = "\u0418\u043d\u0441\u0442\u0440\u0443\u043a\u0446\u0438\u0438 \u043f\u043e \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0435";
var FIND_OUT_MORE_LABEL = "\u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f";
var PLUGINS_LABEL       = "\u041f\u043b\u0430\u0433\u0438\u043d\u044b";
var PLUGINS_DESCRIPTION = "\u041f\u0440\u0435\u0434\u0441\u0442\u0430\u0432\u043b\u0435\u043d\u044b \u0441\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u043e\u043c NetBeans \u0438 \u0441\u0442\u043e\u0440\u043e\u043d\u043d\u0438\u043c\u0438 \u0440\u0430\u0437\u0440\u0430\u0431\u043e\u0442\u0447\u0438\u043a\u0430\u043c\u0438.";
var TUTORIALS_LABEL     = " \u0420\u0443\u043a\u043e\u0432\u043e\u0434\u0441\u0442\u0432\u0430";
var TUTORIALS_DESCRIPTION = "\u041d\u0430\u043f\u0438\u0441\u0430\u043d\u044b \u0438 \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u044b \u0441\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u043e\u043c NetBeans \u0438 \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u043e\u043d\u0430\u043b\u0430\u043c\u0438.";
var TRAINING_LABEL = "\u0423\u0447\u0435\u0431\u043d\u044b\u0435 \u043a\u0443\u0440\u0441\u044b";
var TRAINING_DESCRIPTION = "\u041a\u0443\u0440\u0441\u044b \u0434\u043b\u044f \u0440\u0430\u0437\u0432\u0438\u0442\u0438\u044f \u0412\u0430\u0448\u0438\u0445 \u043d\u0430\u0432\u044b\u043a\u043e\u0432.";
var SUPPORT_LABEL        = "\u041f\u043e\u0434\u0434\u0435\u0440\u0436\u043a\u0430";
var SUPPORT_DESCRIPTION  = "\u0413\u0438\u0431\u043a\u0438\u0435 \u0443\u0441\u043b\u043e\u0432\u0438\u044f \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u043a\u0438 \u0434\u043b\u044f \u0442\u043e\u0433\u043e, \u0447\u0442\u043e\u0431\u044b \u0432\u044b \u0440\u0430\u0437\u0432\u0438\u0432\u0430\u043b\u0438 \u0441\u0432\u043e\u0438 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f.";

// $PRODUCT_NAME $TYPE (Installer for $PLATFORM/Zip)/$Language ($Language_ID) $filename ($SIZE) SHA-256: $MD5
var INFO_MESSAGE = "{0}{1} {2}/{3} ({4})<br>{5} ({6} \u041c\u0431)<br>SHA-256: {7}";
var INFO_MESSAGE_OTHER = "{0} ({1} \u041c\u0431)<br>SHA-256: {2}";

//DO NOT TRANSLATE
var SUBSCRIPTION_PHP_URL = "http://services.netbeans.org/sub.php";

