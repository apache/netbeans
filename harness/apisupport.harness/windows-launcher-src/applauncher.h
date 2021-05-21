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

#ifndef _APPLAUNCHER_H
#define	_APPLAUNCHER_H

#include <string>
#include <windows.h>

#include "shlobj.h"
#include "../../../nb/ide.launcher/windows/nblauncher.h"

class AppLauncher : public NbLauncher {

    static const char *OPT_DEFAULT_USER_DIR;
    static const char *OPT_DEFAULT_CACHE_DIR;
    static const char *OPT_DEFAULT_OPTIONS;
    static const char *OPT_EXTRA_CLUSTERS;
    static const char *OPT_JDK_HOME;
    static const char *APPNAME_TOKEN;
    static const char *REG_APPDATA_NAME;
    static const char *CACHE_SUFFIX;

public:
    AppLauncher();
    virtual ~AppLauncher();

protected:
    virtual bool initBaseNames();
    virtual const char * getAppName();
    virtual void addSpecificOptions(CmdArgs &args);
    virtual void adjustHeapSize();
    virtual bool findUserDir(const char *str);
    virtual bool findCacheDir(const char *str);
    virtual const char * getDefUserDirOptName();
    virtual const char * getDefCacheDirOptName();
    virtual const char * getDefOptionsOptName();
    virtual const char * getExtraClustersOptName();
    virtual const char * getJdkHomeOptName();
    virtual const char * getCurrentDir();
    virtual std::string constructApplicationDir(const std::string& dir, bool cache);

private:
    AppLauncher(const AppLauncher& orig);
};

#endif	/* _NBLAUNCHER_H */

