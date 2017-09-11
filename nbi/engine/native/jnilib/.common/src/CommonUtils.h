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
#include <wchar.h>
#include <stdlib.h>
#ifdef _MSC_VER 
#include <windows.h>
#endif

#ifndef _CommonUtils_H
#define	_CommonUtils_H


#ifdef _MSC_VER 
#define FREE(x) { if((x)!=NULL) { LocalFree(x); (x)=NULL;}}
#else  
#define FREE(x) { if((x)!=NULL) {      free(x); (x)=NULL;}}
#endif

#ifdef _MSC_VER 
#define MALLOC(x) LocalAlloc(LPTR, x)
#else  
#define MALLOC(x) malloc(x)
#endif


#ifdef _MSC_VER
#define ZERO(x,y) SecureZeroMemory((x),(y))
#else
#define ZERO(x,y) memset((x),0,(y))
#endif


#ifdef _MSC_VER
#define STRLEN(x) lstrlenA(x)
#else
#define STRLEN(x) strlen(x)
#endif


#ifdef _MSC_VER
#define WCSLEN(x) ((unsigned long)lstrlenW(x))
#else
#define WCSLEN(x) ((unsigned long)wcslen(x))
#endif


#ifdef _MSC_VER
#define STRNCPY(x,y,z) lstrcpynA((x),(y),(z))
#else
#define STRNCPY(x,y,z) strncpy((x),(y),(z))
#endif

#ifdef _MSC_VER
#define WCSNCPY(x,y,z) lstrcpynW((x),(y),(z))
#else
#define WCSNCPY(x,y,z) wcsncpy((x),(y),(z))
#endif


#define LOG_DEBUG    4
#define LOG_MESSAGE  3
#define LOG_WARNING  2
#define LOG_ERROR    1
#define LOG_CRITICAL 0

#ifdef __cplusplus
extern "C" {
#endif
    
jbyteArray getStringBytes(JNIEnv* jEnv, jstring jString);

jstring newStringFromJByteArray(JNIEnv* jEnv, jbyteArray jByteArray, int length);
jstring newStringFromJCharArray(JNIEnv* jEnv, jcharArray jCharArray, int length);

jstring getString (JNIEnv* jEnv, const char* chars);
jstring getStringW(JNIEnv* jEnv, const wchar_t * chars);


jstring getStringWithLength(JNIEnv* jEnv, const char* chars, int length);
jstring getStringWithLengthW(JNIEnv* jEnv, const wchar_t * chars, int length);

char* getChars(JNIEnv* jEnv, jstring jString);
wchar_t * getWideChars(JNIEnv *jEnv, jstring str);

char* getStringFromMethod(JNIEnv* jEnv, jobject object, const char* methodName);
wchar_t* getWideStringFromMethod(JNIEnv* jEnv, jobject object, const char* methodName) ;

jint getIntFromMethod(JNIEnv* jEnv, jobject object, const char* methodName);

jboolean isInstanceOf(JNIEnv* jEnv, jobject object, const char* className);

void throwException(JNIEnv* jEnv, const char* message);

void writeLog(JNIEnv* jEnv, int level, const char* message);

int mkdirs (JNIEnv* jEnv, const char *path);
int mkdirsW(JNIEnv* jEnv, const wchar_t *path);

unsigned char* getByteFromMultiString(JNIEnv *jEnv, jobjectArray jObjectArray, unsigned long* size);

#ifdef __cplusplus
}
#endif
#endif /* _CommonUtils_H */
