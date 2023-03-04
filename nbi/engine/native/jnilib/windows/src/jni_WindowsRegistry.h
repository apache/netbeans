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
/* Header for class org_netbeans_installer_utils_system_windows_WindowsRegistry */

#ifndef _Included_org_netbeans_installer_utils_system_windows_WindowsRegistry
#define _Included_org_netbeans_installer_utils_system_windows_WindowsRegistry
#ifdef __cplusplus
extern "C" {
#endif
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CLASSES_ROOT
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CLASSES_ROOT 0L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_USER
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_USER 1L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_LOCAL_MACHINE
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_LOCAL_MACHINE 2L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_USERS
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_USERS 3L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_CONFIG
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_CURRENT_CONFIG 4L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_DYN_DATA
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_DYN_DATA 5L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_DATA
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_DATA 6L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_NLSTEXT
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_NLSTEXT 7L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_TEXT
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKEY_PERFORMANCE_TEXT 8L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKCR
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKCR 0L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKCU
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKCU 1L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_HKLM
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_HKLM 2L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_NONE
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_NONE 0L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_SZ
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_SZ 1L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_EXPAND_SZ
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_EXPAND_SZ 2L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_BINARY
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_BINARY 3L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_DWORD_LITTLE_ENDIAN
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_DWORD_LITTLE_ENDIAN 4L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_DWORD
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_DWORD 4L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_DWORD_BIG_ENDIAN
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_DWORD_BIG_ENDIAN 5L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_LINK
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_LINK 6L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_MULTI_SZ
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_MULTI_SZ 7L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_RESOURCE_LIST
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_RESOURCE_LIST 8L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_FULL_RESOURCE_DESCRIPTOR
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_FULL_RESOURCE_DESCRIPTOR 9L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_RESOURCE_REQUIREMENTS_LIST
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_RESOURCE_REQUIREMENTS_LIST 10L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_QWORD_LITTLE_ENDIAN
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_QWORD_LITTLE_ENDIAN 11L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_QWORD
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_REG_QWORD 11L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_DEFAULT
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_DEFAULT 0L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_32BIT
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_32BIT 1L
#undef org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_64BIT
#define org_netbeans_installer_utils_system_windows_WindowsRegistry_MODE_64BIT 2L
/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    checkKeyAccess0
 * Signature: (IILjava/lang/String;I;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_checkKeyAccess0
  (JNIEnv *, jobject, jint, jint, jstring, jint);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    valueExists0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_valueExists0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    keyEmpty0
 * Signature: (IILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_keyEmpty0
  (JNIEnv *, jobject, jint, jint, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    countSubKeys0
 * Signature: (IILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_countSubKeys0
  (JNIEnv *, jobject, jint, jint, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    countValues0
 * Signature: (IILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_countValues0
  (JNIEnv *, jobject, jint, jint, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    getSubkeyNames0
 * Signature: (IILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getSubkeyNames0
  (JNIEnv *, jobject, jint, jint, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    getValueNames0
 * Signature: (IILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getValueNames0
  (JNIEnv *, jobject, jint, jint, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    getValueType0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getValueType0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    createKey0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_createKey0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    deleteKey0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_deleteKey0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    deleteValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_deleteValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    getStringValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;Z)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getStringValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring, jboolean);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    setStringValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setStringValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring, jstring, jboolean);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    get32BitValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_get32BitValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    set32BitValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_set32BitValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring, jint);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    getMultiStringValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getMultiStringValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    setMultiStringValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setMultiStringValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring, jobjectArray);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    getBinaryValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_getBinaryValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    setBinaryValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;[B)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setBinaryValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring, jbyteArray);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    setNoneValue0
 * Signature: (IILjava/lang/String;Ljava/lang/String;[B)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_setNoneValue0
  (JNIEnv *, jobject, jint, jint, jstring, jstring, jbyteArray);

/*
 * Class:     org_netbeans_installer_utils_system_windows_WindowsRegistry
 * Method:    IsWow64Process0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_windows_WindowsRegistry_IsWow64Process0
  (JNIEnv *, jobject);


#ifndef KEY_WOW64_32KEY
#define KEY_WOW64_32KEY 0x0200
#endif
#ifndef KEY_WOW64_64KEY
#define KEY_WOW64_64KEY 0x0100
#endif


#ifdef __cplusplus
}
#endif
#endif
