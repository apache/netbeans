/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
