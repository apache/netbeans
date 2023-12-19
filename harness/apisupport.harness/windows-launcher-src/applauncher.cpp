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
 /*
 * Author: Tomas Holy
 */

#include "applauncher.h"
#include "../../../platform/o.n.bootstrap/launcher/windows/utilsfuncs.h"
#include "../../../platform/o.n.bootstrap/launcher/windows/argnames.h"

using namespace std;

const char *AppLauncher::OPT_DEFAULT_USER_DIR = "default_userdir=";
const char *AppLauncher::OPT_DEFAULT_CACHE_DIR = "default_cachedir=";
const char *AppLauncher::OPT_DEFAULT_OPTIONS = "default_options=";
const char *AppLauncher::OPT_EXTRA_CLUSTERS = "extra_clusters=";
const char *AppLauncher::OPT_JDK_HOME = "jdkhome=";
const char *AppLauncher::APPNAME_TOKEN = "${APPNAME}";
const char *AppLauncher::CACHE_SUFFIX = "\\Cache\\";

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
    if (!NbLauncher::findUserDir(str)) {    // will set userDir and possibly userHome.
        return false;
    }
    int pos = userDir.find(APPNAME_TOKEN);
    if (pos != string::npos) {
        userDir.replace(pos, strlen(APPNAME_TOKEN), appName);
    }
    return true;
}

bool AppLauncher::findCacheDir(const char *str) {
    logMsg("AppLauncher::findCacheDir");
    if (!NbLauncher::findCacheDir(str)) {    // will set userDir and possibly userHome.
        return false;
    }
    int pos = cacheDir.find(APPNAME_TOKEN);
    if (pos != string::npos) {
        cacheDir.replace(pos, strlen(APPNAME_TOKEN), appName);
    }
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

std::string AppLauncher::constructApplicationDir(const std::string& dir, bool cache) {
    if (cache) {
        return dir + "\\" + getAppName() + CACHE_SUFFIX;
    } else {
        return dir + "\\" + getAppName() + "\\";
    }
}
