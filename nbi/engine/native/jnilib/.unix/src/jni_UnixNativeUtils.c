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
