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

var AUTOMATIC_DOWNLOAD_MESSAGE  = "\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u306f\u81ea\u52d5\u7684\u306b\u59cb\u307e\u308a\u307e\u3059\u3002<br>\u59cb\u307e\u3089\u306a\u3044\u5834\u5408\u306f <a href=\"{0}\" class=\"download_link\">\u3053\u3053\u3092\u30af\u30ea\u30c3\u30af\u3057\u3066\u958b\u59cb\u3057\u3066\u304f\u3060\u3055\u3044</a>\u3002<br>";

var PRODUCT_NAME      = "NetBeans {0}";
var INSTALLER_MESSAGE = "{0} \u30a4\u30f3\u30b9\u30c8\u30fc\u30e9";
var NOFILE_MESSAGE    = "\u30d5\u30a1\u30a4\u30eb\u304c\u3042\u308a\u307e\u305b\u3093" ;

var NETBEANS_DOWNLOAD_STARTED_PAGE_TITLE       = "NetBeans IDE \u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3092\u958b\u59cb\u3057\u307e\u3057\u305f";
var NETBEANS_DOWNLOAD_STARTED_PAGE_DESCRIPTION = "NetBeans IDE \u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3092\u958b\u59cb\u3057\u307e\u3057\u305f";

var NETBEANS_DOWNLOAD_STARTED_HEADER = "NetBeans IDE {0} \u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3092\u958b\u59cb\u3057\u307e\u3057\u305f";
var RELEASE_NOTES_LABEL = "\u30ea\u30ea\u30fc\u30b9\u30ce\u30fc\u30c8";
var INSTALL_NOTES_LABEL = "\u30a4\u30f3\u30b9\u30c8\u30fc\u30eb\u624b\u9806";
var FIND_OUT_MORE_LABEL = "\u305d\u306e\u4ed6\u306e\u60c5\u5831";
var PLUGINS_LABEL       = "\u30d7\u30e9\u30b0\u30a4\u30f3";
var PLUGINS_DESCRIPTION = "NetBeans \u30b3\u30df\u30e5\u30cb\u30c6\u30a3\u30fc\u3084\u30b5\u30fc\u30c9\u30d1\u30fc\u30c6\u30a3\u304b\u3089\u8ca2\u732e\u3055\u308c\u305f\u30d7\u30e9\u30b0\u30a4\u30f3\u3002";
var TUTORIALS_LABEL     = "\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb";
var TUTORIALS_DESCRIPTION = "NetBeans \u30b3\u30df\u30e5\u30cb\u30c6\u30a3\u30fc\u3084\u30d7\u30ed\u30d5\u30a7\u30c3\u30b7\u30e7\u30ca\u30eb\u304c\u66f8\u3044\u305f\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u3002";
var TRAINING_LABEL = "\u30c8\u30ec\u30fc\u30cb\u30f3\u30b0";
var TRAINING_DESCRIPTION = "\u3042\u306a\u305f\u306e\u30b9\u30ad\u30eb\u5411\u4e0a\u306e\u305f\u3081\u306b\u5f79\u7acb\u3064\u30b3\u30fc\u30b9\u3002";
var SUPPORT_LABEL        = "\u30b5\u30dd\u30fc\u30c8";
var SUPPORT_DESCRIPTION  = "\u3042\u306a\u305f\u306e\u30a4\u30ce\u30d9\u30fc\u30b7\u30e7\u30f3\u3092\u624b\u52a9\u3051\u3059\u308b\u67d4\u8edf\u306a\u30b5\u30dd\u30fc\u30c8\u30aa\u30d7\u30b7\u30e7\u30f3\u3002";

// $PRODUCT_NAME $TYPE (Installer for $PLATFORM/Zip)/$Language ($Language_ID) $filename ($SIZE) SHA-256: $MD5
var INFO_MESSAGE = "{0}{1} {2}/{3} ({4})<br>{5} ({6} MB)<br>SHA-256: {7}";
var INFO_MESSAGE_OTHER = "{0} ({1} MB)<br>SHA-256: {2}";

//DO NOT TRANSLATE
var SUBSCRIPTION_PHP_URL = "http://services.netbeans.org/sub.php";

