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
#include <lmcons.h>
#include <shlguid.h>
#include <shlobj.h>

#include "../../.common/src/CommonUtils.h"
#include "WindowsUtils.h"
#include "jni_WindowsNativeUtils.h"

////////////////////////////////////////////////////////////////////////////////
// Functions

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_isCurrentUserAdmin0(JNIEnv* jEnv, jobject jObject) {
    BOOL result = FALSE;
    
    PACL pACL = NULL;
    PSID psidAdmin = NULL;
    HANDLE token = NULL;
    HANDLE duplToken = NULL;
    PSECURITY_DESCRIPTOR adminDescriptor = NULL;
    
    SID_IDENTIFIER_AUTHORITY SystemSidAuthority = SECURITY_NT_AUTHORITY;
    DWORD aclSize;
    
    const DWORD ACCESS_READ  = 1;
    const DWORD ACCESS_WRITE = 2;
    
    GENERIC_MAPPING mapping;
    
    PRIVILEGE_SET ps;
    DWORD status;
    DWORD structSize = sizeof(PRIVILEGE_SET);
    
    // MS KB 118626
    while (TRUE) {
        if (!OpenThreadToken(GetCurrentThread(), TOKEN_DUPLICATE | TOKEN_QUERY, TRUE, &token)) {
            if (GetLastError() != ERROR_NO_TOKEN) {
                throwException(jEnv, "Native error");
                break;
            }
            
            if (!OpenProcessToken(GetCurrentProcess(), TOKEN_DUPLICATE | TOKEN_QUERY, &token)) {
                throwException(jEnv, "Native error");
                break;
            }
        }
        
        if (!DuplicateToken(token, SecurityImpersonation, &duplToken)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        if (!AllocateAndInitializeSid(&SystemSidAuthority, 2, SECURITY_BUILTIN_DOMAIN_RID, DOMAIN_ALIAS_RID_ADMINS, 0, 0, 0, 0, 0, 0, &psidAdmin)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        adminDescriptor = (PSECURITY_DESCRIPTOR) LocalAlloc(LPTR, SECURITY_DESCRIPTOR_MIN_LENGTH);
        if (adminDescriptor == NULL) {
            throwException(jEnv, "Native error");
            break;
        }
        if (!InitializeSecurityDescriptor(adminDescriptor, SECURITY_DESCRIPTOR_REVISION)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        aclSize = sizeof(ACL) + sizeof(ACCESS_ALLOWED_ACE) + GetLengthSid(psidAdmin) - sizeof(DWORD);
        
        pACL = (PACL) LocalAlloc(LPTR, aclSize);
        if (pACL == NULL) {
            throwException(jEnv, "Native error");
            break;
        }
        if (!InitializeAcl(pACL, aclSize, ACL_REVISION2)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        if (!AddAccessAllowedAce(pACL, ACL_REVISION2, ACCESS_READ | ACCESS_WRITE , psidAdmin)) {
            throwException(jEnv, "Native error");
            break;
        }
        if (!SetSecurityDescriptorDacl(adminDescriptor, TRUE, pACL, FALSE)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        SetSecurityDescriptorGroup(adminDescriptor, psidAdmin, FALSE);
        SetSecurityDescriptorOwner(adminDescriptor, psidAdmin, FALSE);
        
        if (!IsValidSecurityDescriptor(adminDescriptor)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        mapping.GenericRead    = ACCESS_READ;
        mapping.GenericWrite   = ACCESS_WRITE;
        mapping.GenericExecute = 0;
        mapping.GenericAll     = ACCESS_READ | ACCESS_WRITE;
        
        if (!AccessCheck(adminDescriptor, duplToken, ACCESS_READ, &mapping, &ps, &structSize, &status, &result)) {
            throwException(jEnv, "Native error");
            break;
        }
        
        break;
    }
    
    if (pACL) {
        LocalFree(pACL);
    }
    if (adminDescriptor) {
        LocalFree(adminDescriptor);
    }
    if (psidAdmin) {
        FreeSid(psidAdmin);
    }
    if (duplToken) {
        CloseHandle(duplToken);
    }
    if (token) {
        CloseHandle(token);
    }
    
    return result;
}

JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_getFreeSpace0(JNIEnv* jEnv, jobject jObject, jstring jPath) {
    WCHAR*  path = getWideChars(jEnv, jPath);
    jlong  size = 0;
    typedef struct int64s { unsigned long Low, High; } int64t;
    int64t bytes;
    if (GetDiskFreeSpaceExW(path, (PULARGE_INTEGER) &bytes, NULL, NULL)) {
        unsigned long h = bytes.High;
        // workaround of using missing _allmul function
        // replace multiplication by sum
        // (2^32 * high + low) = 2^32 + 2^32 + ... (bytes.High times total) + bytes.Low     
        // can be relatively expensive on big sizes (peta bytes and more)
        while(h > 0) {
            h--;
            size+=4294967296L;
        }
        size+= bytes.Low;
    } else {
        throwException(jEnv, "Native error");
    }
    
    FREE(path);
    
    return size;
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createShortcut0(JNIEnv* jEnv, jobject jObject, jobject jShortcut) {
    unsigned short *shortcutPath     = getWideStringFromMethod(jEnv, jShortcut, "getPath");
    unsigned short *targetPath       = getWideStringFromMethod(jEnv, jShortcut, "getTargetPath");
    unsigned short *description      = getWideStringFromMethod(jEnv, jShortcut, "getDescription");
    unsigned short *iconPath         = getWideStringFromMethod(jEnv, jShortcut, "getIconPath");
    jint            iconIndex        = getIntFromMethod       (jEnv, jShortcut, "getIconIndex");
    unsigned short *workingDirectory = getWideStringFromMethod(jEnv, jShortcut, "getWorkingDirectoryPath");
    unsigned short *arguments        = getWideStringFromMethod(jEnv, jShortcut, "getArgumentsString");
    
    HRESULT     tempResult;
    IShellLinkW* shell;
    
    HRESULT comStart = CoInitialize(NULL);
    int errorCode = 0;
    
    tempResult = CoCreateInstance(&CLSID_ShellLink, NULL, CLSCTX_INPROC_SERVER, &IID_IShellLinkW, (void **) &shell);
    
    if (SUCCEEDED(tempResult)) {
        IPersistFile *persistFile;
        // we will save the shell link in persistent storage
        tempResult = shell->lpVtbl->QueryInterface(shell, &IID_IPersistFile, (void **) &persistFile);
        
        if (SUCCEEDED(tempResult)) {
            tempResult = shell->lpVtbl->SetPath(shell, targetPath);
            if (!SUCCEEDED(tempResult)) {
                throwException(jEnv, "Native error (-2)");
                errorCode = -2;
            }
            // make sure description length is less than MAX_PATH
            if ((errorCode == 0) && (description != NULL)) {
                if (WCSLEN(description) < MAX_PATH) {
                    if (!SUCCEEDED(shell->lpVtbl->SetDescription(shell, description))) {
                        throwException(jEnv, "Native error (-3)");
                        errorCode = -3;
                    }
                } else {
                    unsigned short *desc = (unsigned short *) MALLOC(sizeof(unsigned short) * MAX_PATH);
                    ZERO(desc, sizeof(unsigned short) * MAX_PATH);
                    desc = WCSNCPY(desc, description, MAX_PATH);
                    if (!SUCCEEDED(shell->lpVtbl->SetDescription(shell, desc))) {
                        throwException(jEnv, "Native error (-4)");
                        errorCode = -4;
                    }
                    FREE(desc);
                }
            }
            if ((errorCode == 0) && (arguments != NULL)) {
                if (!SUCCEEDED(shell->lpVtbl->SetArguments(shell, arguments))) {
                    throwException(jEnv, "Native error (-5)");
                    errorCode = -5;
                }
            }
            if ((errorCode == 0) && (workingDirectory != NULL)) {
                if (!SUCCEEDED(shell->lpVtbl->SetWorkingDirectory(shell, workingDirectory))) {
                    throwException(jEnv, "Native error (-6)");
                    errorCode = -6;
                }
            }
            if ((errorCode == 0) && (iconPath != NULL)) {
                if (!SUCCEEDED(shell->lpVtbl->SetIconLocation(shell, iconPath, iconIndex))) {
                    throwException(jEnv, "Native error (-7)");
                    errorCode = -7;
                }
            }
            // use normal window.
            if (errorCode == 0) {
                if (!SUCCEEDED(shell->lpVtbl->SetShowCmd(shell, SW_NORMAL))) {
                    throwException(jEnv, "Native error (-8)");
                    errorCode = -8;
                }
            }
            if (errorCode == 0) {
                if (mkdirsW(jEnv, shortcutPath)) {
                    if (!SUCCEEDED(persistFile->lpVtbl->Save(persistFile, shortcutPath, TRUE))) {
                        throwException(jEnv, "Native error (-9)");
                        errorCode = -9;
                    }
                } else {
                    throwException(jEnv, "Native error (-10)");
                    errorCode = -10;
                }
            }
            
            if (errorCode == 0) {
                persistFile->lpVtbl->Release(persistFile);
            }
        } else {
            throwException(jEnv, "Native error (-11)");
            errorCode = -11;
        }
        shell->lpVtbl->Release(shell);
    } else {
        throwException(jEnv, "Native error (-12)");
        errorCode = -12;
    }
    
    if (comStart == S_OK) {
        CoUninitialize();
    }
    
    FREE(shortcutPath);
    FREE(targetPath);
    FREE(description);
    FREE(iconPath);
    FREE(workingDirectory);
    FREE(arguments);
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_deleteFileOnReboot0(JNIEnv* jEnv, jobject jObject, jstring jPath) {
    unsigned short * path = getWideChars(jEnv, jPath);
    
    if (!MoveFileExW(path, NULL, MOVEFILE_DELAY_UNTIL_REBOOT)) {
        throwException(jEnv, "Native error");
    }
    
    FREE(path);
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createProcessWithoutHandles0(JNIEnv* jEnv, jobject jObject, jstring jCommand) {
    unsigned short * command = getWideChars(jEnv, jCommand);
    STARTUPINFOW si;
    PROCESS_INFORMATION pi;
    
    ZERO(&si, sizeof(si));
    si.cb = sizeof(si);
    ZERO(&pi, sizeof(pi));
    if(!CreateProcessW(NULL,   // executable name - use command line
            command,    // command line
            NULL,   // process security attribute
            NULL,   // thread security attribute
            FALSE,   // inherits system handles
            0,      // no creation flags
            NULL,   // use parent's environment block
            NULL,   // use parent's starting directory
            &si,    // (in) startup information
            &pi)) {   // (out) process information
        throwException(jEnv, "Cannot create process.\n");
    }
    
    
    FREE(command);
}
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyAssociationChanged0(JNIEnv *jEnv, jobject jObj) {
    SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, 0, 0);
}

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyEnvironmentChanged0(JNIEnv *jEnv, jobject jObj) {
    /* maximum 1 sec timeout for each window in the system */
    DWORD dwReturnValue = 0;
    LRESULT result = SendMessageTimeout(HWND_BROADCAST, WM_SETTINGCHANGE, 0, (LPARAM) "Environment", SMTO_ABORTIFHUNG, 1000, (PDWORD_PTR) &dwReturnValue);
    return (result!=0);
}


JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_checkAccessTokenAccessLevel0(JNIEnv *jEnv, jobject jObj, jstring jPath, jint jLevel) {
    unsigned short * path = getWideChars(jEnv, jPath);
    PSECURITY_DESCRIPTOR    pSD;
    DWORD nLength;
    
    PRIVILEGE_SET PrivilegeSet;
    DWORD PrivSetSize = sizeof (PRIVILEGE_SET);
    
    HANDLE hToken;
    
    GENERIC_MAPPING GenericMapping;
    DWORD DesiredAccess = (DWORD) jLevel ;
    
    BOOL bAccessGranted;
    DWORD GrantedAccess;
    
    // create memory for storing user's security descriptor
    GetFileSecurityW(path, OWNER_SECURITY_INFORMATION | GROUP_SECURITY_INFORMATION | DACL_SECURITY_INFORMATION, NULL, 0, &nLength);
    
    pSD = (PSECURITY_DESCRIPTOR) HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, nLength);
    
    if (pSD == NULL) {
        throwException(jEnv, "Unable to allocate memory to store security descriptor.\n");
        return -1;
    }
    // Get the security descriptor
    if (!GetFileSecurityW(path, OWNER_SECURITY_INFORMATION | GROUP_SECURITY_INFORMATION | DACL_SECURITY_INFORMATION,  pSD, nLength, &nLength)) {
        throwException(jEnv, "Unable to obtain security descriptor.\n");
        FREE(path);
        return (-3);
    }
    FREE(path);
    
    /* Perform security impersonation of the user and open */
    /* the resulting thread token. */
    if (!ImpersonateSelf(SecurityImpersonation)) {
        throwException(jEnv, "Unable to perform security impersonation.\n");
        HeapFree(GetProcessHeap(), 0, pSD);
        return (-4);
    }
    
    if (!OpenThreadToken(GetCurrentThread(), TOKEN_DUPLICATE | TOKEN_QUERY, FALSE, &hToken)) {
        throwException(jEnv, "Unable to get current thread's token.\n");
        HeapFree(GetProcessHeap(), 0, pSD);
        return (-5);
    }
    RevertToSelf();
    
    ZERO(&GenericMapping, sizeof (GENERIC_MAPPING));
    
    DesiredAccess = DesiredAccess | STANDARD_RIGHTS_READ;
    GenericMapping.GenericRead = FILE_GENERIC_READ;
    
    if(jLevel & FILE_WRITE_DATA) {
        GenericMapping.GenericWrite = FILE_GENERIC_WRITE;
    }
    
    MapGenericMask(&DesiredAccess, &GenericMapping);
    
    /* Perform access check using the token. */
    if (!AccessCheck(pSD, hToken, DesiredAccess, &GenericMapping, &PrivilegeSet, &PrivSetSize, &GrantedAccess, &bAccessGranted)) {
        throwException(jEnv, "Unable to perform access check.\n");
        CloseHandle(hToken);
        HeapFree(GetProcessHeap(), 0, pSD);
        return (-6);
    }
    /* Clean up. */
    HeapFree(GetProcessHeap(), 0, pSD);
    CloseHandle(hToken);
    return (bAccessGranted);
}


