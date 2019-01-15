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

#include "jvmlauncher.h"
#include <assert.h>

using namespace std;

const char *JvmLauncher::JDK_KEY = "Software\\JavaSoft\\Java Development Kit";
const char *JvmLauncher::JRE_KEY = "Software\\JavaSoft\\Java Runtime Environment";
const char *JvmLauncher::JDK_POST9_KEY = "Software\\JavaSoft\\JDK";
const char *JvmLauncher::JRE_POST9_KEY = "Software\\JavaSoft\\JRE";
const char *JvmLauncher::CUR_VERSION_NAME = "CurrentVersion";
const char *JvmLauncher::JAVA_HOME_NAME = "JavaHome";
const char *JvmLauncher::JAVA_BIN_DIR = "\\bin";
const char *JvmLauncher::JAVA_EXE_FILE = "\\bin\\java.exe";
const char *JvmLauncher::JAVAW_EXE_FILE = "\\bin\\javaw.exe";
const char *JvmLauncher::JAVA_CLIENT_DLL_FILE = "\\bin\\client\\jvm.dll";
const char *JvmLauncher::JAVA_SERVER_DLL_FILE = "\\bin\\server\\jvm.dll";
const char *JvmLauncher::JAVA_JRE_PREFIX = "\\jre";
const char *JvmLauncher::JNI_CREATEVM_FUNC = "JNI_CreateJavaVM";

extern void exitHook(int status);

JvmLauncher::JvmLauncher()
    : suppressConsole(false) {
}

JvmLauncher::JvmLauncher(const JvmLauncher& orig) {
}

JvmLauncher::~JvmLauncher() {
}

bool JvmLauncher::checkJava(const char *path, const char *prefix) {
    assert(path);
    assert(prefix);
    logMsg("checkJava(%s)", path);
    javaPath = path;
    if (*javaPath.rbegin() == '\\') {
        javaPath.erase(javaPath.length() - 1, 1);
    }
    javaExePath = javaPath + prefix + JAVA_EXE_FILE;
    javawExePath = javaPath + prefix + JAVAW_EXE_FILE;
    javaClientDllPath = javaPath + prefix + JAVA_CLIENT_DLL_FILE;
    javaServerDllPath = javaPath + prefix + JAVA_SERVER_DLL_FILE;
    if (!fileExists(javaClientDllPath.c_str())) {
        javaClientDllPath = "";
    }
    if (!fileExists(javaServerDllPath.c_str())) {
        javaServerDllPath = "";
    }
    javaBinPath = javaPath + prefix + JAVA_BIN_DIR;
    if (fileExists(javaExePath.c_str()) || !javaClientDllPath.empty() || !javaServerDllPath.empty()) {
        if (!fileExists(javawExePath.c_str())) {
            logMsg("javaw.exe not exists, forcing java.exe");
            javawExePath = javaExePath;
        }        
        return true;
    }

    javaPath.clear();
    javaBinPath.clear();
    javaExePath.clear();
    javawExePath.clear();
    javaClientDllPath.clear();
    javaServerDllPath.clear();
    return false;
}

bool JvmLauncher::initialize(const char *javaPathOrMinVersion) {
    logMsg("JvmLauncher::initialize()\n\tjavaPathOrMinVersion: %s", javaPathOrMinVersion);
    assert(javaPathOrMinVersion);
    if (isVersionString(javaPathOrMinVersion)) {
        return findJava(javaPathOrMinVersion);
    } else {
        return (checkJava(javaPathOrMinVersion, JAVA_JRE_PREFIX) || checkJava(javaPathOrMinVersion, ""));
    }
}

bool JvmLauncher::getJavaPath(string &path) {
    logMsg("JvmLauncher::getJavaPath()");
    path = javaPath;
    return !javaPath.empty();
}

bool JvmLauncher::start(const char *mainClassName, const list<string> &args, const list<string> &options, bool &separateProcess, DWORD *retCode) {
    assert(mainClassName);
    logMsg("JvmLauncher::start()\n\tmainClassName: %s\n\tseparateProcess: %s",
            mainClassName, separateProcess ? "true" : "false");
    logMsg("  args:");
    for (list<string>::const_iterator it = args.begin(); it != args.end(); ++it) {
        logMsg("\t%s", it->c_str());
    }
    logMsg("  options:");
    for (list<string>::const_iterator it = options.begin(); it != options.end(); ++it) {
        logMsg("\t%s", it->c_str());
    }

    if (!javaExePath.empty() && javaClientDllPath.empty() && javaServerDllPath.empty()) {
        logMsg("Found only java.exe at %s. No DLLs. Falling back to java.exe\n", javaExePath.c_str());
        separateProcess = true;
    } else {
        if (javaExePath.empty() || (javaClientDllPath.empty() && javaServerDllPath.empty())) {
            if (!initialize("")) {
                return false;
            }
        }
    }  

    if (!separateProcess) {
        // both client/server found, check option which should be used
        if (!javaClientDllPath.empty() && !javaServerDllPath.empty()) {
            javaDllPath = findClientOption(options) ? javaClientDllPath : javaServerDllPath;
        } else {
            javaDllPath = javaClientDllPath.empty() ? javaServerDllPath : javaClientDllPath;
        }

        // it is necessary to absolutize dll path because current dir has to be
        // temporarily changed for dll loading
        char absoluteJavaDllPath[MAX_PATH] = "";
        strncpy(absoluteJavaDllPath, javaDllPath.c_str(), MAX_PATH);
        normalizePath(absoluteJavaDllPath, MAX_PATH);
        javaDllPath = absoluteJavaDllPath;

        logMsg("Java DLL path: %s", javaDllPath.c_str());
        if (!canLoadJavaDll()) {
            logMsg("Falling back to running Java in a separate process; DLL cannot be loaded (64-bit DLL?).");
            separateProcess = true;
        }
    }

    return separateProcess ? startOutProcJvm(mainClassName, args, options, retCode)
            : startInProcJvm(mainClassName, args, options);
}

bool JvmLauncher::findClientOption(const list<string> &options) {
    for (list<string>::const_iterator it = options.begin(); it != options.end(); ++it) {
        if (*it == "-client") {
            return true;
        }
    }
    return false;
}

bool JvmLauncher::canLoadJavaDll() {
    // be prepared for stupid placement of msvcr71.dll in java installation
    // (in java 1.6/1.7 jvm.dll is dynamically linked to msvcr71.dll which si placed
    // in bin directory)
    PrepareDllPath prepare(javaBinPath.c_str());
    HMODULE hDll = LoadLibrary(javaDllPath.c_str());
    if (hDll) {
        FreeLibrary(hDll);
        return true;
    }
    logErr(true, false, "Cannot load %s.", javaDllPath.c_str());
    return false;
}

bool JvmLauncher::isVersionString(const char *str) {
    char *end = 0;
    strtod(str, &end);
    return *end == '\0';
}

bool JvmLauncher::startInProcJvm(const char *mainClassName, const std::list<std::string> &args, const std::list<std::string> &options) {
    class Jvm {
    public:

        Jvm(JvmLauncher *jvmLauncher)
            : hDll(0)
            , hSplash(0)
            , jvm(0)
            , env(0)
            , jvmOptions(0)
            , jvmLauncher(jvmLauncher)
        {
        }

        ~Jvm() {
            if (env && env->ExceptionOccurred()) {
                env->ExceptionDescribe();
            }

            if (jvm) {
                logMsg("Destroying JVM");
                jvm->DestroyJavaVM();
            }

            if (jvmOptions) {
                delete[] jvmOptions;
            }

            if (hDll) {
                FreeLibrary(hDll);
            }
            if (hSplash) {
                FreeLibrary(hSplash);
            }
        }

        bool init(const list<string> &options) {
            logMsg("JvmLauncher::Jvm::init()");
            logMsg("LoadLibrary(\"%s\")", jvmLauncher->javaDllPath.c_str());
            {
                PrepareDllPath prepare(jvmLauncher->javaBinPath.c_str());
                hDll = LoadLibrary(jvmLauncher->javaDllPath.c_str());
                if (!hDll) {
                    logErr(true, true, "Cannot load %s.", jvmLauncher->javaDllPath.c_str());
                    return false;
                }
                
                string pref = jvmLauncher->javaBinPath;
                pref += "\\splashscreen.dll";
                const string splash = pref;
                logMsg("Trying to load %s", splash.c_str());
                hSplash = LoadLibrary(splash.c_str());
                logMsg("Splash loaded as %d", hSplash);
            }

            CreateJavaVM createJavaVM = (CreateJavaVM) GetProcAddress(hDll, JNI_CREATEVM_FUNC);
            if (!createJavaVM) {
                logErr(true, true, "GetProcAddress for %s failed.", JNI_CREATEVM_FUNC);
                return false;
            }

            logMsg("JVM options:");
            jvmOptions = new JavaVMOption[options.size() + 1];
            int i = 0;
            for (list<string>::const_iterator it = options.begin(); it != options.end(); ++it, ++i) {
                const string &option = *it;
                logMsg("\t%s", option.c_str());
                if (option.find("-splash:") == 0 && hSplash > 0) {
                    const string splash = option.substr(8);
                    logMsg("splash at %s", splash.c_str());
                    
                    SplashInit splashInit = (SplashInit)GetProcAddress(hSplash, "SplashInit");
                    SplashLoadFile splashLoadFile = (SplashLoadFile)GetProcAddress(hSplash, "SplashLoadFile");
                    
                    logMsg("splash init %d and load %d", splashInit, splashLoadFile);
                    if (splashInit && splashLoadFile) {
                        splashInit();
                        splashLoadFile(splash.c_str());
                    }
                }
                jvmOptions[i].optionString = (char *) option.c_str();
                jvmOptions[i].extraInfo = 0;
            }
            JavaVMInitArgs jvmArgs;
            jvmOptions[options.size()].optionString = (char *) "exit";
            jvmOptions[options.size()].extraInfo    = (void *) &exitHook;
            
            jvmArgs.options = jvmOptions;
            jvmArgs.nOptions = options.size() + 1;
            jvmArgs.version = JNI_VERSION_1_4;
            jvmArgs.ignoreUnrecognized = JNI_TRUE;

            logMsg("Creating JVM...");
            if (createJavaVM(&jvm, &env, &jvmArgs) < 0) {
                logErr(false, true, "JVM creation failed");
                return false;
            }
            logMsg("JVM created.");
            return true;
        }
        typedef jint (CALLBACK *CreateJavaVM)(JavaVM **jvm, JNIEnv **env, void *args);
        typedef void (CALLBACK *SplashInit)();
        typedef int (CALLBACK *SplashLoadFile)(const char* file);

        HMODULE hDll;
        HMODULE hSplash;
        JavaVM *jvm;
        JNIEnv *env;
        JavaVMOption *jvmOptions;
        JvmLauncher *jvmLauncher;
    };

    Jvm jvm(this);
    if (!jvm.init(options)) {
        return false;
    }

    jclass mainClass = jvm.env->FindClass(mainClassName);
    if (!mainClass) {
        logErr(false, true, "Cannot find class %s.", mainClassName);
        return false;
    }

    jmethodID mainMethod = jvm.env->GetStaticMethodID(mainClass, "main", "([Ljava/lang/String;)V");
    if (!mainMethod) {
        logErr(false, true, "Cannot get main method.");
        return false;
    }
    
    jclass jclassString = jvm.env->FindClass("java/lang/String");
    if (!jclassString) {
        logErr(false, true, "Cannot find java/lang/String class");
        return false;
    }

    jstring jstringArg = jvm.env->NewStringUTF("");
    if (!jstringArg) {
        logErr(false, true, "NewStringUTF() failed");
        return false;
    }

    jobjectArray mainArgs = jvm.env->NewObjectArray(args.size(), jclassString, jstringArg);
    if (!mainArgs) {
        logErr(false, true, "NewObjectArray() failed");
        return false;
    }
    int i = 0;
    for (list<string>::const_iterator it = args.begin(); it != args.end(); ++it, ++i) {
        const string &arg = *it;
        const int len = 32*1024;
        char utf8[len] = "";
        if (convertAnsiToUtf8(arg.c_str(), utf8, len))
            logMsg("Conversion to UTF8 failed");
        jstring jstringArg = jvm.env->NewStringUTF(utf8);
        if (!jstringArg) {
            logErr(false, true, "NewStringUTF() failed");
            return false;
        }
        jvm.env->SetObjectArrayElement(mainArgs, i, jstringArg);
    }

    jvm.env->CallStaticVoidMethod(mainClass, mainMethod, mainArgs);
    return true;
}


bool JvmLauncher::startOutProcJvm(const char *mainClassName, const std::list<std::string> &args, const std::list<std::string> &options, DWORD *retCode) {
    string cmdLine = '\"' + (suppressConsole ? javawExePath : javaExePath) + '\"';
    cmdLine.reserve(32*1024);
    for (list<string>::const_iterator it = options.begin(); it != options.end(); ++it) {
        cmdLine += " \"";
        cmdLine += *it;
        cmdLine += "\"";
    }
    
    // mainClass and args
    cmdLine += ' ';
    cmdLine += mainClassName;
    for (list<string>::const_iterator it = args.begin(); it != args.end(); ++it) {
        if (javaClientDllPath.empty() && *it == "-client") {
            logMsg("Removing -client option, client java dll not found.");
            // remove client parameter, no client java found
            continue;
        }
        cmdLine += " \"";
        cmdLine += *it;
        cmdLine += "\"";
    }

    logMsg("Command line:\n%s", cmdLine.c_str());
    if (cmdLine.size() >= 32*1024) {
        logErr(false, true, "Command line is too long. Length: %u. Maximum length: %u.", cmdLine.c_str(), 32*1024);
        return false;
    }

    STARTUPINFO si = {0};
    si.cb = sizeof (STARTUPINFO);
    PROCESS_INFORMATION pi = {0};

    char cmdLineStr[32*1024] = "";
    strcpy(cmdLineStr, cmdLine.c_str());
    if (!CreateProcess(NULL, cmdLineStr, NULL, NULL, FALSE, CREATE_SUSPENDED, NULL, NULL, &si, &pi)) {
        logErr(true, true, "Failed to create process");
        return false;
    }

    disableFolderVirtualization(pi.hProcess);
    ResumeThread(pi.hThread);
    WaitForSingleObject(pi.hProcess, INFINITE);
    if (retCode) {
        GetExitCodeProcess(pi.hProcess, retCode);
    }
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    return true;
}

bool JvmLauncher::findJava(const char *minJavaVersion) {
    // scan for registry for jdk/jre version 9
    if (findJava(JDK_POST9_KEY, "", minJavaVersion)) {
        return true;
    }
    if (findJava(JRE_POST9_KEY, "", minJavaVersion)) {
        return true;
    }
    if (findJava(JDK_KEY, JAVA_JRE_PREFIX, minJavaVersion)) {
        return true;
    }
    if (findJava(JRE_KEY, "", minJavaVersion)) {
        return true;
    }
    javaPath = "";
    javaExePath = "";
    javaClientDllPath = "";
    javaServerDllPath = "";
    javaBinPath = "";
    return false;  
}

bool JvmLauncher::findJava(const char *javaKey, const char *prefix, const char *minJavaVersion) {
    logMsg("JvmLauncher::findJava()\n\tjavaKey: %s\n\tprefix: %s\n\tminJavaVersion: %s", javaKey, prefix, minJavaVersion);
    string value;
    bool result = false;
    if (getStringFromRegistry(HKEY_LOCAL_MACHINE, javaKey, CUR_VERSION_NAME, value)) {
        if (value >= minJavaVersion) {
            string path;
            if (getStringFromRegistry(HKEY_LOCAL_MACHINE, (string(javaKey) + "\\" + value).c_str(), JAVA_HOME_NAME, path)) {
                if (*path.rbegin() == '\\') {
                    path.erase(path.length() - 1, 1);
                }                
                result = checkJava(path.c_str(), prefix);
            }
        }
    }
    if(!result && isWow64()) {
        if (getStringFromRegistry64bit(HKEY_LOCAL_MACHINE, javaKey, CUR_VERSION_NAME, value)) {
            if (value >= minJavaVersion) {
                string path;
                if (getStringFromRegistry64bit(HKEY_LOCAL_MACHINE, (string(javaKey) + "\\" + value).c_str(), JAVA_HOME_NAME, path)) {
                    if (*path.rbegin() == '\\') {
                        path.erase(path.length() - 1, 1);
                    }
                    result = checkJava(path.c_str(), prefix);
                }
            }
        }
    } 
    // probably also need to check 32bit registry when launcher becomes 64-bit but is not the case now.   
    return result;    
}
