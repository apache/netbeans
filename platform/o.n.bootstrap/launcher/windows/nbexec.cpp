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

#include "platformlauncher.h"
#include "utilsfuncs.h"

PlatformLauncher launcher;

extern "C" BOOL APIENTRY DllMain(HANDLE hModule,
        DWORD ul_reason_for_call,
        LPVOID lpReserved
        ) {
    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
            break;
        case DLL_THREAD_ATTACH:
            break;
        case DLL_THREAD_DETACH:
            break;
        case DLL_PROCESS_DETACH:
            launcher.onExit();
            break;
    }
    return TRUE;
}

volatile int exitStatus = 0;

void exitHook(int status) {
    exitStatus = status;
    logMsg("Exit hook called with status %d", status);
    // do not handle possible restarts, if we are just CLI-connecting to a running process.
    if (status != -252) {
        launcher.onExit();
    }
    logMsg("Exit hook terminated.");
}

#define NBEXEC_EXPORT extern "C" __declspec(dllexport)

NBEXEC_EXPORT int startPlatform(int argc, char *argv[], const char *helpMsg) {
    DWORD retCode = 0;
    launcher.appendToHelp(helpMsg);
    launcher.setSuppressConsole(!isConsoleAttached());
    if (!launcher.start(argv, argc, &retCode)) {
        return -1;
    }
    return retCode;
}


