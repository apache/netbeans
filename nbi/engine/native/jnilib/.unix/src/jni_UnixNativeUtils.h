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
