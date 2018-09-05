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
    // registry key change starting with version 9 
    static const char *JDK_POST9_KEY;
    static const char *JRE_POST9_KEY;
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

