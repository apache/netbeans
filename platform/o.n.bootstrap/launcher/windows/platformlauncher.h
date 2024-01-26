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

#ifndef _PLATFORMLAUNCHER_H
#define	_PLATFORMLAUNCHER_H

#include "jvmlauncher.h"
#include <string>
#include <list>
#include <set>

class PlatformLauncher {
    static const char *REQ_JAVA_VERSION;
    static const char *HELP_MSG;

    static const char *HEAP_DUMP_PATH;
    static const char *RESTART_FILE_PATH;

    static const char *OPT_JDK_HOME;
    static const char *OPT_NB_PLATFORM_HOME;
    static const char *OPT_NB_CLUSTERS;
    static const char *OPT_NB_USERDIR;
    static const char *OPT_DEFAULT_USERDIR_ROOT;
    static const char *OPT_HEAP_DUMP;
    static const char *OPT_HEAP_DUMP_PATH;
    static const char *OPT_JAVA_SECURITY_MANAGER_ALLOW;
    static const char *OPT_KEEP_WORKING_SET_ON_MINIMIZE;
    static const char *OPT_CLASS_PATH;
    static const char *OPT_SPLASH;
    static const char *OPT_SPLASH_PATH;

    static const char *UPDATER_MAIN_CLASS;
    static const char *IDE_MAIN_CLASS;


public:
    PlatformLauncher();
    virtual ~PlatformLauncher();

    bool start(char* argv[], int argc, DWORD *retCode);
    void appendToHelp(const char *msg);
    void onExit();

    void setSuppressConsole(bool val) {
        suppressConsole = val;
    }

private:
    PlatformLauncher(const PlatformLauncher& orig);
    bool parseArgs(int argc, char *argv[]);
    bool initPlatformDir();
    bool processAutoUpdateCL();
    void deleteNewClustersFile();
    bool checkForNewUpdater(const char *basePath);
    bool shouldAutoUpdate(bool firstStart, const char *basePath);
    bool shouldAutoUpdateClusters(bool firstStart);
    void prepareOptions();
    std::string & constructClassPath(bool runUpdater);
    void addFilesToClassPath(const char *dir, const char *subdir, const char *pattern);
    void addToClassPath(const char *path, bool onlyIfExists = false);
    void addJarsToClassPathFrom(const char *dir);
    bool run(bool updater, DWORD *retCode);
    bool restartRequested();

private:
    bool separateProcess;
    bool suppressConsole;
    bool heapDumpPathOptFound;
    bool nosplash;
    bool exiting;
    std::string platformDir;
    std::string userDir;
    std::string defaultUserDirRoot;
    std::string clusters;
    std::string bootclass;
    std::string jdkhome;
    std::string cpBefore;
    std::string cpAfter;
    std::string auClusters;
    std::string nextAction;
    std::string parentProcID;

    std::list<std::string> javaOptions;
    std::list<std::string> launcherOptions;
    std::list<std::string> progArgs;
    JvmLauncher jvmLauncher;
    std::set<std::string> addedToCP;
    std::string classPath;
    std::string appendHelp;
};

#endif	/* _PLATFORMLAUNCHER_H */

