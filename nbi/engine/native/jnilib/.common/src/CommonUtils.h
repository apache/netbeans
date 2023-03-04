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
