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

var BUNDLED_SERVERS_GROUP_NAME = "\u30d0\u30f3\u30c9\u30eb&nbsp;\u30b5\u30fc\u30d0\u30fc";
                                                   
var NETBEANS_DOWNLOAD_BUNDLES_MSG = "NetBeans IDE \u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u30d0\u30f3\u30c9\u30eb";
var NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG = "NetBeans IDE \u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u30d0\u30f3\u30c9\u30eb(\u30b3\u30df\u30e5\u30cb\u30c6\u30a3\u306b\u3088\u308b\u8a00\u8a9e)";
var NETBEANS_PACKS_MSG 		  = "\u30b5\u30dd\u30fc\u30c8\u30c6\u30af\u30ce\u30ed\u30b8\u30fc";

var JDK_DOWNLOAD_LINK    = "http://java.sun.com/javase/ja/6/download.html";
var NBJDK_DOWNLOAD_LINK  = "http://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-142931.html";


var JAVA_COM_LINK        = "http://java.com/";

//var JDK_NOTE_ALL      = "NetBeans \u3092\u30a4\u30f3\u30b9\u30c8\u30fc\u30eb\u3057 PHP\u3001C/C++ \u306e NetBeans \u30d0\u30f3\u30c9\u30eb\u3092\u5b9f\u884c\u3059\u308b\u306b\u306f Java 6 \u4ee5\u964d\u306e\u30d0\u30fc\u30b8\u30e7\u30f3\u304c\u5fc5\u8981\u3067\u3059\u3002<a href=\"{0}\">\u6700\u65b0\u306e Java \u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9</a>\u304b\u3089\u53d6\u5f97\u3067\u304d\u307e\u3059\u3002<br><br>NetBeans \u3092\u30a4\u30f3\u30b9\u30c8\u30fc\u30eb\u3057 Java SE\u3001Java EE \u304a\u3088\u3073\u3059\u3079\u3066\u306e\u30d0\u30f3\u30c9\u30eb\u3092\u5b9f\u884c\u3059\u308b\u306b\u306f JDK 6 \u4ee5\u4e0a\u306e\u30d0\u30fc\u30b8\u30e7\u30f3\u304c\u5fc5\u8981\u3067\u3059\u3002<a href=\"{1}\">\u6700\u65b0\u306e JDK \u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9</a>\u304b\u3089\u53d6\u5f97\u3067\u304d\u307e\u3059\u3002\u307e\u305f\u306f <a href=\"{2}\">JDK \u3068 NetBeans IDE \u306e Java SE \u30d0\u30f3\u30c9\u30eb</a> \u3092\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3067\u304d\u307e\u3059\u3002<br><br>";
var JDK_NOTE_ALL      = "HTML/JS\u3001PHP \u304a\u3088\u3073 C/C++ \u306e NetBeans \u306b\u306f JRE \u304c\u542b\u307e\u308c\u3066\u304a\u308a Java \u3092\u5225\u9014\u30a4\u30f3\u30b9\u30c8\u30fc\u30eb\u3059\u308b\u5fc5\u8981\u306f\u3042\u308a\u307e\u305b\u3093\u3002<br> <br>Java SE\u3001Java EE \u304a\u3088\u3073\u3059\u3079\u3066\u306e\u30d0\u30f3\u30c9\u30eb\u3092\u5b9f\u884c\u3059\u308b\u306b\u306f JDK 7 \u4ee5\u4e0a\u306e\u30d0\u30fc\u30b8\u30e7\u30f3\u304c\u5fc5\u8981\u3067\u3059\u3002<a href=\"{1}\">\u6700\u65b0\u306e JDK</a> \u304b\u3089\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3067\u304d\u307e\u3059\u3002\u307e\u305f\u306f <a href=\"{2}\">JDK \u3068 NetBeans IDE Java SE \u30d0\u30f3\u30c9\u30eb</a> \u3092\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3067\u304d\u307e\u3059\u3002<br><br>"; 
//var JDK_NOTE_MACOSX   = "JDK 5.0 is required for installing and running the NetBeans IDE.";
var JDK_NOTE_MACOSX   = "";

var FREE_SIZE_MESSAGE  = "\u7121\u511f,&nbsp;{0}&nbsp;MB";
var NOT_AVAILABLE_SIZE = "--";

var NOTE_PREFIX    = "\u6ce8: ";
var NOTE_AND_SEP   = "{0} \u304a\u3088\u3073 {1}";
var NOTE_COMMA_SEP = "{0}, {1}";

var SINGLE_NOT_AVAILABLE_ZIP      = "{0} \u306f {1} \u306b\u306f\u542b\u307e\u308c\u3066\u3044\u307e\u305b\u3093";
var SINGLE_NOT_AVAILABLE_BUNDLE   = "{0} \u306f {1} \u3067\u306f\u5229\u7528\u3067\u304d\u307e\u305b\u3093";
var MULTIPLE_NOT_AVAILABLE_ZIP    = "{0} \u306f {1} \u306b\u306f\u542b\u307e\u308c\u3066\u3044\u307e\u305b\u3093";
var MULTIPLE_NOT_AVAILABLE_BUNDLE = "{0} \u306f {1} \u3067\u306f\u5229\u7528\u3067\u304d\u307e\u305b\u3093";

var ZIP_FILES_LIST_NAME             = "ZIP \u30d5\u30a1\u30a4\u30eb";
var MODULE_CLUSTERS_FILES_LIST_NAME = "\u30e2\u30b8\u30e5\u30fc\u30eb\u30af\u30e9\u30b9\u30bf";

var NOTE_SOLARIS = "\u6ce8 : Java ME \u306f Windows, Linux \u304a\u3088\u3073 Mac OS X \u30d7\u30e9\u30c3\u30c8\u30d5\u30a9\u30fc\u30e0\u3067\u5229\u7528\u3067\u304d\u307e\u3059\u3002";
var NOTE_MACOSX  = "\u6ce8 : Java ME \u306f Windows, Linux \u304a\u3088\u3073 Mac OS X \u30d7\u30e9\u30c3\u30c8\u30d5\u30a9\u30fc\u30e0\u3067\u5229\u7528\u3067\u304d\u307e\u3059\u3002";
var NOTE_ZIP     = "\u6ce8: GlassFish \u3068 Apache Tomcat \u306f\u30d7\u30e9\u30c3\u30c8\u30d5\u30a9\u30fc\u30e0\u3054\u3068\u306e\u30a4\u30f3\u30b9\u30c8\u30fc\u30e9\u3067\u306e\u307f\u5229\u7528\u3067\u304d\u307e\u3059";
var NOTE_ALL     = "\u6ce8: \u30b0\u30ec\u30a4\u30a2\u30a6\u30c8\u3055\u308c\u3066\u3044\u308b\u30c6\u30af\u30ce\u30ed\u30b8\u30fc\u306f\u3053\u306e\u30d7\u30e9\u30c3\u30c8\u30d5\u30a9\u30fc\u30e0\u3067\u306f\u30b5\u30dd\u30fc\u30c8\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002";

var MORE_LANGUAGES    = "\u305d\u306e\u4ed6\u306e\u8a00\u8a9e...";
var COMMUNITY_MESSAGE = "NetBeans \u30b3\u30df\u30e5\u30cb\u30c6\u30a3\u306b\u3088\u308a\u8ca2\u732e\u3055\u308c\u305f\u7ffb\u8a33\u8a00\u8a9e\u3092\u3053\u306e\u30da\u30fc\u30b8\u304b\u3089\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3067\u304d\u307e\u3059\u3002\u7ffb\u8a33\u7bc4\u56f2\u306b\u3064\u3044\u3066\u306f<a href=\"http://wiki.netbeans.org/TFL10nCommunityStatus\">\u30c6\u30fc\u30bf\u30b9\u30da\u30fc\u30b8</a>\u3092\u3054\u89a7\u4e0b\u3055\u3044\u3002\u6700\u65b0\u306e\u958b\u767a\u30d3\u30eb\u30c9\u3092\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u3059\u308b\u306b\u306f<a href=\"http://wiki.netbeans.org/TFLocalizationDevelopmentUC\">\u30d7\u30e9\u30b0\u30a4\u30f3\u30da\u30fc\u30b8</a>\u3092\u53c2\u7167\u3057\u3066\u304f\u3060\u3055\u3044\u3002";
var COMMUNITY_CONTRIBUTED_SEP    = " -- \u30b3\u30df\u30e5\u30cb\u30c6\u30a3\u30fc\u306b\u3088\u308b\u8ca2\u732e -- " ;
var SUN_SUPPORTED_SEP            = " -- Oracle \u306b\u3088\u308b\u30b5\u30dd\u30fc\u30c8 -- " ;

var NETBEANS_DOWNLOAD_PAGE_TITLE       = "NetBeans IDE \u30c0\u30a6\u30f3\u30ed\u30fc\u30c9";
var NETBEANS_DOWNLOAD_PAGE_DESCRIPTION = "NetBeans IDE \u30c0\u30a6\u30f3\u30ed\u30fc\u30c9";



var NETBEANS_DOWNLOAD_HEADER = "NetBeans IDE {0} \u30c0\u30a6\u30f3\u30ed\u30fc\u30c9";
var DEVELOPMENT_TITLE        = "\u958b\u767a\u7248";
var ARCHIVE_TITLE            = "\u30a2\u30fc\u30ab\u30a4\u30d6";
var EMAIL_LABEL              = "Email&nbsp;\u30a2\u30c9\u30ec\u30b9&nbsp;(\u4efb\u610f):&nbsp;";
var SUBSCRIBE_LABEL          = "\u30cb\u30e5\u30fc\u30b9\u30ec\u30bf\u30fc\u3092\u8cfc\u8aad:";
var MONTHLY_LABEL            = "\u6708\u3054\u3068&nbsp;&nbsp;&nbsp;&nbsp;";
var WEEKLY_LABEL             = "\u9031\u3054\u3068";
var CONTACT_LABEL            = "NetBeans \u304c\u3053\u306e\u30a2\u30c9\u30ec\u30b9\u5b9b\u306b\u9023\u7d61\u3057\u3066\u3082\u3088\u3044";
var LANGUAGE_LABEL           = "IDE \u306e\u8a00\u8a9e:";
var PLATFORM_LABEL           = "\u30d7\u30e9\u30c3\u30c8\u30d5\u30a9\u30fc\u30e0:";
var PLUGIN_MANAGER_LABEL     = "\u30a4\u30f3\u30b9\u30c8\u30fc\u30eb\u5f8c\u306f IDE \u306e\u30d7\u30e9\u30b0\u30a4\u30f3\u30de\u30cd\u30fc\u30b8\u30e3\u30fc (\u30c4\u30fc\u30eb | \u30d7\u30e9\u30b0\u30a4\u30f3) \u3067\u30d1\u30c3\u30af\u3092\u8ffd\u52a0\u3001\u524a\u9664\u3067\u304d\u307e\u3059\u3002<br><br>";
var JAVATOOLSBUNDLE_LABEL    = "\u307e\u305f <a href=\"http://java.sun.com/javaee/downloads/index.jsp\">Java EE 5 Tools Bundle</a> \u3084 <a href=\"http://download.netbeans.org/netbeans/6.1/mysql_bundle/\">MySQL GlassFish Bundle</a> \u306e\u4e00\u90e8\u3068\u3057\u3066 NetBeans IDE \u306e\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u304c\u3067\u304d\u307e\u3059\u3002";
var SOURCE_BINARY_LABEL      = "NetBeans \u306e\u30bd\u30fc\u30b9\u30b3\u30fc\u30c9\u3068\u3001\u5b9f\u884c\u74b0\u5883\u306e\u30d0\u30f3\u30c9\u30eb\u3092\u9664\u304f\u30d0\u30a4\u30ca\u30ea\u30d3\u30eb\u30c9\u306f <a id=\"zip_link\" href=\"{0}\">zip \u30d5\u30a1\u30a4\u30eb\u5f62\u5f0f</a> \u3067\u3082\u30c0\u30a6\u30f3\u30ed\u30fc\u30c9\u53ef\u80fd\u3067\u3059\u3002";
var BUILD_WIKI_LABEL         = "\u30bd\u30fc\u30b9\u304b\u3089 IDE \u3092\u30d3\u30eb\u30c9\u3059\u308b\u306b\u306f\u3001<a href=\"http://wiki.netbeans.org/wiki/view/WorkingWithNetBeansSources\">\u624b\u9806</a>\u3092\u53c2\u7167\u3057\u3066\u304f\u3060\u3055\u3044\u3002";
var PLATFORM_DEV_LABEL       = "NetBeans IDE \u306e Java SE \u3067 NetBeans \u30d7\u30e9\u30c3\u30c8\u30d5\u30a9\u30fc\u30e0\u3092\u57fa\u306b\u3057\u305f\u30a2\u30d7\u30ea\u30b1\u30fc\u30b7\u30e7\u30f3\u3092\u958b\u767a\u3067\u304d\u307e\u3059\u3002\u8a73\u7d30\u306f <a href=\"{0}\">NetBeans \u30d7\u30e9\u30c3\u30c8\u30d5\u30a9\u30fc\u30e0</a> \u3092\u53c2\u7167\u3057\u3066\u304f\u3060\u3055\u3044\u3002";
var PLATFORM_INFO_LINK       = "http://platform.netbeans.org/platform-get.html";

var LICENSE_NOTES_LINK      = "http://www.netbeans.org/about/legal/product-licences.html";

var ARCHIVE_BUILDS_LINK      = "http://www.netbeans.info/downloads/dev.php";
var DEVELOPMENT_BUILDS_LINK  = "http://bits.netbeans.org/download/trunk/nightly/latest/";

// TRANSLATE NOTE: change download*.gif to download*_<locale>.gif
var DOWNLOAD_BUTTON_NORMAL    = "download_ja.gif";
var DOWNLOAD_BUTTON_DISABLED  = "download_d_ja.gif";
var DOWNLOAD_BUTTON_HIGHLIGHT = "download_h_ja.gif";

// DO NOT TRANSLATE
var START_PAGE = "start.html";
var ZIP_PAGE   = "zip.html";
