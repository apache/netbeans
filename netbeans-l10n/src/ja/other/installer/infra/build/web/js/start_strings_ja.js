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

