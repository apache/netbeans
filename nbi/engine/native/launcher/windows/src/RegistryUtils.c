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

#include "RegistryUtils.h"
#include "StringUtils.h"
#include "SystemUtils.h"
#include "FileUtils.h"

WCHAR * getStringValue(HKEY root, WCHAR *key, WCHAR *valueName, BOOL access64key) {
    
    HKEY hkey = 0 ;
    WCHAR *result = NULL;
    DWORD  type  = 0;
    DWORD  size  = 0;
    byte*  value = NULL;
    
    if(RegOpenKeyExW(root, key, 0, KEY_READ | ((access64key && IsWow64) ? KEY_WOW64_64KEY : 0), &hkey) == ERROR_SUCCESS) {
        
        if (RegQueryValueExW(hkey, valueName, NULL, &type, NULL, &size) == ERROR_SUCCESS) {
            
            value = (byte*) LocalAlloc(LPTR,(size + 1) * sizeof(WCHAR));
            ZERO(value, sizeof(WCHAR) * (size + 1));
            if (RegQueryValueExW(hkey, valueName, NULL, &type, value, &size) == ERROR_SUCCESS) {
                if(type == REG_SZ) {
                    result = (WCHAR *)value;
                }
            }
            if(result==NULL) {
                FREE(value);
            }            
        }
    }
    
    if(hkey!=0) {
        RegCloseKey(hkey);
    }
    return result;
}
WCHAR * getStringValuePC(HKEY root, WCHAR *parentkey, WCHAR *childkey, WCHAR *valueName, BOOL access64key) {
    WCHAR * key = appendStringW(appendStringW(appendStringW(NULL, parentkey), L"\\"), childkey);
    WCHAR *value = getStringValue(root, key, valueName, access64key);
    FREE(key);
    return value;
}
