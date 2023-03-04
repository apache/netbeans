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
/* Header for class org_netbeans_installer_utils_system_WindowsNativeUtils */

#ifndef _Included_org_netbeans_installer_utils_system_WindowsNativeUtils
#define _Included_org_netbeans_installer_utils_system_WindowsNativeUtils

#ifdef __cplusplus
extern "C" {
#endif

#undef org_netbeans_installer_utils_system_WindowsNativeUtils_MIN_UID_INDEX
#define org_netbeans_installer_utils_system_WindowsNativeUtils_MIN_UID_INDEX 1L

#undef org_netbeans_installer_utils_system_WindowsNativeUtils_MAX_UID_INDEX
#define org_netbeans_installer_utils_system_WindowsNativeUtils_MAX_UID_INDEX 100L

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    isCurrentUserAdmin0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_isCurrentUserAdmin0
  (JNIEnv *, jobject);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    getFreeSpace0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_getFreeSpace0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    createShortcut0
 * Signature: (Lorg/netbeans/installer/utils/system/shortcut/FileShortcut;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createShortcut0
  (JNIEnv *, jobject, jobject);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    deleteFileOnReboot0
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_deleteFileOnReboot0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    createProcessWithoutHandles0
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createProcessWithoutHandles0
  (JNIEnv *, jobject, jstring);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    notifyAssociationChanged0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyAssociationChanged0
  (JNIEnv *, jobject);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    checkAccessTokenAccessLevel0
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_checkAccessTokenAccessLevel0
  (JNIEnv *, jobject, jstring, jint);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    notifyEnvironmentChanged0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyEnvironmentChanged0
  (JNIEnv *, jobject);


#ifdef __cplusplus
}
#endif

#endif
