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
