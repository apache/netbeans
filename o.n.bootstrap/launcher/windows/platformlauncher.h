/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2016 Oracle and/or its affiliates. All rights reserved.
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

