/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>

#include "SystemUtils.h"

BOOL IsWow64 = FALSE;

void getOSVersion(DWORD *id, DWORD *major, DWORD *minor, DWORD *productType) {
    OSVERSIONINFOEX ver;
    ver.dwOSVersionInfoSize = sizeof(ver);
    GetVersionEx((OSVERSIONINFO *) &ver);
    *id = ver.dwPlatformId;
    *major = ver.dwMajorVersion;
    *minor =  ver.dwMinorVersion;
    *productType = ver.wProductType;
    return;
}

DWORD is9x() {
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_WINDOWS) ? 1 : 0;
}
DWORD isNT() {    
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_NT && major == 4 && minor == 0) ? 1 : 0;
}
DWORD is2k() {
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_NT && major == 5 && minor == 0) ? 1 : 0;
}

DWORD isXP() {
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_NT && major == 5 && minor == 1) ? 1 : 0;
}

DWORD is2003() {
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_NT && major == 5 && minor == 2) ? 1 : 0;
}
DWORD isVista() {
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_NT && major == 6 && minor == 0 && type == VER_NT_WORKSTATION) ? 1 : 0;
}

DWORD is2008() {
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_NT && major == 6 && (minor == 0 || minor == 1) && type != VER_NT_WORKSTATION) ? 1 : 0;
}

DWORD is7() {
    DWORD id, major, minor, type;
    getOSVersion(& id, & major, & minor, & type);
    return (id == VER_PLATFORM_WIN32_NT && major == 6 && minor == 1 && type == VER_NT_WORKSTATION) ? 1 : 0;
}

typedef BOOL (WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);
LPFN_ISWOW64PROCESS fnIsWow64Process;

void initWow64()
{
    IsWow64 = FALSE;

    fnIsWow64Process = (LPFN_ISWOW64PROCESS)GetProcAddress(GetModuleHandle(TEXT("kernel32")),"IsWow64Process");
  
    if (NULL != fnIsWow64Process)
    {
        if (!fnIsWow64Process(GetCurrentProcess(),&IsWow64))
        {
            // handle error
        }
    }
}
