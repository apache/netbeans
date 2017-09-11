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

#ifndef _JVMLAUNCHER_H
#define	_JVMLAUNCHER_H

#include <windows.h>
#include <string>
#include <list>
#include "jni.h"
#include "utilsfuncs.h"

class JvmLauncher {
    static const int MAX_ARGS_LEN = 32*1024;

    static const char *JDK_KEY;
    static const char *JRE_KEY;
    static const char *CUR_VERSION_NAME;
    static const char *JAVA_HOME_NAME;
    static const char *JAVA_BIN_DIR;
    static const char *JAVA_EXE_FILE;
    static const char *JAVAW_EXE_FILE;
    static const char *JAVA_CLIENT_DLL_FILE;
    static const char *JAVA_SERVER_DLL_FILE;
    static const char *JAVA_JRE_PREFIX;
    static const char *JNI_CREATEVM_FUNC;

public:
    JvmLauncher();
    virtual ~JvmLauncher();

    bool initialize(const char *javaPathOrMinVersion);
    bool getJavaPath(std::string &path);
    bool start(const char *mainClassName, const std::list<std::string> &args, const std::list<std::string> &options, bool &separateProcess, DWORD *retCode);

    void setSuppressConsole(bool val) {
        suppressConsole = val;
    }

private:
    JvmLauncher(const JvmLauncher& orig);

    bool checkJava(const char *javaPath, const char *prefix);
    bool findJava(const char *minJavaVersion);
    bool findJava(const char *javaKey, const char *prefix, const char *minJavaVersion);
    bool startOutProcJvm(const char *mainClassName, const std::list<std::string> &args, const std::list<std::string> &options, DWORD *retCode);
    bool startInProcJvm(const char *mainClassName, const std::list<std::string> &args, const std::list<std::string> &options);
    bool isVersionString(const char *str);
    bool canLoadJavaDll();
    bool findClientOption(const std::list<std::string> &options);

private:
    bool suppressConsole;
    std::string javaExePath;
    std::string javawExePath;
    std::string javaDllPath;
    std::string javaClientDllPath;
    std::string javaServerDllPath;
    std::string javaPath;
    std::string javaBinPath;

    class PrepareDllPath {
    public:
        PrepareDllPath(const char *dllDirectory)
            : setDllDirectory(0) {
            logMsg("PrepareDllPath: %s", dllDirectory);
            oldCurDir[0] = '\0';

            // SetDllDirectory is present since XP SP1, so we have to load it dynamically
            HINSTANCE hKernel32 = GetModuleHandle("kernel32");
            if (!hKernel32) {
                logErr(true, false, "Cannot load kernel32.");
                return;
            }

            LPFNSDD setDllDirectory = (LPFNSDD)GetProcAddress(hKernel32, "SetDllDirectoryA");
            if (setDllDirectory) {
                setDllDirectory(dllDirectory);
            } else {
                logErr(true, false, "Cannot find SetDllDirectoryA");
            }
            GetCurrentDirectory(MAX_PATH, oldCurDir);
            SetCurrentDirectory(dllDirectory);
        }
        ~PrepareDllPath() {
            if (setDllDirectory) {
                setDllDirectory(NULL);
            }
            if (oldCurDir[0]) {
                SetCurrentDirectory(oldCurDir);
            }
        }
    private:
        typedef BOOL (WINAPI *LPFNSDD)(LPCTSTR lpPathname);
        LPFNSDD setDllDirectory;
        char oldCurDir[MAX_PATH];
    };
};

#endif	/* _JVMLAUNCHER_H */

