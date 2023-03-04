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
/* Header for class org_netbeans_installer_utils_system_UnixNativeUtils */

#ifndef _Included_org_netbeans_installer_utils_system_UnixNativeUtils
#define _Included_org_netbeans_installer_utils_system_UnixNativeUtils
#ifdef __cplusplus
extern "C" {
#endif


#define MODE_RU 1L
#define MODE_WU 2L
#define MODE_EU 4L
#define MODE_RG 8L
#define MODE_WG 16L
#define MODE_EG 32L
#define MODE_RO 64L
#define MODE_WO 128L
#define MODE_EO 256L
    
#define MODE_CHANGE_SET 1L
#define MODE_CHANGE_ADD 2L
#define MODE_CHANGE_REMOVE 4L
        
    

/*
 * Class:     org_netbeans_installer_utils_system_UnixNativeUtils
 * Method:    getFreeSpace0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getFreeSpace0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_UnixNativeUtils
 * Method:    setPermission0
 * Signature: (Ljava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_setPermissions0
  (JNIEnv *, jobject, jstring, jint, jint);

/*
 * Class:     org_netbeans_installer_utils_system_UnixNativeUtils
 * Method:    getPermissions0
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getPermissions0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_UnixNativeUtils
 * Method:    isCurrentUserAdmin0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_isCurrentUserAdmin0
  (JNIEnv *, jobject);



#ifdef __cplusplus
}
#endif
#endif
