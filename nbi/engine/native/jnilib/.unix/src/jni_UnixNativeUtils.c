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
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/statvfs.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "../../.common/src/CommonUtils.h"
#include "jni_UnixNativeUtils.h"


jboolean statMode(const char *path, int *mode) {
    struct stat sb;
    if (stat(path, &sb) == 0) {
        *mode = sb.st_mode;
        return 1;
    } else {
        return 0;
    }
}


JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getFreeSpace0(JNIEnv* jEnv, jobject jObject, jstring jPath) {
    char* path   = getChars(jEnv, jPath);
    jlong result = 0;
    
    struct statvfs fsstat;
    if(memset(&fsstat, 0, sizeof(struct statvfs)) != NULL) {
        if(statvfs(path, &fsstat) == 0) {
            result = (jlong) fsstat.f_frsize;
            result *= (jlong) fsstat.f_bfree;
        }
    }
    
    
    FREE(path);
    return result;
}


JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_setPermissions0(JNIEnv *jEnv, jobject jObject, jstring jPath, jint jMode, jint jChange) {
    char* path = getChars(jEnv, jPath);    
    int currentMode = 0 ;
    char * msg = NULL;
    if(statMode(path, &currentMode)) {
        switch (jChange) {
            case MODE_CHANGE_SET:
                currentMode |= (S_IRWXU | S_IRWXG | S_IRWXO);
                currentMode &= jMode;
                break;
            case MODE_CHANGE_ADD:
                currentMode |= jMode;
                break;
            case MODE_CHANGE_REMOVE:
                currentMode &= ~jMode;
                break;
            default:     
                msg = (char*) malloc(sizeof(char) * 60);
                memset(msg, 0, sizeof(char) * 60);
                sprintf(msg, "Selected change mode (%ld) is not supported", jChange);
                throwException(jEnv, msg);
                FREE(msg);
                FREE(path);                
                return;                
        }
        chmod(path, currentMode);
    } else {
        throwException(jEnv, "Can`t get file current permissions");
    }
    FREE(path);
}


JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_getPermissions0(JNIEnv *jEnv, jobject jObject, jstring jPath) {
    char* path = getChars(jEnv, jPath);
    int currentMode;
    if(statMode(path, &currentMode)) {
        return currentMode & (S_IRWXU | S_IRWXG | S_IRWXO);
    } else {
        throwException(jEnv, "Can`t get file current permissions");
    }
    
    FREE(path);
}

JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_UnixNativeUtils_isCurrentUserAdmin0 (JNIEnv *jEnv, jobject jObject) {
    return (geteuid()==0);
}
