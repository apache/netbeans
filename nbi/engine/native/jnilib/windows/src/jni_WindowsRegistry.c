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

////////////////////////////////////////////////////////////////////////////////
// Globals

// maximum length of a registry value
const DWORD MAX_LEN_VALUE_NAME = 16383;

////////////////////////////////////////////////////////////////////////////////
// Functions

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_checkKeyAccess0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jint jLevel) {
    HKEY  hkey = 0;
    unsigned short * key = getWideChars(jEnv, jKey);
    
    jboolean result = FALSE;
    REGSAM access = (jLevel==0) ? KEY_READ : KEY_ALL_ACCESS;
    result = (RegOpenKeyExW(getHKEY(jSection), key, 0, access | getMode(jMode), &hkey) == ERROR_SUCCESS);
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    
    return result;
}

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_valueExists0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName) {
    HKEY  hkey  = 0;
    unsigned short * key   = getWideChars(jEnv, jKey);
    unsigned short * value = getWideChars(jEnv, jName);
    jboolean result = FALSE;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_QUERY_VALUE | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        result = (RegQueryValueExW(hkey, value, NULL, NULL, NULL, NULL) == ERROR_SUCCESS);
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    FREE(key);
    FREE(value);
    
    return result;
}

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_keyEmpty0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey) {
    HKEY  hkey = 0;
    unsigned short* key = getWideChars(jEnv, jKey);
    
    DWORD subkeys = 1;
    DWORD values  = 1;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_READ | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        if (RegQueryInfoKeyW(hkey, NULL, NULL, NULL, &subkeys, NULL, NULL, &values, NULL, NULL, NULL, NULL) != ERROR_SUCCESS) {
            throwException(jEnv, "Cannot read key data");
        }
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    
    return (values + subkeys == 0);
}

JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_countSubKeys0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey) {
    HKEY  hkey = 0;
    unsigned short* key = getWideChars(jEnv, jKey);
    
    DWORD count = 0;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_READ | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        if(RegQueryInfoKeyW(hkey, NULL, NULL, NULL, &count, NULL, NULL, NULL, NULL, NULL, NULL, NULL) != ERROR_SUCCESS) {
            throwException(jEnv, "Cannot read key data");
        }
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    
    return count;
}

JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_countValues0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey) {
    HKEY  hkey = 0;
    unsigned short* key = getWideChars(jEnv, jKey);
    
    DWORD count = 0;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_READ | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        if (RegQueryInfoKeyW(hkey, NULL, NULL, NULL, NULL, NULL, NULL, &count, NULL, NULL, NULL, NULL) != ERROR_SUCCESS) {
            throwException(jEnv, "Cannot read key data");
        }
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    
    return count;
}

JNIEXPORT jobjectArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getSubkeyNames0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey) {
    HKEY  hkey   = 0;
    unsigned short* key    = getWideChars(jEnv, jKey);
    DWORD number = 0;
    int   err    = 0;
    int   index  = 0 ;
    
    unsigned short* buffer = (unsigned short*) MALLOC(sizeof(char) * MAX_LEN_VALUE_NAME);
    
    jobjectArray result = NULL;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_READ | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        if (RegQueryInfoKeyW(hkey, NULL, NULL, NULL, &number, NULL, NULL, NULL, NULL, NULL, NULL, NULL) == ERROR_SUCCESS) {
            jclass stringClazz = (*jEnv)->FindClass(jEnv, "java/lang/String");
            result = (*jEnv)->NewObjectArray(jEnv, number, stringClazz, NULL);
            
            do {
                DWORD size = MAX_LEN_VALUE_NAME;
                buffer[0]  = 0;
                
                err = RegEnumKeyExW(hkey, index, buffer, &size, NULL, NULL, NULL, NULL);
                if (err == ERROR_SUCCESS) {
                    (*jEnv)->SetObjectArrayElement(jEnv, result, index, getStringW(jEnv, buffer));
                } else {
                    if (err != ERROR_NO_MORE_ITEMS) {
                        throwException(jEnv, "Cannot get key names");
                    }
                }
                
                index++;
            } while (err == ERROR_SUCCESS);
        } else {
            throwException(jEnv, "Cannot read key data");
        }
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    FREE(buffer);
    
    return result;
}

JNIEXPORT jobjectArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getValueNames0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey) {
    HKEY  hkey         = 0;
    unsigned short* key          = getWideChars(jEnv, jKey);
    DWORD valuesCount  = 0;
    int   err          = 0;
    int   index        = 0;
    
    unsigned short* buffer = (unsigned short*) MALLOC(sizeof(char) * MAX_LEN_VALUE_NAME);
    
    jobjectArray result = NULL;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_QUERY_VALUE | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        if (RegQueryInfoKeyW(hkey, NULL, NULL, NULL, NULL, NULL, NULL, &valuesCount, NULL, NULL, NULL, NULL) == ERROR_SUCCESS) {
            jclass stringClazz = (*jEnv)->FindClass(jEnv, "java/lang/String");
            result = (*jEnv)->NewObjectArray(jEnv, valuesCount, stringClazz, NULL);
            
            do {
                DWORD size = MAX_LEN_VALUE_NAME;
                buffer[0]  = 0;
                
                err = RegEnumValueW(hkey, index, buffer, &size, NULL, NULL, NULL, NULL);
                if (err == ERROR_SUCCESS) {
                    (*jEnv)->SetObjectArrayElement(jEnv, result, index, getStringW(jEnv, buffer));
                } else {
                    if (err != ERROR_NO_MORE_ITEMS) {
                        throwException(jEnv, "Cannot get value names");
                    }
                }
                
                index++;
            } while (err == ERROR_SUCCESS);
        } else {
            throwException(jEnv, "Cannot read key data");
        }
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    FREE(buffer);
    
    return result;
}

JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getValueType0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName) {
    HKEY  hkey   =0;
    unsigned short* key   = getWideChars(jEnv, jKey);
    unsigned short* value = getWideChars(jEnv, jName);
    
    DWORD type = REG_NONE;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_QUERY_VALUE | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        if (RegQueryValueExW(hkey, value, NULL, &type, NULL, NULL) != ERROR_SUCCESS) {
            throwException(jEnv, "Cannot read value");
        }
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    FREE(value);
    
    return type;
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_createKey0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jParent, jstring jChild) {
    HKEY  hkey   = 0;
    HKEY  newKey = 0;
    unsigned short* parent = getWideChars(jEnv, jParent);
    unsigned short* child  = getWideChars(jEnv, jChild);
    
    if (RegOpenKeyExW(getHKEY(jSection), parent, 0, KEY_CREATE_SUB_KEY | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        LONG result = RegCreateKeyExW(hkey, child, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_READ | KEY_WRITE | getMode(jMode), NULL, &newKey, NULL);
        if (result == ERROR_SUCCESS) {
            if (newKey != 0) {
                RegCloseKey(newKey);
            }
        } else if (result == ERROR_ACCESS_DENIED) {
            throwException(jEnv, "Could not create a new key (access denied)");
        } else {
            throwException(jEnv, "Could not create a new key");
        }
    } else {
        throwException(jEnv, "Could not open the parent key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(parent);
    FREE(child);
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_deleteKey0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jParent, jstring jChild) {
    HKEY  hkey       = 0;
    unsigned short* jParentS = getWideChars(jEnv, jParent);
    unsigned short* jChildS  = getWideChars(jEnv, jChild);
    
    
    if (RegOpenKeyExW(getHKEY(jSection), jParentS, 0, KEY_READ | KEY_WRITE | getMode(jMode), &hkey) == ERROR_SUCCESS) {
	LONG mode = getMode(jMode);
	if(mode==0) {
            if (RegDeleteKeyW(hkey, jChildS) != ERROR_SUCCESS) {
                throwException(jEnv, "Could not delete key");
	    }
        } else {
             typedef LONG (WINAPI *LPFN_REGDELETEKEYEXW) (HKEY, LPCWSTR, REGSAM, DWORD);
             LPFN_REGDELETEKEYEXW regDeleteKeyExW;
             HMODULE hModule = GetModuleHandle(TEXT("advapi32.dll"));
             if(hModule == NULL) {
                 hModule = LoadLibraryA("advapi32.dll");
             }
             regDeleteKeyExW = (LPFN_REGDELETEKEYEXW) GetProcAddress(hModule,"RegDeleteKeyExW");
             if (NULL != regDeleteKeyExW) {
                 if (regDeleteKeyExW(hkey, jChildS, mode, 0) != ERROR_SUCCESS) {
     		    throwException(jEnv, "Could not delete key");
                 }
             } else {
                    throwException(jEnv, "Cannot load function RegDeleteKeyExW");
             }
        }
    } else {
        throwException(jEnv, "Could not open the parent key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(jParentS);
    FREE(jChildS);
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_deleteValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName) {
    HKEY  hkey   = 0;
    unsigned short* key   = getWideChars(jEnv, jKey);
    unsigned short* value = getWideChars(jEnv, jName);
    
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_SET_VALUE | getMode(jMode) , &hkey) == ERROR_SUCCESS) {
        if (RegDeleteValueW(hkey, value) != ERROR_SUCCESS) {
            throwException(jEnv, "Cannot delete value");
        }
    } else {
        throwException(jEnv, "Canont open key");
    }
    
    if(hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    FREE(value);
}

JNIEXPORT jstring JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getStringValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName, jboolean jExpand) {
    unsigned short*   key    = getWideChars(jEnv, jKey);
    unsigned short*   name   = getWideChars(jEnv, jName);
    DWORD   type   = REG_NONE;
    byte*   value  = NULL;
    
    jstring result = NULL;
    
    if (queryValue(getMode(jMode), getHKEY(jSection), key, name, &type, NULL, &value, jExpand)) {
        if (type == REG_SZ || type == REG_EXPAND_SZ) {
            result = getStringW(jEnv, (unsigned short*) value);
        } else {
            throwException(jEnv, "Value has wrong type");
        }
    } else {
        throwException(jEnv, "Cannot read value");
    }
    
    FREE(key);
    FREE(name);
    FREE(value);
    
    return result;
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setStringValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName, jstring jValue, jboolean jExpand) {
    unsigned short* key   = getWideChars(jEnv, jKey);
    unsigned short* name  = getWideChars(jEnv, jName);
    unsigned short* value = getWideChars(jEnv, jValue);
    
    if (!setValue(getMode(jMode), getHKEY(jSection), key, name, jExpand ? REG_EXPAND_SZ : REG_SZ, (byte*) value, ((int) WCSLEN(value)) * sizeof(unsigned short), 0)) {
        throwException(jEnv, "Could not set value");
    }
    
    FREE(key);
    FREE(name);
    FREE(value);
}

JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_get32BitValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName) {
    HKEY  hkey   = 0;
    unsigned short* key   = getWideChars(jEnv, jKey);
    unsigned short* value = getWideChars(jEnv, jName);
    
    jint result = -1;
    if(RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_QUERY_VALUE | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        DWORD dwType    = 0;
        DWORD dwValue   = 0;
        DWORD dwBufSize = sizeof(dwValue);
        
        if (RegQueryValueExW(hkey, value, NULL, &dwType, (LPBYTE) &dwValue, &dwBufSize) == ERROR_SUCCESS) {
            if ((dwType == REG_DWORD) || (dwType == REG_DWORD_BIG_ENDIAN)) {
                result = dwValue;
            } else {
                throwException(jEnv, "Value is of wrong type");
            }
        } else {
            throwException(jEnv, "Cannot read key data");
        }
    } else {
        throwException(jEnv, "Could not open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    FREE(value);
    
    return result;
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_set32BitValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName, jint jValue) {
    unsigned short*  key       = getWideChars(jEnv, jKey);
    unsigned short*  name      = getWideChars(jEnv, jName);
    DWORD  dword     = (DWORD) jValue;
    LPBYTE byteValue = (LPBYTE) &dword;
    
    if (!setValue(getMode(jMode),getHKEY(jSection), key, name, REG_DWORD, byteValue, sizeof(dword), 0)) {
        throwException(jEnv, "Cannot set value");
    }
    
    FREE(key);
    FREE(name);
}

JNIEXPORT jobjectArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getMultiStringValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName) {
    HKEY  hkey   = 0;
    unsigned short* key   = getWideChars(jEnv, jKey);
    unsigned short* value = getWideChars(jEnv, jName);
    
    int	i, start, count, cnt;
    LONG regErr = 0;
    unsigned short* data = 0;
    jstring string;
    jclass strClass;
    DWORD dwType;
    DWORD size = 0;
    
    jarray result = 0;
    if(RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_QUERY_VALUE | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        if (RegQueryValueExW(hkey, value, NULL, &dwType, NULL, &size) == ERROR_SUCCESS) {
            if (dwType == REG_MULTI_SZ) {
                data = (unsigned short*) LocalAlloc(LPTR, size + 8);
                
                if (data != NULL) {
                    if (RegQueryValueExW(hkey, value, NULL, &dwType, (byte*) data, &size) == ERROR_SUCCESS) {
                        for (count = 0, i = 0; i < (int) size; i++) {
                            if (data[i] == '\0') { // \0 - end of a single string
                                count++;
                                if (data[i + 1] == '\0') { // two \0 's in a row - end of all strings
                                    break;
                                }
                            }
                        }
                        
                        strClass = (*jEnv)->FindClass(jEnv, "java/lang/String");
                        if (strClass != NULL) {
                            result = (*jEnv)->NewObjectArray(jEnv, (jsize) count, strClass, NULL);
                            if (result != NULL) {
                                for (cnt = 0, start = 0, i = 0; (i < (int) size) && (cnt < count); i++) {
                                    if (data[i] == '\0') {
                                        string = getStringWithLengthW(jEnv, &data[start], i - start);
                                        
                                        if (string != NULL) {
                                            (*jEnv)->SetObjectArrayElement(jEnv, (jobjectArray) result, (jsize) cnt++, string);
                                            
                                            start = i + 1;
                                            if (data[start] == '\0') {
                                                break;
                                            }
                                        } else {
                                            throwException(jEnv, "Cannot create an array element");
                                            break;
                                        }
                                    }
                                }
                            } else {
                                throwException(jEnv, "Cannot create resulting array");
                            }
                        } else {
                            throwException(jEnv, "Cannot find java.lang.String");
                        }
                    } else {
                        throwException(jEnv, "Cannot read value data");
                    }
                    FREE(data);
                } else {
                    throwException(jEnv, "Cannot allocate memory for value");
                }
            } else {
                throwException(jEnv, "Value is of wrong type");
            }
        } else {
            throwException(jEnv, "Cannot read key data");
        }
    } else {
        throwException(jEnv, "Cannot open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    FREE(value);
    
    return (jobjectArray) result;
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setMultiStringValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName, jobjectArray jValue) {
    unsigned short* key    = getWideChars(jEnv, jKey);
    unsigned short* name   = getWideChars(jEnv, jName);
    DWORD size   = 0;
    BYTE* data   = getByteFromMultiString(jEnv, jValue, &size);
    
    if (!setValue(getMode(jMode),getHKEY(jSection), key, name, REG_MULTI_SZ, data, size, 0)) {
        throwException(jEnv, "Cannot set value");
    }
    
    FREE(key);
    FREE(name);
    FREE(data);
}

JNIEXPORT jbyteArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getBinaryValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName) {
    HKEY  hkey   = 0;
    unsigned short* key   = getWideChars(jEnv, jKey);
    unsigned short* value = getWideChars(jEnv, jName);
    
    jbyteArray result = NULL;
    if (RegOpenKeyExW(getHKEY(jSection), key, 0, KEY_QUERY_VALUE | getMode(jMode), &hkey) == ERROR_SUCCESS) {
        DWORD dwType  = 0;
        DWORD dwValue = 0;
        DWORD size    = 0;
        BYTE* data    = NULL;
        
        if (RegQueryValueExW(hkey, value, NULL, &dwType, NULL, &size) == ERROR_SUCCESS) {
            if (dwType == REG_BINARY || dwType == REG_NONE) {
                data = (BYTE*) LocalAlloc(LPTR, size + 8);
                if (RegQueryValueExW(hkey, value, NULL, &dwType, (BYTE*) data, &size) == ERROR_SUCCESS) {
                    if (data != NULL) {
                        result = (*jEnv)->NewByteArray(jEnv, (jsize) size);
                        (*jEnv)->SetByteArrayRegion(jEnv, result, 0, (jsize) size, (jbyte*) data);
                    }
                } else {
                    throwException(jEnv, "Could not read key data");
                }
                FREE(data);
            } else {
                throwException(jEnv, "Value is of wrong type");
            }
        } else {
            throwException(jEnv, "Could not read key data");
        }
    } else {
        throwException(jEnv, "Could not open key");
    }
    
    if (hkey != 0) {
        RegCloseKey(hkey);
    }
    
    FREE(key);
    FREE(value);
    
    return result;
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setBinaryValue0(JNIEnv *jEnv, jobject jObject, jint jMode, jint jSection, jstring jKey, jstring jName, jbyteArray jValue) {
    unsigned short*  key     = getWideChars(jEnv, jKey);
    unsigned short*  name    = getWideChars(jEnv, jName);
    BYTE*  data    = (BYTE*) (*jEnv)->GetByteArrayElements(jEnv, jValue, 0);
    DWORD  length  = (*jEnv)->GetArrayLength(jEnv, jValue);
    
    if (!setValue(getMode(jMode),getHKEY(jSection), key, name, REG_BINARY, data, length, 0)) {
        throwException(jEnv, "Cannot set value");
    }
    
    FREE(key);
    FREE(name);
    if (data != NULL) {
        (*jEnv)->ReleaseByteArrayElements(jEnv, jValue, (jbyte*) data, JNI_ABORT);
    }
}

JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setNoneValue0(JNIEnv *jEnv, jobject jobj, jint jMode, jint jSection, jstring jKey, jstring jName, jbyteArray jValue) {
    unsigned short*  key     = getWideChars(jEnv, jKey);
    unsigned short*  name    = getWideChars(jEnv, jName);
    BYTE*  data    = (BYTE*) (*jEnv)->GetByteArrayElements(jEnv, jValue, 0);
    DWORD  length  = (*jEnv)->GetArrayLength(jEnv, jValue);
    
    if (!setValue(getMode(jMode),getHKEY(jSection), key, name, REG_NONE, data, length, 0)) {
        throwException(jEnv, "Cannot set value");
    }
    
    FREE(key);
    FREE(name);
    if (data != NULL) {
        (*jEnv)->ReleaseByteArrayElements(jEnv, jValue, (jbyte*) data, JNI_ABORT);
    }
}

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_IsWow64Process0(JNIEnv *jEnv, jobject jObject) {
    typedef BOOL (WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);
    LPFN_ISWOW64PROCESS fnIsWow64Process;
    BOOL IsWow64 = FALSE;

    fnIsWow64Process = (LPFN_ISWOW64PROCESS)GetProcAddress(GetModuleHandle(TEXT("kernel32")),"IsWow64Process");
  
    if (NULL != fnIsWow64Process)
    {
        if (!fnIsWow64Process(GetCurrentProcess(),&IsWow64))
        {
        }
    }
    return IsWow64;
}
