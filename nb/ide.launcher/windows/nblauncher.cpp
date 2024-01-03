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

#ifndef _WIN32_WINNT        
#define _WIN32_WINNT 0x05010100
#endif

#include <shlobj.h>
#include <winnls.h>
#include "nblauncher.h"
#include "../../../platform/o.n.bootstrap/launcher/windows/utilsfuncs.h"
#include "../../../platform/o.n.bootstrap/launcher/windows/argnames.h"
#include "../../../platform/o.n.bootstrap/launcher/windows/nbexecloader.h"

using namespace std;

const char *NbLauncher::NBEXEC_FILE_PATH = NBEXEC_DLL;
const char *NbLauncher::OPT_NB_DEFAULT_USER_DIR = "netbeans_default_userdir=";
const char *NbLauncher::OPT_NB_DEFAULT_CACHE_DIR = "netbeans_default_cachedir=";
const char *NbLauncher::OPT_NB_DEFAULT_OPTIONS = "netbeans_default_options=";
const char *NbLauncher::OPT_NB_EXTRA_CLUSTERS = "netbeans_extraclusters=";
const char *NbLauncher::OPT_NB_JDK_HOME = "netbeans_jdkhome=";
const char *NbLauncher::ENV_USER_PROFILE = "USERPROFILE";
const char *NbLauncher::HOME_TOKEN = "${HOME}";
const char *NbLauncher::DEFAULT_USERDIR_ROOT_TOKEN = "${DEFAULT_USERDIR_ROOT}";
const char *NbLauncher::DEFAULT_CACHEDIR_ROOT_TOKEN = "${DEFAULT_CACHEDIR_ROOT}";
const char *NbLauncher::NETBEANS_DIRECTORY = "\\NetBeans\\";
const char *NbLauncher::NETBEANS_CACHES_DIRECTORY = "\\NetBeans\\Cache\\";

const char *NbLauncher::CON_ATTACH_MSG = 
"\n\nThe launcher has determined that the parent process has a console and will reuse it for its own console output.\n"
"Closing the console will result in termination of the running program.\n"
"Use '--console suppress' to suppress console output.\n"
"Use '--console new' to create a separate console window.\n";

const char *NbLauncher::staticOptions[] = {
    "-J-Dnetbeans.importclass=org.netbeans.upgrade.AutoUpgrade",
    "--branding",
    "nb"
};

NbLauncher::NbLauncher() {
}

NbLauncher::NbLauncher(const NbLauncher& orig) {
}

NbLauncher::~NbLauncher() {
}

int NbLauncher::start(char *cmdLine) {
    CmdArgs args(50);
    args.addCmdLine(cmdLine);
    return start(args.getCount(), args.getArgs());
}

int NbLauncher::start(int argc, char *argv[]) {
    SetErrorMode(SetErrorMode(0) | SEM_FAILCRITICALERRORS | SEM_NOOPENFILEERRORBOX);
    
    DWORD parentProcID = 0;
    if (!checkLoggingArg(argc, argv, true) || !setupProcess(argc, argv, parentProcID, CON_ATTACH_MSG) || !initBaseNames() || !readClusterFile()) {
        return -1;
    }

    parseConfigFile((baseDir + "\\etc\\" + getAppName() + ".conf").c_str());

    if (!parseArgs(argc, argv)) {
        return -1;
    }
    string oldUserDir = userDir;
    parseConfigFile((userDir + "\\etc\\" + getAppName() + ".conf").c_str());
    userDir = oldUserDir;

    addExtraClusters();
    string nbexecPath;
    SetDllDirectory(baseDir.c_str());
    if (dirExists(platformDir.c_str())) {
        nbexecPath = platformDir;
    } else {
        nbexecPath = baseDir + '\\' + platformDir;
    }
    if (!dirExists(nbexecPath.c_str())) {
        logErr(false, true, "Could not find platform cluster:\n%s", nbexecPath.c_str());
        return false;
    }

    CmdArgs newArgs(argc + 20);
    addSpecificOptions(newArgs);
    
    if (!clusters.empty()) {
        newArgs.add(ARG_NAME_CLUSTERS);
        newArgs.add(clusters.c_str());
    }
    if (!userDir.empty()) {
        newArgs.add(ARG_NAME_USER_DIR);
        newArgs.add(userDir.c_str());
    }
    if (!defUserDirRoot.empty()) {
        newArgs.add(ARG_DEFAULT_USER_DIR_ROOT);
        newArgs.add(defUserDirRoot.c_str());
    }
    if (!cacheDir.empty() && !customUserDirFound) {
        newArgs.add(ARG_NAME_CACHE_DIR);
        newArgs.add(cacheDir.c_str());
    }
    if (!nbOptions.empty()) {
        newArgs.addCmdLine(nbOptions.c_str());
    }
    for (int i = 0; i < argc; i++) {
        newArgs.add(argv[i]);
    }
    if (!jdkHome.empty()) {
        newArgs.add(ARG_NAME_JDKHOME);
        newArgs.add(jdkHome.c_str());
    }
    if (parentProcID) {
        newArgs.add(ARG_NAME_LA_PPID);
        char tmp[16] = "";
        newArgs.add(itoa(parentProcID, tmp, 10));
    }
    nbexecPath += NBEXEC_FILE_PATH;

    const char *curDir = getCurrentDir();
    if (curDir) {
        char olddir[MAX_PATH];
        DWORD rc = GetCurrentDirectory(MAX_PATH, olddir);
        if (rc == 0) {
            logErr(true, false, "Failed to get current directory");
        } else {
            string od = string(olddir);
            od.insert(0, "-J-Dnetbeans.user.dir=");
            newArgs.add(od.c_str());
        }
        logMsg("Changing current directory to: \"%s\"", curDir);
        SetCurrentDirectory(curDir);
    }

    NBExecLoader loader;
    return loader.start(nbexecPath.c_str(), newArgs.getCount(), newArgs.getArgs());
}

UINT GetAnsiCodePageForLocale(LCID lcid) {
    // See https://devblogs.microsoft.com/oldnewthing/20161007-00/?p=94475
    UINT acp;
    int sizeInChars = sizeof(acp) / sizeof(TCHAR);
    if (GetLocaleInfo(lcid,
                      LOCALE_IDEFAULTANSICODEPAGE | LOCALE_RETURN_NUMBER,
                      reinterpret_cast<LPTSTR>(&acp),
                      sizeInChars) != sizeInChars)
    {
        return 0;
    }
    return acp;
}

bool NbLauncher::initBaseNames() {
    char path[MAX_PATH] = "";
    getCurrentModulePath(path, MAX_PATH);
    logMsg("Executable: %s", path);
    char *bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    appName = bslash + 1;
    appName.erase(appName.rfind('.'));
    
    if (ARCHITECTURE == 64) {
        appName = appName.erase(appName.length() - 2);
    }
    
    logMsg("Application name: %s", appName.c_str());

    *bslash = '\0';
    bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    *bslash = '\0';        

    /* Useful messages for debugging character set issues. On Java versions where
    https://bugs.openjdk.org/browse/JDK-8272352 has been fixed, NetBeans should now run fine when
    there are Unicode characters in the NetBeans installation path, the JDK path, the user/cache
    directory paths, or in the java.io.tmpdir path (the latter sometimes being a problem for JNA,
    which is used by FlatLAF). Since the JVM is started in-process via JNI, the Java environment
    will inherit the UTF-8 code page setting that we have set in the launcher's application
    manifest, without requiring the user to change regional settings in the Control Panel. (JEP 400
    might eventually do something similar for the java.exe/javaw.exe executables. See
    https://www.mail-archive.com/core-libs-dev@openjdk.java.net/msg80489.html .) */
    logMsg("ANSI code page per GetACP()              : %d", GetACP());
    logMsg("ANSI code page per GetConsoleCP()        : %d", GetConsoleCP());
    logMsg("ANSI code page for GetThreadLocale()     : %d", GetAnsiCodePageForLocale(GetThreadLocale()));
    logMsg("ANSI code page for GetUserDefaultLCID()  : %d", GetAnsiCodePageForLocale(GetUserDefaultLCID()));
    logMsg("ANSI code page for GetSystemDefaultLCID(): %d", GetAnsiCodePageForLocale(GetSystemDefaultLCID()));

    baseDir = path;

    logMsg("Base dir: %s", baseDir.c_str());
    return true;
}

void NbLauncher::addCluster(const char *cluster) {

    class SetCurDir {
    public:
        SetCurDir(const char *dir) {
            oldCurDir[0] = '\0';
            DWORD rc = GetCurrentDirectory(MAX_PATH, oldCurDir);
            if (rc == 0) {
                logErr(true, false, "Failed to get current directory");
                return;
            }
            if (rc > MAX_PATH) {
                logMsg("Failed to get current directory, buffer is too small.");
                return;
            }
            if (!SetCurrentDirectory(dir)) {
                logErr(true, true, "Failed to set current directory to \"%s\"", dir);
                oldCurDir[0] = '\0';
            }
        }

        ~SetCurDir() {
            if (oldCurDir[0]) {
                if (!SetCurrentDirectory(oldCurDir)) {
                    logErr(true, true, "Failed to set current directory to \"%s\"", oldCurDir);
                }
            }
        }
    private:
        char oldCurDir[MAX_PATH];
    };

    logMsg("addCluster: %s", cluster);
    SetCurDir setCurDir(baseDir.c_str());
    char clusterPath[MAX_PATH + 1] = {0};
    strncpy(clusterPath, cluster, MAX_PATH);
    if (!normalizePath(clusterPath, MAX_PATH)) {
        logMsg("Invalid cluster path: %s", cluster);
        return;
    }
    if (!clusters.empty()) {
        clusters += ';';
    }
    logMsg("Adding cluster %s", clusterPath);
    clusters += clusterPath;
}

void NbLauncher::addExtraClusters() {
    logMsg("addExtraClusters()");
    const char delim = ';';
    string::size_type start = extraClusters.find_first_not_of(delim, 0);
    string::size_type end = extraClusters.find_first_of(delim, start);
    while (string::npos != end || string::npos != start) {
        string cluster = extraClusters.substr(start, end - start);
        addCluster(cluster.c_str());
        start = extraClusters.find_first_not_of(delim, end);
        end = extraClusters.find_first_of(delim, start);
    }
}

bool NbLauncher::readClusterFile() {
    clusters = "";
    string clusterFile = baseDir + "\\etc\\" + getAppName() + ".clusters";
    logMsg("readClusterFile() file: %s", clusterFile.c_str());

    FILE* file = fopen(clusterFile.c_str(), "r");
    if (!file) {
        logErr(true, true, "Cannot open file \"%s\" for reading.", clusterFile.c_str());
        return false;
    }

    char line[4096] = "";
    while (fgets(line, sizeof(line), file)) {
        char *str = skipWhitespaces(line);
        if (*str == '#' || *str == '\0') {
            continue;
        }
        char *pc = str;
        while (*pc != '\0' && *pc != '\t' && *pc != '\n' && *pc != '\r') {
            pc++;
        }
        *pc = '\0';

        if (platformDir.empty()) {
            char *slash = strrchr(str, '\\');
            if (!slash) {
                slash = strrchr(str, '/');
            }
            char *dir = slash ? slash + 1 : str;
            if (strncmp(dir, "platform", strlen("platform")) == 0) {
                platformDir = str;
            } else {
                addCluster(str);
            }
        } else {
            addCluster(str);
        }
    }
    bool ok = ferror(file) == 0;
    if (!ok) {
        logErr(true, true, "Error while reading file \"%s\".", clusterFile.c_str());
    }
    fclose(file);
    return ok;
}

bool NbLauncher::parseArgs(int argc, char *argv[]) {
#define CHECK_ARG \
    if (i+1 == argc) {\
        logErr(false, true, "Argument is missing for \"%s\" option.", argv[i]);\
        return false;\
    }

    logMsg("parseArgs():");
    for (int i = 0; i < argc; i++) {
        logMsg("\t%s", argv[i]);
    }
    customUserDirFound = 0;
    for (int i = 0; i < argc; i++) {
        if (strcmp(ARG_NAME_USER_DIR, argv[i]) == 0) {
            CHECK_ARG;
            char tmp[MAX_PATH + 1] = {0};
            strncpy(tmp, argv[++i], MAX_PATH);
            if (!normalizePath(tmp, MAX_PATH)) {
                logErr(false, true, "User directory path \"%s\" is not valid.", argv[i]);
                return false;
            }
            customUserDirFound = 1;
            userDir = tmp;
            logMsg("User dir: %s", userDir.c_str());
        }
        if (strcmp(ARG_NAME_CACHE_DIR, argv[i]) == 0) {
            CHECK_ARG;
            char tmp[MAX_PATH + 1] = {0};
            strncpy(tmp, argv[++i], MAX_PATH);
            if (!normalizePath(tmp, MAX_PATH)) {
                logErr(false, true, "Cache directory path \"%s\" is not valid.", argv[i]);
                return false;
            }
            cacheDir = tmp;
            logMsg("Cache dir: %s", cacheDir.c_str());
        }
    }
    logMsg("parseArgs() finished");
    return true;
}

bool NbLauncher::findUserDir(const char *str) {
    logMsg("NbLauncher::findUserDir()");    
    if (strncmp(str, HOME_TOKEN, strlen(HOME_TOKEN)) == 0) {
        if (userHome.empty()) {
            char *userProfile = getenv(ENV_USER_PROFILE);
            if (userProfile) {
                userHome = userProfile;
            } else {
                TCHAR userHomeChar[MAX_PATH]; 
                if (FAILED(SHGetFolderPath(NULL, CSIDL_DESKTOP, NULL, 0, userHomeChar))) {    
                    return false;
                }
                userHome = userHomeChar;
                userHome.erase(userHome.rfind('\\'));
            }
            logMsg("User home: %s", userHome.c_str());
        }
        userDir = userHome + (str + strlen(HOME_TOKEN));
    } else if (strncmp(str, DEFAULT_USERDIR_ROOT_TOKEN, strlen(DEFAULT_USERDIR_ROOT_TOKEN)) == 0) {       
        std::string s = std::string("Replacing ") + DEFAULT_USERDIR_ROOT_TOKEN;
        logMsg(s.c_str());
        userDir = getDefaultUserDirRoot() + (str + strlen(DEFAULT_USERDIR_ROOT_TOKEN));
    } else {
        getDefaultUserDirRoot();
        userDir = str;
    }
    return true;
}

bool NbLauncher::findCacheDir(const char *str) {
    logMsg("NbLauncher::findCacheDir()");
    if (strncmp(str, HOME_TOKEN, strlen(HOME_TOKEN)) == 0) {
        if (userHome.empty()) {
            char *userProfile = getenv(ENV_USER_PROFILE);
            if (userProfile) {
                userHome = userProfile;
            } else {
                TCHAR userHomeChar[MAX_PATH]; 
                if (FAILED(SHGetFolderPath(NULL, CSIDL_DESKTOP, NULL, 0, userHomeChar))) {    
                    return false;
                }
                userHome = userHomeChar;
                userHome.erase(userHome.rfind('\\'));
            }
            logMsg("User home: %s", userHome.c_str());
        }
        cacheDir = userHome + (str + strlen(HOME_TOKEN));
    } else if (strncmp(str, DEFAULT_CACHEDIR_ROOT_TOKEN, strlen(DEFAULT_CACHEDIR_ROOT_TOKEN)) == 0) {   
        std::string s = std::string("Replacing ") + DEFAULT_CACHEDIR_ROOT_TOKEN;
        logMsg(s.c_str());
        cacheDir = getDefaultCacheDirRoot() + (str + strlen(DEFAULT_CACHEDIR_ROOT_TOKEN));
    } else {
        getDefaultCacheDirRoot();
        cacheDir = str;
    }
    return true;
}

string NbLauncher::getDefaultUserDirRoot() {
    TCHAR defUserDirRootChar[MAX_PATH];
    if (FAILED(SHGetFolderPath(NULL, CSIDL_APPDATA, NULL, 0, defUserDirRootChar))) {
        return std::string();
    }
    defUserDirRoot = constructApplicationDir((string) defUserDirRootChar, false);
    defUserDirRoot.erase(defUserDirRoot.rfind('\\'));
    logMsg("Default Userdir Root: %s", defUserDirRoot.c_str());
    return defUserDirRoot;
}

string NbLauncher::getDefaultCacheDirRoot() {
    TCHAR defCacheDirRootChar[MAX_PATH];
    if (FAILED(SHGetFolderPath(NULL, CSIDL_LOCAL_APPDATA, NULL, 0, defCacheDirRootChar))) {
        return std::string();
    }
    defCacheDirRoot = constructApplicationDir((string) defCacheDirRootChar, true);
    defCacheDirRoot.erase(defCacheDirRoot.rfind('\\'));
    logMsg("Default Cachedir Root: %s", defCacheDirRoot.c_str());
    return defCacheDirRoot;
}

bool NbLauncher::getOption(char *&str, const char *opt) {
    if (strncmp(str, opt, strlen(opt)) == 0) {
        str += strlen(opt);
        char *end = trimWhitespaces(str);
        if (*str == '"') {
            str++;
        }
        if (end >= str && *end == '"') {
            *end = '\0';
        }
        logMsg("Option found: %s%s", opt, str);
        return true;
    }
    return false;
}

bool NbLauncher::parseConfigFile(const char* path) {
    logMsg("parseConfigFile(%s)", path);
    FILE *file = fopen(path, "r");
    if (!file) {
        logErr(true, false, "Cannot open file \"%s\" for reading.", path);
        return false;
    }
    
    char line[4096] = "";
    while (fgets(line, sizeof(line), file)) {
        char *str = skipWhitespaces(line);
        if (*str == '#') {
            continue;
        }
        if (getOption(str, getDefUserDirOptName())) {
             findUserDir(str);
             logMsg("User dir: %s", userDir.c_str());
        } else if (getOption(str, getDefCacheDirOptName())) {
             findCacheDir(str);
             logMsg("Cache dir: %s", cacheDir.c_str());
        } else if (getOption(str, getDefOptionsOptName())) {
            // replace \" by "
            int len = strlen(str);
            int k = 0;
            for (int i = 0; i < len; i++) {
                if (str[i] == '\\' && str[i+1] == '\"') {
                    continue;
                }
                str[k++] = str[i];
            }
            str[k] = '\0';
            nbOptions = str;
            logMsg("After replacement: %s", nbOptions.c_str());

        } else if (getOption(str, getExtraClustersOptName())) {
            extraClusters = str;
        } else if (getOption(str, getJdkHomeOptName())) {
            jdkHome = str;
        }
    }
    bool ok = ferror(file) == 0;
    if (!ok) {
        logErr(true, false, "Error while reading file \"%s\".", path);
    }
    fclose(file);
    return true;
}

typedef void (WINAPI *PGNSI)(LPSYSTEM_INFO);

const char * NbLauncher::getAppName() {
    return "netbeans";
}

void NbLauncher::addSpecificOptions(CmdArgs &args) {
    for (unsigned i = 0; i < sizeof (staticOptions) / sizeof (char*); i++) {
        args.add(staticOptions[i]);
    }
}

const char * NbLauncher::getDefUserDirOptName() {
    return OPT_NB_DEFAULT_USER_DIR;
}

const char * NbLauncher::getDefCacheDirOptName() {
    return OPT_NB_DEFAULT_CACHE_DIR;
}


const char * NbLauncher::getDefOptionsOptName() {
    return OPT_NB_DEFAULT_OPTIONS;
}

const char * NbLauncher::getExtraClustersOptName() {
    return OPT_NB_EXTRA_CLUSTERS;
}

const char * NbLauncher::getJdkHomeOptName() {
    return OPT_NB_JDK_HOME;
}

const char * NbLauncher::getCurrentDir() {
    return 0;
}

std::string NbLauncher::constructApplicationDir(const std::string& dir, bool cache) {
   if (cache) {
       return dir + NETBEANS_CACHES_DIRECTORY;
   } else {
       return dir + NETBEANS_DIRECTORY;
   }
}
