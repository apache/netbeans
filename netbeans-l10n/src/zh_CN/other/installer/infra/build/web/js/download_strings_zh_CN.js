/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

var BUNDLED_SERVERS_GROUP_NAME = "\u7ed1\u5b9a\u7684\u670d\u52a1\u5668";
                                                   
var NETBEANS_DOWNLOAD_BUNDLES_MSG = "NetBeans IDE \u4e0b\u8f7d\u5305";
var NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG = "\u7531\u793e\u533a\u7ffb\u8bd1\u7684 NetBeans IDE \u4e0b\u8f7d\u5305";
var NETBEANS_PACKS_MSG 		  = "\u6240\u652f\u6301\u7684\u6280\u672f";

var JDK_DOWNLOAD_LINK    = "http://java.sun.com/javase/downloads/index.jsp";
var NBJDK_DOWNLOAD_LINK  = "http://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-142931.html";


var JAVA_COM_LINK        = "http://java.com/";

// var JDK_NOTE_ALL      = "\u5b89\u88c5\u548c\u8fd0\u884c PHP\u3001C/C++ \u8fd9\u4e9b NetBeans \u5305\u65f6\u9700\u8981 Java 6 \u53ca\u66f4\u9ad8\u7248\u672c\u3002\u60a8\u53ef\u4ee5\u4e0b\u8f7d<a href=\"{0}\">java.com \u4e0a\u6700\u65b0\u7684 Java \u7248\u672c</a>\u3002<br><br>\u5b89\u88c5\u548c\u8fd0\u884c Java SE\u3001Java\u3001All \u8fd9\u4e9b NetBeans \u5305\u65f6\u9700\u8981 JDK 6 \u53ca\u66f4\u9ad8\u7248\u672c\u3002 \u60a8\u53ef\u4ee5\u4e0b\u8f7d<a href=\"{1}\">\u5355\u72ec\u7684 JDK</a> \u6216\u8005\u4e0b\u8f7d\u6700\u65b0\u7684 <a href=\"{2}\">JDK \u548c NetBeans IDE Java SE \u5b89\u88c5\u5305</a>\u3002<br><br>";
var JDK_NOTE_ALL      = "HTML/JS, PHP and C/C++ NetBeans bundles include Java Runtime Environment and do not require a separate Java installation.<br><br>JDK 7 and later versions are required for installing and running the Java SE, Java EE and All NetBeans Bundles. You can download <a href=\"{1}\">standalone JDK</a> or download the latest <a href=\"{2}\">JDK with NetBeans IDE Java SE bundle</a>.<br><br>";


//var JDK_NOTE_MACOSX   = "JDK 5.0 is required for installing and running the NetBeans IDE.";
var JDK_NOTE_MACOSX   = "";

var FREE_SIZE_MESSAGE  = "\u514d\u8d39\uff0c{0}&nbsp;MB";
var NOT_AVAILABLE_SIZE = "--";

var NOTE_PREFIX    = "\u8bf7\u6ce8\u610f\uff1a";
var NOTE_AND_SEP   = "{0} \u548c {1}";
var NOTE_COMMA_SEP = "{0}\uff0c{1}";

var SINGLE_NOT_AVAILABLE_ZIP      = "{0} \u4e0d\u5728 {1} \u4e2d";
var SINGLE_NOT_AVAILABLE_BUNDLE   = "{0} \u4e0d\u5728 {1} \u4e2d";
var MULTIPLE_NOT_AVAILABLE_ZIP    = "{0} \u4e0d\u5728 {1} \u4e2d";
var MULTIPLE_NOT_AVAILABLE_BUNDLE = "{0} \u4e0d\u5728 {1} \u4e2d";

var ZIP_FILES_LIST_NAME             = "Zip \u6587\u4ef6";
var MODULE_CLUSTERS_FILES_LIST_NAME = "\u6a21\u5757\u7fa4\u96c6";

var NOTE_SOLARIS = "\u8bf7\u6ce8\u610f\uff1aJava ME \u4ec5\u53ef\u7528\u4e8e Windows\u3001Linux \u548c Mac OS X \u4e0a\u3002";
var NOTE_MACOSX  = "\u8bf7\u6ce8\u610f\uff1aJava ME \u4ec5\u53ef\u7528\u4e8e Windows\u3001Linux \u548c Mac OS X \u4e0a\u3002"; 
var NOTE_ZIP     = "\u8bf7\u6ce8\u610f\uff1a\u53ea\u6709\u67d0\u4e9b\u7279\u5b9a\u5e73\u53f0\u7684\u5b89\u88c5\u5305\u4e2d\u5305\u542b GlassFish \u548c Apache Tomcat\u3002";
var NOTE_ALL     = "\u8bf7\u6ce8\u610f\uff1a\u5728\u6b64\u5e73\u53f0\u4e0a\u4e0d\u652f\u6301\u53d8\u6210\u7070\u8272\u7684\u6280\u672f\u3002"; 

var MORE_LANGUAGES    = "\u66f4\u591a\u8bed\u8a00\u7248\u672c...";
var COMMUNITY_MESSAGE = "\u6b64\u9875\u9762\u53ef\u4e0b\u8f7d\u7684\u5404\u79cd\u8bed\u8a00\u7248\u672c\u5b8c\u5168\u7531 NetBeans \u793e\u533a\u8d21\u732e\u3002\u4e86\u89e3\u66f4\u591a\u5173\u4e8e\u7ffb\u8bd1\u91cf\u7684\u4fe1\u606f\u53ef\u53c2\u8003<a href=\"http://wiki.netbeans.org/TFL10nCommunityStatus\">\u793e\u533a\u7ffb\u8bd1\u72b6\u6001</a>\u9875\u9762\u3002\u8981\u60f3\u4e0b\u8f7d\u793e\u533a\u6700\u65b0\u7684\u8d21\u732e\uff0c\u8bf7\u8bbf\u95ee<a href=\"http://wiki.netbeans.org/TFLocalizationDevelopmentUC\">\u672c\u5730\u5316\u63d2\u4ef6</a>\u9875\u9762\u3002";
var COMMUNITY_CONTRIBUTED_SEP    = " -- \u7531\u793e\u533a\u8d21\u732e -- " ;
var SUN_SUPPORTED_SEP            = " -- \u7531 Oracle \u652f\u6301 -- " ;

var NETBEANS_DOWNLOAD_PAGE_TITLE       = "NetBeans IDE \u4e0b\u8f7d";
var NETBEANS_DOWNLOAD_PAGE_DESCRIPTION = "NetBeans IDE \u4e0b\u8f7d";

var NETBEANS_DOWNLOAD_HEADER = "NetBeans IDE {0} \u4e0b\u8f7d";
var DEVELOPMENT_TITLE        = "\u5f00\u53d1\u7248";
var ARCHIVE_TITLE            = "\u65e9\u671f\u7248";
var PYTHON_TITLE             = "Python&nbsp;EA";
var EMAIL_LABEL              = "\u7535\u5b50\u90ae\u4ef6\u5730\u5740\uff08\u53ef\u9009\uff09\uff1a";
var SUBSCRIBE_LABEL          = "\u8ba2\u9605\u65b0\u95fb\u90ae\u4ef6\uff1a";
var MONTHLY_LABEL            = "\u6bcf\u6708&nbsp;&nbsp;&nbsp;&nbsp;";
var WEEKLY_LABEL             = "\u6bcf\u5468";
var CONTACT_LABEL            = "NetBeans&nbsp;\u53ef\u4f7f\u7528\u6b64\u5730\u5740\u8054\u7cfb\u6211"; 
var LANGUAGE_LABEL           = "\u8bed\u8a00\uff1a"; 
var PLATFORM_LABEL           = "\u5e73\u53f0\uff1a"; 
var PLUGIN_MANAGER_LABEL     = "\u60a8\u53ef\u4ee5\u7a0d\u540e\u901a\u8fc7 IDE \u63d2\u4ef6\u7ba1\u7406\u5668\uff08\u5de5\u5177|\u63d2\u4ef6\uff09\u6dfb\u52a0\u6216\u8005\u5220\u9664\u8f6f\u4ef6\u5305\u3002<br><br>"; 
var JAVATOOLSBUNDLE_LABEL    = "\u60a8\u4e5f\u53ef\u4ee5\u4e0b\u8f7d\u5305\u542b NetBeans IDE \u7684 <a href=\"http://java.sun.com/javaee/downloads/index.jsp\">Java EE 5 \u5de5\u5177\u5305</a> \u6216 <a href=\"http://download.netbeans.org/netbeans/6.1/mysql_bundle/\">MySQL GlassFish \u5b89\u88c5\u5305</a>\u3002";
var SOURCE_BINARY_LABEL      = "NetBeans \u6e90\u4ee3\u7801\u548c\u4e8c\u8fdb\u5236\u751f\u6210\u6587\u4ef6\uff08\u4e0d\u5305\u62ec\u96c6\u6210\u7684\u8fd0\u884c\u73af\u5883\uff09\u8fd8\u4ee5<a id=\"zip_link\" href=\"{0}\">zip \u6587\u4ef6\u683c\u5f0f</a>\u5b58\u5728\u3002";
var BUILD_WIKI_LABEL         = "\u8bf7\u53c2\u89c1<a href=\"http://wiki.netbeans.org/wiki/view/WorkingWithNetBeansSources\">\u5982\u4f55\u7531\u6e90\u6587\u4ef6\u751f\u6210 IDE \u7684\u8bf4\u660e</a>\u6216<a href=\"{0}\">\u5b89\u88c5\u6307\u5bfc</a>\u3002";
var PLATFORM_DEV_LABEL = "\u53ef\u4ee5\u4f7f\u7528 NetBeans IDE \u4e2d\u7684 Java SE \u6765\u5f00\u53d1\u57fa\u4e8e NetBeans \u5e73\u53f0\u7684\u5e94\u7528\u7a0b\u5e8f\u3002\u66f4\u591a\u76f8\u5173\u4fe1\u606f\u8bf7\u89c1 <a href=\"{0}\">NetBeans \u5e73\u53f0</a>\u3002";
var PLATFORM_INFO_LINK       = "http://platform.netbeans.org/platform-get.html";

var LICENSE_NOTES_LINK      = "http://www.netbeans.org/about/legal/product-licences.html";

var ARCHIVE_BUILDS_LINK      = "http://www.netbeans.info/downloads/dev.php";
var DEVELOPMENT_BUILDS_LINK  = "http://bits.netbeans.org/download/trunk/nightly/latest/";
var PYTHON_LINK              = "http://download.netbeans.org/netbeans/6.5/python/ea/";

// TRANSLATE NOTE: change download*.gif to download*_<locale>.gif
var DOWNLOAD_BUTTON_NORMAL    = "download_zh_CN.gif";
var DOWNLOAD_BUTTON_DISABLED  = "download_d_zh_CN.gif";
var DOWNLOAD_BUTTON_HIGHLIGHT = "download_h_zh_CN.gif";

// DO NOT TRANSLATE
var START_PAGE = "start.html";
var ZIP_PAGE   = "zip.html";
