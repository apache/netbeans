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

#include "utilsfuncs.h"
#include "platformlauncher.h"
#include "argnames.h"

volatile extern int exitStatus;

using namespace std;

const char *PlatformLauncher::HELP_MSG =
"\nUsage: launcher {options} arguments\n\
\n\
General options:\n\
  --help                show this help\n\
  --jdkhome <path>      path to JDK\n\
  -J<jvm_option>        pass <jvm_option> to JVM\n\
\n\
  --cp:p <classpath>    prepend <classpath> to classpath\n\
  --cp:a <classpath>    append <classpath> to classpath\n\
\n\
  --fork-java           run java in separate process\n\
  --trace <path>        path for launcher log (for trouble shooting)\n\
\n";

const char *PlatformLauncher::REQ_JAVA_VERSION = "1.8";

const char *PlatformLauncher::OPT_JDK_HOME = "-Djdk.home=";
const char *PlatformLauncher::OPT_NB_PLATFORM_HOME = "-Dnetbeans.home=";
const char *PlatformLauncher::OPT_NB_CLUSTERS = "-Dnetbeans.dirs=";
const char *PlatformLauncher::OPT_NB_USERDIR = "-Dnetbeans.user=";
const char *PlatformLauncher::OPT_DEFAULT_USERDIR_ROOT = "-Dnetbeans.default_userdir_root=";
const char *PlatformLauncher::OPT_HEAP_DUMP = "-XX:+HeapDumpOnOutOfMemoryError";
const char *PlatformLauncher::OPT_HEAP_DUMP_PATH = "-XX:HeapDumpPath=";
const char *PlatformLauncher::OPT_JAVA_SECURITY_MANAGER_ALLOW = "-Djava.security.manager=allow";
const char *PlatformLauncher::OPT_KEEP_WORKING_SET_ON_MINIMIZE = "-Dsun.awt.keepWorkingSetOnMinimize=true";
const char *PlatformLauncher::OPT_CLASS_PATH = "-Djava.class.path=";
const char *PlatformLauncher::OPT_SPLASH = "-splash:";
const char *PlatformLauncher::OPT_SPLASH_PATH = "\\var\\cache\\splash.png";

const char *PlatformLauncher::HEAP_DUMP_PATH =  "\\var\\log\\heapdump.hprof";
const char *PlatformLauncher::RESTART_FILE_PATH =  "\\var\\restart";

const char *PlatformLauncher::UPDATER_MAIN_CLASS = "org/netbeans/updater/UpdaterFrame";
const char *PlatformLauncher::IDE_MAIN_CLASS = "org/netbeans/Main";

PlatformLauncher::PlatformLauncher()
    : separateProcess(false)
    , suppressConsole(false)
    , heapDumpPathOptFound(false)
    , nosplash(false)
    , exiting(false) {
}

PlatformLauncher::PlatformLauncher(const PlatformLauncher& orig) {
}

PlatformLauncher::~PlatformLauncher() {
}

bool PlatformLauncher::start(char* argv[], int argc, DWORD *retCode) {
    if (!checkLoggingArg(argc, argv, false) || !initPlatformDir() || !parseArgs(argc, argv)) {
        return false;
    }
    disableFolderVirtualization(GetCurrentProcess());

    if (jdkhome.empty()) {
        if (!jvmLauncher.initialize(REQ_JAVA_VERSION)) {
            logErr(false, true, "Cannot find Java %s or higher.", REQ_JAVA_VERSION);
            return false;
        }
    }
    jvmLauncher.getJavaPath(jdkhome);
    
    deleteNewClustersFile();
    prepareOptions();

    if (nextAction.empty()) {
        if (shouldAutoUpdateClusters(true)) {
            // run updater
            if (!run(true, retCode)) {
                return false;
            }
        }

        while (true) {
            // run app
            if (!run(false, retCode)) {
                return false;
            }

            if (shouldAutoUpdateClusters(false)) {
                // run updater
                if (!run(true, retCode)) {
                    return false;
                }
            } else if (!restartRequested()) {
                break;
            }
        }
    } else {
        if (nextAction == ARG_NAME_LA_START_APP) {
            return run(false, retCode);
        } else if (nextAction == ARG_NAME_LA_START_AU) {
            if (shouldAutoUpdateClusters(false)) {
                return run(true, retCode);
            }
        } else {
            logErr(false, true, "We should not get here.");
            return false;
        }
    }

    return true;
}

bool PlatformLauncher::run(bool updater, DWORD *retCode) {
    logMsg(updater ? "Starting updater..." : "Starting application...");
    constructClassPath(updater);
    const char *mainClass;
    if (updater) {
        mainClass = UPDATER_MAIN_CLASS;
        nextAction = ARG_NAME_LA_START_APP;
    } else {
        DeleteFile((userDir + RESTART_FILE_PATH).c_str());
        mainClass = bootclass.empty() ? IDE_MAIN_CLASS : bootclass.c_str();
        nextAction = ARG_NAME_LA_START_AU;
    }

    string option = OPT_NB_CLUSTERS;
    option += auClusters.empty() ? clusters : auClusters;
    javaOptions.push_back(option);

    option = OPT_CLASS_PATH;
    option += classPath;
    javaOptions.push_back(option);

    jvmLauncher.setSuppressConsole(suppressConsole);
    bool rc = jvmLauncher.start(mainClass, progArgs, javaOptions, separateProcess, retCode);
    if (!separateProcess) {
        exit(0);
    }

    javaOptions.pop_back();
    javaOptions.pop_back();
    return rc;
}



bool PlatformLauncher::initPlatformDir() {
    char path[MAX_PATH] = "";
    getCurrentModulePath(path, MAX_PATH);
    logMsg("Module: %s", path);
    char *bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    *bslash = '\0';
    bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    *bslash = '\0';
    clusters = platformDir = path;
    logMsg("Platform dir: %s", platformDir.c_str());
    return true;
}

bool PlatformLauncher::parseArgs(int argc, char *argv[]) {
#define CHECK_ARG \
    if (i+1 == argc) {\
        logErr(false, true, "Argument is missing for \"%s\" option.", argv[i]);\
        return false;\
    }

    logMsg("Parsing arguments:");
    for (int i = 0; i < argc; i++) {
        logMsg("\t%s", argv[i]);
    }

    for (int i = 0; i < argc; i++) {
        if (strcmp(ARG_NAME_SEPAR_PROC, argv[i]) == 0) {
            separateProcess = true;
            logMsg("Run Java in separater process");
        } else if (strcmp(ARG_NAME_LAUNCHER_LOG, argv[i]) == 0) {
            CHECK_ARG;
            i++;
        } else if (strcmp(ARG_NAME_LA_START_APP, argv[i]) == 0
                || strcmp(ARG_NAME_LA_START_AU, argv[i]) == 0) {
            nextAction = argv[i];
            logMsg("Next launcher action: %s", nextAction.c_str());
        } else if (strcmp(ARG_NAME_LA_PPID, argv[i]) == 0) {
            CHECK_ARG;
            suppressConsole = false;
            parentProcID = argv[++i];
            logMsg("Parent process ID found: %s", parentProcID.c_str());
        } else if (strcmp(ARG_NAME_USER_DIR, argv[i]) == 0) {
            CHECK_ARG;
            char tmp[MAX_PATH + 1] = {0};
            strncpy(tmp, argv[++i], MAX_PATH);
            if (strcmp(tmp, "memory") != 0 && !normalizePath(tmp, MAX_PATH)) {
                logErr(false, true, "User directory path \"%s\" is not valid.", argv[i]);
                return false;
            }
            userDir = tmp;
            logMsg("User dir: %s", userDir.c_str());
        } else if (strcmp(ARG_DEFAULT_USER_DIR_ROOT, argv[i]) == 0) {
            CHECK_ARG;
            char tmp[MAX_PATH + 1] = {0};
            strncpy(tmp, argv[++i], MAX_PATH);
            if (strcmp(tmp, "memory") != 0 && !normalizePath(tmp, MAX_PATH)) {
                logErr(false, true, "Default User directory path \"%s\" is not valid.", argv[i]);
                return false;
            }
            defaultUserDirRoot = tmp;
            logMsg("Default Userdir root: %s", defaultUserDirRoot.c_str());
        } else if (strcmp(ARG_NAME_CLUSTERS, argv[i]) == 0) {
            CHECK_ARG;
            clusters = argv[++i];
        } else if (strcmp(ARG_NAME_BOOTCLASS, argv[i]) == 0) {
            CHECK_ARG;
            bootclass = argv[++i];
        } else if (strcmp(ARG_NAME_JDKHOME, argv[i]) == 0) {
            CHECK_ARG;            
            if (jdkhome.empty()) {
                jdkhome = argv[++i];
                if (!jvmLauncher.initialize(jdkhome.c_str())) {
                    logMsg("Cannot locate java installation in specified jdkhome: %s", jdkhome.c_str());
                    string errMsg = "Cannot locate java installation in specified jdkhome:\n";
                    errMsg += jdkhome;
                    errMsg += "\nDo you want to try to use default version?";
                    jdkhome = "";
                    if (::MessageBox(NULL, errMsg.c_str(), "Invalid jdkhome specified", MB_ICONQUESTION | MB_YESNO) == IDNO) {
                        return false;
                    }
                }
            } else {
                i++;
            }
        } else if (strcmp(ARG_NAME_CP_PREPEND, argv[i]) == 0
                || strcmp(ARG_NAME_CP_PREPEND + 1, argv[i]) == 0) {
            CHECK_ARG;
            cpBefore += argv[++i];
        } else if (strcmp(ARG_NAME_CP_APPEND, argv[i]) == 0
                || strcmp(ARG_NAME_CP_APPEND + 1, argv[i]) == 0
                || strncmp(ARG_NAME_CP_APPEND + 1, argv[i], 3) == 0
                || strncmp(ARG_NAME_CP_APPEND, argv[i], 4) == 0) {
            CHECK_ARG;
            cpAfter += argv[++i];
        } else if (strncmp("-J", argv[i], 2) == 0) {
            javaOptions.push_back(argv[i] + 2);
            if (strncmp(argv[i] + 2, OPT_HEAP_DUMP_PATH, strlen(OPT_HEAP_DUMP_PATH)) == 0) {
                heapDumpPathOptFound = true;
            }
        } else {
            if (strcmp(argv[i], "-h") == 0
                    || strcmp(argv[i], "-help") == 0
                    || strcmp(argv[i], "--help") == 0
                    || strcmp(argv[i], "/?") == 0) {
                printToConsole(HELP_MSG);
                if (!appendHelp.empty()) {
                    printToConsole(appendHelp.c_str());
                }
            } else if (strcmp(ARG_NAME_NOSPLASH, argv[i]) == 0) {
                 nosplash = true;
            }
            progArgs.push_back(argv[i]);
        }
    }
    return true;
}

bool PlatformLauncher::processAutoUpdateCL() {
    logMsg("processAutoUpdateCL()...");
    if (userDir.empty()) {
        logMsg("\tuserdir empty, quiting");
        return false;
    }
    string listPath = userDir;
    listPath += "\\update\\download\\netbeans.dirs";

    WIN32_FIND_DATA fd = {0};
    HANDLE hFind = 0;
    hFind = FindFirstFile(listPath.c_str(), &fd);
    if (hFind == INVALID_HANDLE_VALUE) {
        logMsg("File \"%s\" does not exist", listPath.c_str());
        return false;
    }
    FindClose(hFind);

    FILE *file = fopen(listPath.c_str(), "r");
    if (!file) {
        logErr(true, false, "Cannot open file %s", listPath.c_str());
        return false;
    }

    int len = fd.nFileSizeLow + 1;
    char *str = new char[len];
    if (!fgets(str, len, file)) {
        fclose(file);
        delete[] str;
        logErr(true, false, "Cannot read from file %s", listPath.c_str());
        return false;
    }
    len = strlen(str) - 1;
    if (str[len] == '\n') {
        str[len] = '\0';
    }

    auClusters = str;
    fclose(file);
    delete[] str;
    return true;
}

void PlatformLauncher::deleteNewClustersFile() {
    logMsg("deleteNewClustersFile()...");
    if (userDir.empty()) {
        logMsg("\tuserdir empty, quiting");
        return;
    }
    string listPath = userDir;
    listPath += "\\update\\download\\netbeans.dirs";

    if (fileExists(listPath.c_str())) {
        DeleteFileA(listPath.c_str());
        logMsg("%s file deleted.", listPath.c_str());
    }
}

// check if new updater exists, if exists install it (replace old one) and remove ...\new_updater directory
bool PlatformLauncher::checkForNewUpdater(const char *basePath) {
    logMsg("checkForNewUpdater() at %s", basePath);
    BOOL removeDir = false;
    string srcPath = basePath;
    srcPath += "\\update\\new_updater\\updater.jar";
    WIN32_FIND_DATA fd = {0};
    HANDLE hFind = FindFirstFile(srcPath.c_str(), &fd);
    if (hFind != INVALID_HANDLE_VALUE) {
        logMsg("New updater found: %s", srcPath.c_str());
        FindClose(hFind);
        string destPath = basePath;
        destPath += "\\modules\\ext\\updater.jar";
        createPath(destPath.c_str());

        int i = 0;
        while (true) {
            if (MoveFileEx(srcPath.c_str(), destPath.c_str(), MOVEFILE_REPLACE_EXISTING | MOVEFILE_WRITE_THROUGH)) {
                break;
            }
            if (exiting || ++i > 10) {
                logErr(true, false, "Failed to move \"%s\" to \"%s\"", srcPath.c_str(), destPath.c_str());
                return false;
            }
            logErr(true, false, "Failed to move \"%s\" to \"%s\", trying to wait", srcPath.c_str(), destPath.c_str());
            Sleep(100);
        }
        logMsg("New updater successfully moved from \"%s\" to \"%s\"", srcPath.c_str(), destPath.c_str());
        removeDir = true;
    } else {
        logMsg("No new updater at %s", srcPath.c_str());
    }
    string locPath = basePath;
    locPath += "\\update\\new_updater\\updater_*.jar";
    hFind = FindFirstFile(locPath.c_str(), &fd);
    while (hFind != INVALID_HANDLE_VALUE) {
        string destPath = basePath;
        string name = fd.cFileName;
        logMsg("New updater localization found: %s", name.c_str());
        destPath += "\\modules\\ext\\locale\\";
        destPath += name;

        string fromPath = basePath;
        fromPath += "\\update\\new_updater\\";
        fromPath += name;

        createPath(destPath.c_str());

        int i = 0;
        while (true) {
            if (MoveFileEx(fromPath.c_str(), destPath.c_str(), MOVEFILE_REPLACE_EXISTING | MOVEFILE_WRITE_THROUGH)) {
                break;
            }
            if (exiting || ++i > 10) {
                logErr(true, false, "Failed to move \"%s\" to \"%s\"", fromPath.c_str(), destPath.c_str());
                return false;
            }
            logErr(true, false, "Failed to move \"%s\" to \"%s\", trying to wait", fromPath.c_str(), destPath.c_str());
            Sleep(100);
        }
        logMsg("New updater successfully moved from \"%s\" to \"%s\"", fromPath.c_str(), destPath.c_str());
        removeDir = true;
        
        if (!FindNextFile(hFind, &fd)) {
            break;
        }
    }
    FindClose(hFind);

    if (removeDir) {
        srcPath.erase(srcPath.rfind('\\'));
        logMsg("Removing directory \"%s\"", srcPath.c_str());
        if (!RemoveDirectory(srcPath.c_str())) {
            logErr(true, false, "Failed to remove directory \"%s\"", srcPath.c_str());
        }
    }
    return true;
}

bool PlatformLauncher::shouldAutoUpdate(bool firstStart, const char *basePath) {
    // The logic is following:
    // if there is an NBM for installation then run updater
    // unless it is not a first start and we asked to install later (on next start)

    // then also check if last run left list of modules to disable/uninstall and
    // did not mark them to be deactivated later (on next start)
    string path = basePath;
    path += "\\update\\download\\*.nbm";
    logMsg("Checking for updates: %s", path.c_str());
    WIN32_FIND_DATA fd;
    HANDLE hFindNbms = FindFirstFile(path.c_str(), &fd);
    if (hFindNbms != INVALID_HANDLE_VALUE) {
        logMsg("Some updates found at %s", path.c_str());
        FindClose(hFindNbms);
    } else {
        //also check for OSGi jars if *.nbm not found
        path = basePath;
        path += "\\update\\download\\*.jar";
        hFindNbms = FindFirstFile(path.c_str(), &fd);
        if (hFindNbms != INVALID_HANDLE_VALUE) {
            logMsg("Some OSGi updates found at %s", path.c_str());
            FindClose(hFindNbms);
        }
    }

    path = basePath;
    path += "\\update\\download\\install_later.xml";
    HANDLE hFind = FindFirstFile(path.c_str(), &fd);
    if (hFind != INVALID_HANDLE_VALUE) {
        logMsg("install_later.xml found: %s", path.c_str());
        FindClose(hFind);
    }

    if (hFindNbms != INVALID_HANDLE_VALUE && (firstStart || hFind == INVALID_HANDLE_VALUE)) {
        return true;
    }

    path = basePath;
    path += "\\update\\deactivate\\deactivate_later.txt";
    hFind = FindFirstFile(path.c_str(), &fd);
    if (hFind != INVALID_HANDLE_VALUE) {
        logMsg("deactivate_later.txt found: %s", path.c_str());
        FindClose(hFind);
    }

    if (firstStart || hFind == INVALID_HANDLE_VALUE) {
        path = basePath;
        path += "\\update\\deactivate\\to_disable.txt";
        hFind = FindFirstFile(path.c_str(), &fd);
        if (hFind != INVALID_HANDLE_VALUE) {
            logMsg("to_disable.txt found: %s", path.c_str());
            FindClose(hFind);
            return true;
        }

        path = basePath;
        path += "\\update\\deactivate\\to_enable.txt";
        hFind = FindFirstFile(path.c_str(), &fd);
        if (hFind != INVALID_HANDLE_VALUE) {
            logMsg("to_enable.txt found: %s", path.c_str());
            FindClose(hFind);
            return true;
        }

        path = basePath;
        path += "\\update\\deactivate\\to_uninstall.txt";
        hFind = FindFirstFile(path.c_str(), &fd);
        if (hFind != INVALID_HANDLE_VALUE) {
            logMsg("to_uninstall.txt found: %s", path.c_str());
            FindClose(hFind);
            return true;
        }
    }

    return false;
}

bool PlatformLauncher::shouldAutoUpdateClusters(bool firstStart) {
    bool runUpdater = false;
    string cl = processAutoUpdateCL() ? auClusters : clusters;
    checkForNewUpdater(platformDir.c_str());
    runUpdater = shouldAutoUpdate(firstStart, platformDir.c_str());

    const char delim = ';';
    string::size_type start = cl.find_first_not_of(delim, 0);
    string::size_type end = cl.find_first_of(delim, start);
    while (string::npos != end || string::npos != start) {
        string cluster = cl.substr(start, end - start);
        checkForNewUpdater(cluster.c_str());
        if (!runUpdater) {
            runUpdater = shouldAutoUpdate(firstStart, cluster.c_str());
        }
        start = cl.find_first_not_of(delim, end);
        end = cl.find_first_of(delim, start);
    }

    checkForNewUpdater(userDir.c_str());
    if (!runUpdater) {
        runUpdater = shouldAutoUpdate(firstStart, userDir.c_str());
    }
    return runUpdater;
}

void PlatformLauncher::prepareOptions() {
    string option = OPT_JDK_HOME;
    option += jdkhome;
    javaOptions.push_back(option);

    if (!nosplash) {
        string splashPath = userDir;
        splashPath += OPT_SPLASH_PATH;
        if (fileExists(splashPath.c_str())) {
            javaOptions.push_back(OPT_SPLASH + splashPath);
        }
    }

    option = OPT_NB_PLATFORM_HOME;
    option += platformDir;
    javaOptions.push_back(option);

    option = OPT_NB_USERDIR;
    option += userDir;
    javaOptions.push_back(option);
    
    option = OPT_DEFAULT_USERDIR_ROOT;
    option += defaultUserDirRoot;
    javaOptions.push_back(option);

    option = OPT_HEAP_DUMP;
    javaOptions.push_back(option);

    if (!heapDumpPathOptFound) {
        option = OPT_HEAP_DUMP_PATH;
        option += userDir;
        option += HEAP_DUMP_PATH;
        javaOptions.push_back(option);
        // rename old heap dump to .old
        string heapdumpfile = userDir + HEAP_DUMP_PATH;
        if (fileExists(heapdumpfile.c_str())) {
            string heapdumpfileold = heapdumpfile + ".old";
            if (fileExists(heapdumpfileold.c_str())) {
                DeleteFileA(heapdumpfileold.c_str());
            }
            MoveFile (heapdumpfile.c_str(), heapdumpfileold.c_str());
        }
    }
    
    option = OPT_KEEP_WORKING_SET_ON_MINIMIZE;
    javaOptions.push_back(option);

    option = OPT_JAVA_SECURITY_MANAGER_ALLOW;
    javaOptions.push_back(option);
}

string & PlatformLauncher::constructClassPath(bool runUpdater) {
    logMsg("constructClassPath()");
    addedToCP.clear();
    classPath = cpBefore;

    addJarsToClassPathFrom(userDir.c_str());
    addJarsToClassPathFrom(platformDir.c_str());

    if (runUpdater) {
        const char *baseUpdaterPath = userDir.c_str();
        string updaterPath = userDir + "\\modules\\ext\\updater.jar";

        // if user updater does not exist, use updater from platform
        if (!fileExists(updaterPath.c_str())) {
            baseUpdaterPath = platformDir.c_str();
            updaterPath = platformDir + "\\modules\\ext\\updater.jar";
        }

        addToClassPath(updaterPath.c_str(), false);
        addFilesToClassPath(baseUpdaterPath, "\\modules\\ext\\locale", "updater_*.jar");
    }

    addToClassPath((jdkhome + "\\lib\\dt.jar").c_str(), true);
    addToClassPath((jdkhome + "\\lib\\tools.jar").c_str(), true);

    if (!cpAfter.empty()) {
        addToClassPath(cpAfter.c_str(), false);
    }
    logMsg("ClassPath: %s", classPath.c_str());
    return classPath;
}

void PlatformLauncher::addJarsToClassPathFrom(const char *dir) {
    addFilesToClassPath(dir, "lib\\patches", "*.jar");
    addFilesToClassPath(dir, "lib\\patches", "*.zip");

    addFilesToClassPath(dir, "lib", "*.jar");
    addFilesToClassPath(dir, "lib", "*.zip");

    addFilesToClassPath(dir, "lib\\locale", "*.jar");
    addFilesToClassPath(dir, "lib\\locale", "*.zip");
}

void PlatformLauncher::addFilesToClassPath(const char *dir, const char *subdir, const char *pattern) {
    logMsg("addFilesToClassPath()\n\tdir: %s\n\tsubdir: %s\n\tpattern: %s", dir, subdir, pattern);
    string path = dir;
    path += '\\';
    path += subdir;
    path += '\\';

    WIN32_FIND_DATA fd = {0};
    string patternPath = path + pattern;
    HANDLE hFind = FindFirstFile(patternPath.c_str(), &fd);
    if (hFind == INVALID_HANDLE_VALUE) {
        logMsg("Nothing found (%s)", patternPath.c_str());
        return;
    }
    do {
        string name = subdir;
        name += fd.cFileName;
        string fullName = path + fd.cFileName;
        if (addedToCP.insert(name).second) {
            addToClassPath(fullName.c_str());
        } else {
            logMsg("\"%s\" already added, skipping \"%s\"", name.c_str(), fullName.c_str());
        }
    } while (FindNextFile(hFind, &fd));
    FindClose(hFind);
}

void PlatformLauncher::addToClassPath(const char *path, bool onlyIfExists) {
    logMsg("addToClassPath()\n\tpath: %s\n\tonlyIfExists: %s", path, onlyIfExists ? "true" : "false");
    if (onlyIfExists && !fileExists(path)) {
        return;
    }

    if (!classPath.empty()) {
        classPath += ';';
    }
    classPath += path;
}

void PlatformLauncher::appendToHelp(const char *msg) {
    if (msg) {
        appendHelp = msg;
    }
}

bool PlatformLauncher::restartRequested() {
    return fileExists((userDir + RESTART_FILE_PATH).c_str());
}

void PlatformLauncher::onExit() {
    logMsg("onExit()");
    if (exitStatus == -252) {
        logMsg("Exiting from CLI client, will not restart.");
        return;
    }
    
    if (exiting) {
        logMsg("Already exiting, no need to schedule restart");
        return;
    }
    
    exiting = true;

    if (separateProcess) {
        logMsg("JVM in separate process, no need to restart");
        return;
    }

    bool restart = (nextAction == ARG_NAME_LA_START_APP || (nextAction == ARG_NAME_LA_START_AU && shouldAutoUpdateClusters(false)));
    if (!restart && restartRequested()) {
        restart = true;
        nextAction = ARG_NAME_LA_START_APP;
    }

    if (restart) {
        string cmdLine = GetCommandLine();
        logMsg("Old command line: %s", cmdLine.c_str());
        string::size_type bslashPos = cmdLine.find_last_of('\\');
        string::size_type pos = cmdLine.find(ARG_NAME_LA_START_APP);
        if ((bslashPos == string::npos || bslashPos < pos) && pos != string::npos) {
            cmdLine.erase(pos, strlen(ARG_NAME_LA_START_APP));
        }
        pos = cmdLine.find(ARG_NAME_LA_START_AU);
        if ((bslashPos == string::npos || bslashPos < pos) && pos != string::npos) {
            cmdLine.erase(pos, strlen(ARG_NAME_LA_START_AU));
        }

        if (*cmdLine.rbegin() != ' ') {
            cmdLine += ' ';
        }
        if (!parentProcID.empty() && cmdLine.find(ARG_NAME_LA_PPID) == string::npos) {
            cmdLine += ARG_NAME_LA_PPID;
            cmdLine += ' ';
            cmdLine += parentProcID;
        }

        if (*cmdLine.rbegin() != ' ') {
            cmdLine += ' ';
        }
        cmdLine += nextAction;

        logMsg("New command line: %s", cmdLine.c_str());
        char cmdLineStr[32 * 1024] = "";
        strcpy(cmdLineStr, cmdLine.c_str());
        STARTUPINFO si = {0};
        PROCESS_INFORMATION pi = {0};
        si.cb = sizeof(STARTUPINFO);

        if (!CreateProcess(NULL, cmdLineStr, NULL, NULL, TRUE, 0, NULL, NULL, &si, &pi)) {
            logErr(true, true, "Failed to create process.");
            return;
        }
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
    }
}
