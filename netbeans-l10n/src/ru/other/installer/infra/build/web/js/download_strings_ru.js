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

var BUNDLED_SERVERS_GROUP_NAME = "\u041f\u043e\u0441\u0442\u0430\u0432\u043b\u044f\u0435\u043c\u044b\u0435&nbsp;\u0441\u0435\u0440\u0432\u0435\u0440\u044b";
                                                   
var NETBEANS_DOWNLOAD_BUNDLES_MSG = "\u0421\u0431\u043e\u0440\u043a\u0438 \u0438\u043d\u0442\u0435\u0433\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u043e\u0439 \u0441\u0440\u0435\u0434\u044b NetBeans";
var NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG = "\u041f\u0435\u0440\u0435\u0432\u0435\u0434\u0451\u043d\u043d\u044b\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u043e\u043c \u0441\u0431\u043e\u0440\u043a\u0438 \u0441\u0440\u0435\u0434\u044b NetBeans";
var NETBEANS_PACKS_MSG 		  = "\u041f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u043c\u044b\u0435&nbsp;\u0442\u0435\u0445\u043d\u043e\u043b\u043e\u0433\u0438\u0438";

var JDK_DOWNLOAD_LINK    = "http://java.sun.com/javase/downloads/index.jsp";
var NBJDK_DOWNLOAD_LINK  = "http://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-142931.html";


var JAVA_COM_LINK        = "http://java.com/";

// var JDK_NOTE_ALL      = "\u0414\u043b\u044f \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438 \u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u044f \u0441\u0431\u043e\u0440\u043e\u043a NetBeans \u0434\u043b\u044f PHP \u0438 C/C++ \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u0430 \u0432\u0435\u0440\u0441\u0438\u044f Java 6 \u0438\u043b\u0438 \u0432\u044b\u0448\u0435. <a href=\"{0}\">\u0422\u0435\u043a\u0443\u0449\u0443\u044e \u0432\u0435\u0440\u0441\u0438\u044e \u043c\u043e\u0436\u043d\u043e \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0441 \u0441\u0430\u0439\u0442\u0430 java.com</a>.<br><br>\u0414\u043b\u044f \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438 \u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u044f \u0441\u0431\u043e\u0440\u043e\u043a NetBeans \u0434\u043b\u044f Java SE, Java EE \u0438 \u0441\u0431\u043e\u0440\u043a\u0438 \u0441 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u043a\u043e\u0439 \u0432\u0441\u0435\u0445 \u0442\u0435\u0445\u043d\u043e\u043b\u043e\u0433\u0438\u0439 \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c JDK 6 \u0438\u043b\u0438 \u0432\u044b\u0448\u0435. \u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c <a href=\"{1}\">\u043e\u0442\u0434\u0435\u043b\u044c\u043d\u0443\u044e \u0441\u0431\u043e\u0440\u043a\u0443 JDK</a> \u0438\u043b\u0438 \u0442\u0435\u043a\u0443\u0449\u0443\u044e \u0432\u0435\u0440\u0441\u0438\u044e <a href=\"{2}\">\u0441\u0431\u043e\u0440\u043a\u0438 JDK \u0432\u043c\u0435\u0441\u0442\u0435 \u0441\u043e \u0441\u0440\u0435\u0434\u043e\u0439 NetBeans \u0434\u043b\u044f Java SE</a>.<br><br>";
var JDK_NOTE_ALL      = "HTML/JS, PHP and C/C++ NetBeans bundles include Java Runtime Environment and do not require a separate Java installation.<br><br>JDK 7 and later versions are required for installing and running the Java SE, Java EE and All NetBeans Bundles. You can download <a href=\"{1}\">standalone JDK</a> or download the latest <a href=\"{2}\">JDK with NetBeans IDE Java SE bundle</a>.<br><br>";
//var JDK_NOTE_MACOSX   = "JDK 5.0 is required for installing and running the NetBeans IDE.";
var JDK_NOTE_MACOSX   = "";

var FREE_SIZE_MESSAGE  = "\u0411\u0435\u0441\u043f\u043b\u0430\u0442\u043d\u043e, {0}&nbsp;MB";
var NOT_AVAILABLE_SIZE = "--";

var NOTE_PREFIX    = "\u0417\u0430\u043c\u0435\u0447\u0430\u043d\u0438\u0435: ";
var NOTE_AND_SEP   = "{0} \u0438 {1}";
var NOTE_COMMA_SEP = "{0}, {1}";

var SINGLE_NOT_AVAILABLE_ZIP      = "{0} \u043d\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0432 {1}";
var SINGLE_NOT_AVAILABLE_BUNDLE   = "{0} \u043d\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0434\u043b\u044f {1}";
var MULTIPLE_NOT_AVAILABLE_ZIP    = "{0} \u043d\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b \u0432 {1}";
var MULTIPLE_NOT_AVAILABLE_BUNDLE = "{0} \u043d\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b \u0434\u043b\u044f {1}";

var ZIP_FILES_LIST_NAME             = "\u0410\u0440\u0445\u0438\u0432\u044b Zip";
var MODULE_CLUSTERS_FILES_LIST_NAME = "\u041a\u043b\u0430\u0441\u0442\u0435\u0440\u044b \u043c\u043e\u0434\u0443\u043b\u0435\u0439";

var NOTE_SOLARIS = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435: Java ME \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0434\u043b\u044f Windows, Linux \u0438 Mac OS X.";
var NOTE_MACOSX  = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435: Java ME \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0434\u043b\u044f Windows, Linux \u0438 Mac OS X.";
var NOTE_ZIP     = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435: GlassFish \u0438 Apache Tomcat \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043f\u043b\u0430\u0442\u0444\u043e\u0440\u043c\u0435\u043d\u043d\u043e-\u0437\u0430\u0432\u0438\u0441\u0438\u043c\u044b\u0445 \u0441\u0431\u043e\u0440\u043a\u0430\u0445.";
var NOTE_ALL     = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435: \u0422\u0435\u0445\u043d\u043e\u043b\u043e\u0433\u0438\u0438, \u043e\u0442\u043c\u0435\u0447\u0435\u043d\u043d\u044b\u0435 \u0441\u0435\u0440\u044b\u043c \u0446\u0432\u0435\u0442\u043e\u043c, \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b \u0434\u043b\u044f \u0434\u0430\u043d\u043d\u043e\u0439 \u043f\u043b\u0430\u0442\u0444\u043e\u0440\u043c\u044b.";

var MORE_LANGUAGES    = "\u0414\u0440\u0443\u0433\u0438\u0435 \u044f\u0437\u044b\u043a\u0438...";
var COMMUNITY_MESSAGE = "\u0421\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u043e NetBeans \u0432\u043d\u0435\u0441\u043b\u043e \u0441\u0432\u043e\u0439 \u0432\u043a\u043b\u0430\u0434 \u0432 \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u044b NetBeans \u043d\u0430 \u0442\u0435 \u044f\u0437\u044b\u043a\u0438, \u043a\u043e\u0442\u043e\u0440\u044b\u0435 \u043c\u043e\u0433\u0443\u0442 \u0431\u044b\u0442\u044c \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b \u0441 \u0434\u0430\u043d\u043d\u043e\u0439 \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u044b. \u041e\u0431\u044a\u0451\u043c \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u0430 \u043e\u0442\u043b\u0438\u0447\u0430\u0435\u0442\u0441\u044f \u043e\u0442 \u044f\u0437\u044b\u043a\u0430 \u043a \u044f\u0437\u044b\u043a\u0443, \u0441\u043c. <a href=\"http://wiki.netbeans.org/TFL10nCommunityStatus\">\u0441\u0442\u0430\u0442\u0443\u0441 \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u043e\u0432 \u0441\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u0430</a>. \u041f\u0435\u0440\u0435\u0432\u043e\u0434 \u043e\u0431\u044b\u0447\u043d\u043e \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0430\u0435\u0442\u0441\u044f \u0438 \u043f\u043e\u0441\u043b\u0435 \u043f\u043e\u0441\u0442\u0440\u043e\u0435\u043d\u0438\u044f \u0434\u0430\u043d\u043d\u044b\u0445 \u0441\u0431\u043e\u0440\u043e\u043a, \u0434\u043b\u044f \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438 \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0435\u0433\u043e \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u0430 \u0441\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u0430, \u043f\u043e\u0441\u0435\u0442\u0438\u0442\u0435 <a href=\"http://wiki.netbeans.org/TFLocalizationDevelopmentUC\">\u0441\u0442\u0440\u0430\u043d\u0438\u0446\u0443 \u0441 \u0438\u043d\u0441\u0442\u0440\u0443\u043a\u0446\u0438\u044f\u043c\u0438 \u043f\u043e \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0435 \u043f\u043b\u0430\u0433\u0438\u043d\u0430 \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u0430</a>.";
var COMMUNITY_CONTRIBUTED_SEP    = " -- \u041f\u0435\u0440\u0435\u0432\u0435\u0434\u0435\u043d\u043d\u044b\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u0441\u0442\u0432\u043e\u043c-- " ;
var SUN_SUPPORTED_SEP            = " -- \u041f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u043c\u044b\u0435 Oracle-- " ;

var NETBEANS_DOWNLOAD_PAGE_TITLE       = "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0441\u0440\u0435\u0434\u044b NetBeans";
var NETBEANS_DOWNLOAD_PAGE_DESCRIPTION = "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0441\u0440\u0435\u0434\u044b NetBeans";



var NETBEANS_DOWNLOAD_HEADER = "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0441\u0440\u0435\u0434\u044b NetBeans {0}";
var DEVELOPMENT_TITLE        = "\u0420\u0430\u0437\u0440\u0430\u0431\u043e\u0442\u043a\u0430";
var ARCHIVE_TITLE            = "\u0410\u0440\u0445\u0438\u0432";
var EMAIL_LABEL              = "\u042d\u043b\u0435\u043a\u0442\u0440\u043e\u043d\u043d\u0430\u044f&nbsp;\u043f\u043e\u0447\u0442\u0430&nbsp;(\u043d\u0435\u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u043e):&nbsp;";
var SUBSCRIBE_LABEL          = "\u041f\u043e\u0434\u043f\u0438\u0441\u0430\u0442\u044c\u0441\u044f&nbsp;\u043d\u0430&nbsp;\u043d\u043e\u0432\u043e\u0441\u0442\u0438:";
var MONTHLY_LABEL            = "\u0415\u0436\u0435\u043c\u0435\u0441\u044f\u0447\u043d\u044b\u0435&nbsp;&nbsp;&nbsp;";
var WEEKLY_LABEL             = "\u0415\u0436\u0435\u043d\u0435\u0434\u0435\u043b\u044c\u043d\u044b\u0435";
var CONTACT_LABEL            = "NetBeans&nbsp;\u043c\u043e\u0436\u0435\u0442&nbsp;\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c<br>\u0434\u0430\u043d\u043d\u044b\u0439&nbsp;\u0430\u0434\u0440\u0435\u0441&nbsp;\u0434\u043b\u044f&nbsp;\u0441\u0432\u044f\u0437\u0438&nbsp;\u0441\u043e \u043c\u043d\u043e\u0439";
var LANGUAGE_LABEL           = "\u042f\u0437\u044b\u043a IDE:";
var PLATFORM_LABEL           = "\u041f\u043b\u0430\u0442\u0444\u043e\u0440\u043c\u0430:";
var PLUGIN_MANAGER_LABEL     = "\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0438\u043b\u0438 \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u043a\u043e\u043c\u043f\u043e\u043d\u0435\u043d\u0442\u044b \u043f\u043e\u0437\u0434\u043d\u0435\u0435 \u0441 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u043c\u0435\u043d\u0435\u0434\u0436\u0435\u0440\u0430 \u043f\u043b\u0430\u0433\u0438\u043d\u043e\u0432 \u0441\u0440\u0435\u0434\u044b (Tools | Plugins).<br><br>";
var JAVATOOLSBUNDLE_LABEL    = "\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c NetBeans IDE \u0442\u0430\u043a\u0436\u0435 \u0432 \u0441\u043e\u0441\u0442\u0430\u0432\u0435 <a href=\"http://java.sun.com/javaee/downloads/index.jsp\">Java EE 5 Tools Bundle</a> \u0438\u043b\u0438 <a href=\"http://download.netbeans.org/netbeans/6.1/mysql_bundle/\">MySQL GlassFish Bundle</a>.";
var SOURCE_BINARY_LABEL      = "\u0418\u0441\u0445\u043e\u0434\u043d\u044b\u0439 \u043a\u043e\u0434 \u0438 \u0441\u0431\u043e\u0440\u043a\u0438 NetBeans \u0431\u0435\u0437 \u043f\u043e\u0441\u0442\u0430\u0432\u043b\u044f\u0435\u043c\u044b\u0445 \u0441\u0435\u0440\u0432\u0435\u0440\u043e\u0432 \u0442\u0430\u043a\u0436\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b \u043a\u0430\u043a <a id=\"zip_link\" href=\"{0}\">\u0430\u0440\u0445\u0438\u0432\u044b zip</a>. ";
var BUILD_WIKI_LABEL         = "\u0421\u043c\u043e\u0442\u0440\u0438\u0442\u0435 \u0442\u0430\u043a\u0436\u0435 <a href=\"http://wiki.netbeans.org/wiki/view/WorkingWithNetBeansSources\">\u0438\u043d\u0441\u0442\u0440\u0443\u043a\u0446\u0438\u0438 \u043f\u043e \u043f\u043e\u0441\u0442\u0440\u043e\u0435\u043d\u0438\u044e \u0438\u043d\u0442\u0435\u0433\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u043e\u0439 \u0441\u0440\u0435\u0434\u044b \u0438\u0437 \u0438\u0441\u0445\u043e\u0434\u043d\u043e\u0433\u043e \u043a\u043e\u0434\u0430</a> \u0438\u043b\u0438 <a href=\"{0}\">\u0438\u043d\u0441\u0442\u0440\u0443\u043a\u0446\u0438\u0438 \u043f\u043e \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0435</a>.";
var PLATFORM_DEV_LABEL       = "\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u043d\u0430\u0447\u0430\u0442\u044c \u0440\u0430\u0437\u0440\u0430\u0431\u043e\u0442\u043a\u0443 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0439 \u043d\u0430 \u043e\u0441\u043d\u043e\u0432\u0435 \u041f\u043b\u0430\u0442\u0444\u043e\u0440\u043c\u044b NetBeans \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u044f \u0441\u0431\u043e\u0440\u043a\u0443 NetBeans IDE \u0434\u043b\u044f Java SE. \u0423\u0437\u043d\u0430\u0439\u0442\u0435 \u0431\u043e\u043b\u044c\u0448\u0435 \u043e <a href=\"{0}\">\u041f\u043b\u0430\u0442\u0444\u043e\u0440\u043c\u0435 NetBeans</a>.";
var PLATFORM_INFO_LINK       = "http://platform.netbeans.org/platform-get.html";

var ARCHIVE_BUILDS_LINK      = "http://www.netbeans.info/downloads/dev.php";
var DEVELOPMENT_BUILDS_LINK  = "http://bits.netbeans.org/download/trunk/nightly/latest/";

// TRANSLATE NOTE: change download*.gif to download*_<locale>.gif
var DOWNLOAD_BUTTON_NORMAL    = "download_ru.gif";
var DOWNLOAD_BUTTON_DISABLED  = "download_d_ru.gif";
var DOWNLOAD_BUTTON_HIGHLIGHT = "download_h_ru.gif";

// DO NOT TRANSLATE
var START_PAGE = "start.html";
var ZIP_PAGE   = "zip.html";

