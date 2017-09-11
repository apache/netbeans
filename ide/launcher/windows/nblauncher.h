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
    virtual bool areWeOn32bits();
    virtual void adjustHeapAndPermGenSize();
    virtual bool findUserDir(const char *str);
    virtual bool findCacheDir(const char *str);
    virtual const char * getAppName();
    virtual const char * getDefUserDirOptName();
    virtual const char * getDefCacheDirOptName();
    virtual const char * getDefOptionsOptName();
    virtual const char * getExtraClustersOptName();
    virtual const char * getJdkHomeOptName();
    virtual const char * getCurrentDir();

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

