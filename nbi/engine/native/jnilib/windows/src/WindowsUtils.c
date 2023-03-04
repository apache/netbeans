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

#include <jni.h>
#include <windows.h>
#include <winreg.h>
#include <winnt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>

#include "../../.common/src/CommonUtils.h"
#include "WindowsUtils.h"
#include "jni_WindowsRegistry.h"

HKEY getHKEY(jint jSection) {
    switch (jSection) {
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CLASSES_ROOT:
            return HKEY_CLASSES_ROOT;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_USER:
            return HKEY_CURRENT_USER;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_LOCAL_MACHINE:
            return HKEY_LOCAL_MACHINE;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_USERS:
            return HKEY_USERS;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_CONFIG:
            return HKEY_CURRENT_CONFIG;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_DYN_DATA:
            return HKEY_DYN_DATA;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_DATA:
            return HKEY_PERFORMANCE_DATA;
        default:
            return NULL;
    }
}

LONG getMode(jint jmode) {
    switch (jmode) {
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_DEFAULT:
            return 0L;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_32BIT:
            return KEY_WOW64_32KEY;
        case org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_64BIT:
            return KEY_WOW64_64KEY;
        default:
            return 0L;
    }
}


int queryValue(int mode, HKEY section, const unsigned short* key, const unsigned short* name, DWORD* type, DWORD* size, byte** value, int expand) {
    int result = 1;
    
    HKEY hkey = 0;
    int tempType = 0;
    int tempSize = 0;
    byte* tempValue = NULL;
    
    if (RegOpenKeyExW(section, key, 0, KEY_QUERY_VALUE | mode, &hkey) == ERROR_SUCCESS) {
        if (RegQueryValueExW(hkey, name, NULL, (LPDWORD) &tempType, NULL, (LPDWORD) &tempSize) == ERROR_SUCCESS) {
            tempValue = (byte*) MALLOC(tempSize + 8);
            
            if (tempValue != NULL) {
                ZERO(tempValue, tempSize + 8);
                
                if (RegQueryValueExW(hkey, name, NULL, (LPDWORD) &tempType, tempValue, (LPDWORD) &tempSize) == ERROR_SUCCESS) {
                    if (expand && (tempType == REG_EXPAND_SZ)) {
                        int expandedSize = (int) WCSLEN((unsigned short*) tempValue) + 2;
                        byte* expandedValue = (byte*) MALLOC(expandedSize * sizeof(wchar_t));
                        int expandedCharsNumber = 0;
                        ZERO(expandedValue, expandedSize);
                        expandedCharsNumber = ExpandEnvironmentStringsW((unsigned short*) tempValue, (unsigned short*) expandedValue, tempSize);
                        if (expandedCharsNumber > tempSize) {
                            FREE(expandedValue);
                            expandedValue       = (byte*) MALLOC(expandedCharsNumber * sizeof(wchar_t));
                            expandedCharsNumber = ExpandEnvironmentStringsW((unsigned short*) tempValue, (unsigned short*) expandedValue, expandedCharsNumber);
                        }
                        
                        FREE(tempValue);
                        tempValue = expandedValue;
                    }
                } else {
                    FREE(tempValue);
                    result = 0;
                }
            }
        } else {
            result = 0;
        }
    } else {
        result = 0;
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    if (type != NULL) {
        *type = tempType;
    }
    if (size != NULL) {
        *size = tempSize;
    }
    if ((value != NULL) && (tempValue != NULL)) {
        *value = tempValue;
    }
    
    return result;
}

int setValue(int mode, HKEY section, const unsigned short* key, const unsigned short* name, DWORD type, const byte* data, int size, int expand) {
    int result = 1;
    
    HKEY hkey  = 0;
    
    if (RegOpenKeyExW(section, key, 0, KEY_SET_VALUE | mode, &hkey) == ERROR_SUCCESS) {
        if (!(RegSetValueExW(hkey, name, 0, type, data, size) == ERROR_SUCCESS)) {
            result = 0;
        }
    } else {
        result = 0;
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    return result;
}
