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

#ifndef _NBLAUNCHER_H
#define	_NBLAUNCHER_H

#include <string>
#include <windows.h>
#include <cstddef>
#include "cmdargs.h"

class NbLauncher {
protected:
    static const char *NBEXEC_FILE_PATH;
    static const char *OPT_NB_DEFAULT_USER_DIR;
    static const char *OPT_NB_DEFAULT_CACHE_DIR;
    static const char *OPT_NB_DEFAULT_OPTIONS;
    static const char *OPT_NB_EXTRA_CLUSTERS;
    static const char *OPT_NB_JDK_HOME;
    static const char *REG_SHELL_FOLDERS_KEY;
    static const char *HOME_TOKEN;
    static const char *DEFAULT_USERDIR_ROOT_TOKEN;
    static const char *DEFAULT_CACHEDIR_ROOT_TOKEN;
    static const char *CON_ATTACH_MSG;
    static const char *NETBEANS_DIRECTORY;
    static const char *NETBEANS_CACHES_DIRECTORY;

private:
    static const char *ENV_USER_PROFILE;
    static const char *REG_DESKTOP_NAME;
    static const char *REG_DEFAULT_USERDIR_ROOT;
    static const char *REG_DEFAULT_CACHEDIR_ROOT;
    static const char* staticOptions[];

    typedef int (*StartPlatform)(int argc, char *argv[]);

public:
    NbLauncher();
    virtual ~NbLauncher();

    int start(int argc, char *argv[]);
    int start(char *cmdLine);

protected:
    virtual bool initBaseNames();
    virtual void addSpecificOptions(CmdArgs &args);
    virtual bool findUserDir(const char *str);
    virtual bool findCacheDir(const char *str);
    virtual const char * getAppName();
    virtual const char * getDefUserDirOptName();
    virtual const char * getDefCacheDirOptName();
    virtual const char * getDefOptionsOptName();
    virtual const char * getExtraClustersOptName();
    virtual const char * getJdkHomeOptName();
    virtual const char * getCurrentDir();
    virtual std::string constructApplicationDir(const std::string& dir, bool cache);

private:
    NbLauncher(const NbLauncher& orig);
    bool readClusterFile();
    bool parseArgs(int argc, char *argv[]);
    bool parseConfigFile(const char* path);    
    bool getOption(char *&str, const char *opt);
    void addCluster(const char *cl);
    void addExtraClusters();
    std::string getDefaultUserDirRoot();
    std::string getDefaultCacheDirRoot();

protected:
    std::string baseDir;
    std::string appName;
    std::string platformDir;
    std::string userHome;
    std::string userDir;
    std::string cacheDir;
    std::string defUserDirRoot;
    std::string defCacheDirRoot;
    std::string clusters;
    std::string extraClusters;
    std::string nbOptions;
    std::string jdkHome;
    
private:
    bool customUserDirFound;
};

#endif	/* _NBLAUNCHER_H */

