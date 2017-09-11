/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
 /*
 * Author: Tomas Holy
 */

#include "applauncher.h"
#include "../../o.n.bootstrap/launcher/windows/utilsfuncs.h"
#include "../../o.n.bootstrap/launcher/windows/argnames.h"

using namespace std;

const char *AppLauncher::OPT_DEFAULT_USER_DIR = "default_userdir=";
const char *AppLauncher::OPT_DEFAULT_CACHE_DIR = "default_cachedir=";
const char *AppLauncher::OPT_DEFAULT_OPTIONS = "default_options=";
const char *AppLauncher::OPT_EXTRA_CLUSTERS = "extra_clusters=";
const char *AppLauncher::OPT_JDK_HOME = "jdkhome=";
const char *AppLauncher::APPNAME_TOKEN = "${APPNAME}";

AppLauncher::AppLauncher() {
}

AppLauncher::AppLauncher(const AppLauncher& orig) {
}

AppLauncher::~AppLauncher() {
}

bool AppLauncher::initBaseNames() {
    if (!NbLauncher::initBaseNames()) {
        return false;
    }

    string pattern = baseDir + "\\platform*";
    WIN32_FIND_DATA fd = {0};
    HANDLE hFind;
    hFind = FindFirstFile(pattern.c_str(), &fd);
    if (hFind == INVALID_HANDLE_VALUE) {
        logErr(false, true, "Cannot find 'platform*' folder!");
        return false;
    }

    do {
        if ((fd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
                && fileExists((baseDir + '\\' + fd.cFileName + NbLauncher::NBEXEC_FILE_PATH).c_str())) {
            platformDir = baseDir + '\\' + fd.cFileName;
            break;
        }
    } while (FindNextFile(hFind, &fd));

    FindClose(hFind);

    if (platformDir.empty()) {
        logErr(false, true, "Cannot find valid 'platform*' folder!");
        return false;
    }
    return true;
}

bool AppLauncher::findUserDir(const char *str) {
    logMsg("AppLauncher::findUserDir()");
    if (strncmp(str, HOME_TOKEN, strlen(HOME_TOKEN)) == 0) {
        if (userHome.empty()) {
            TCHAR userHomeChar[MAX_PATH];
            if (FAILED(SHGetFolderPath(NULL, CSIDL_APPDATA, NULL, 0, userHomeChar))) {
                return false;
            }
            userHome = userHomeChar;           
            logMsg("User home: %s", userHome.c_str());
        }
        str += strlen(HOME_TOKEN);
        userDir = userHome;
    }
    const char *appToken = strstr(str, APPNAME_TOKEN);
    if (appToken) {
        userDir += string(str, appToken - str);
        str += appToken - str;
        userDir += appName;
        str += strlen(APPNAME_TOKEN);
    }
    userDir += str;
    return true;
}

bool AppLauncher::findCacheDir(const char *str) {
    logMsg("AppLauncher::findCacheDir");
    if (strncmp(str, HOME_TOKEN, strlen(HOME_TOKEN)) == 0) {
        if (userHome.empty()) {
            TCHAR userHomeChar[MAX_PATH];
            if (FAILED(SHGetFolderPath(NULL, CSIDL_LOCAL_APPDATA, NULL, 0, userHomeChar))) {
                return false;
            }
            userHome = userHomeChar;
            logMsg("User home: %s", userHome.c_str());
        }
        str += strlen(HOME_TOKEN);
        cacheDir = userHome;
    }
    const char *appToken = strstr(str, APPNAME_TOKEN);
    if (appToken) {
        cacheDir += string(str, appToken - str);
        str += appToken - str;
        cacheDir += appName;
        str += strlen(APPNAME_TOKEN);
    }
    cacheDir += str;
    return true;
}

const char * AppLauncher::getAppName() {
    return appName.c_str();
}

void AppLauncher::addSpecificOptions(CmdArgs &args) {
}

void AppLauncher::adjustHeapSize() {
}

const char * AppLauncher::getDefUserDirOptName() {
    return OPT_DEFAULT_USER_DIR;
}

const char * AppLauncher::getDefCacheDirOptName() {
    return OPT_DEFAULT_CACHE_DIR;
}

const char * AppLauncher::getDefOptionsOptName() {
    return OPT_DEFAULT_OPTIONS;
}

const char * AppLauncher::getExtraClustersOptName() {
    return OPT_EXTRA_CLUSTERS;
}

const char * AppLauncher::getJdkHomeOptName() {
    return OPT_JDK_HOME;
}

const char * AppLauncher::getCurrentDir() {
    return baseDir.c_str();
}
