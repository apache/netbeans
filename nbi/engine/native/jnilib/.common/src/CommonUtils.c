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
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#include "CommonUtils.h"

jbyteArray getStringBytes(JNIEnv* jEnv, jstring jString) {
    jbyteArray result = NULL;
    
    if (jString != NULL) {
        jmethodID jGetBytesMethod = (*jEnv)->GetMethodID(jEnv, (*jEnv)->GetObjectClass(jEnv, jString), "getBytes", "()[B");
        
        if (jGetBytesMethod != NULL) {
            jbyteArray jBuffer = (jbyteArray) (*jEnv)->CallObjectMethod(jEnv, jString, jGetBytesMethod);
            
            if (jBuffer != NULL) {
                jsize jLength = (*jEnv)->GetArrayLength(jEnv, jBuffer);
                
                result = (*jEnv)->NewByteArray(jEnv, jLength + 1);
                if (jLength != 0) {
                    jbyte* jChars = (*jEnv)->GetByteArrayElements(jEnv, jBuffer, NULL);
                    (*jEnv)->SetByteArrayRegion(jEnv, result, 0, jLength, jChars);
                    (*jEnv)->ReleaseByteArrayElements(jEnv, jBuffer, jChars, JNI_ABORT);
                }
                (*jEnv)->DeleteLocalRef(jEnv, jBuffer);
            }
            //(*jEnv)->DeleteLocalRef(jEnv, getBytesMethod);
        }
    }
    
    return result;
}


jstring newStringFromJByteArray(JNIEnv* jEnv, jbyteArray jByteArray, int length) {
    jstring result = NULL;
    
    jclass jStringClass = (*jEnv)->FindClass(jEnv, "java/lang/String");
    if (jStringClass != NULL) {
        jmethodID jStringConstructor = (*jEnv)->GetMethodID(jEnv, jStringClass, "<init>", "([BII)V");
        
        if (jStringConstructor != NULL) {
            result = (jstring) (*jEnv)->NewObject(jEnv, jStringClass, jStringConstructor, jByteArray, 0, length);
            //(*jEnv)->DeleteLocalRef(jEnv, stringConstructor);
        }
        (*jEnv)->DeleteLocalRef(jEnv, jStringClass);
    }
    
    return result;
}


jstring newStringFromJCharArray(JNIEnv* jEnv, jcharArray jCharArray, int length) {
    jstring result = NULL;
    
    jclass jStringClass = (*jEnv)->FindClass(jEnv, "java/lang/String");
    if (jStringClass != NULL) {
        jmethodID jStringConstructor = (*jEnv)->GetMethodID(jEnv, jStringClass, "<init>", "([CII)V");
        
        if (jStringConstructor != NULL) {
            result = (jstring) (*jEnv)->NewObject(jEnv, jStringClass, jStringConstructor, jCharArray, 0, length);            
        }
        (*jEnv)->DeleteLocalRef(jEnv, jStringClass);
    }
    
    return result;
}

jstring getString(JNIEnv* jEnv, const char* chars) {
    return (jstring) getStringWithLength(jEnv, chars, (int) STRLEN(chars));
}

jstring getStringW(JNIEnv* jEnv, const wchar_t * chars) {
    return (jstring) getStringWithLengthW(jEnv, chars, (int) WCSLEN(chars));
}

jstring getStringWithLength(JNIEnv* jEnv, const char* chars, int length) {
    jstring result = NULL;
    
    if (chars != NULL) {
        if (length == 0) {
            result = (*jEnv)->NewString(jEnv, (const jchar *) L"", 0);
        } else {
            jbyteArray jByteArray = (*jEnv)->NewByteArray(jEnv, length);
            
            if (jByteArray != NULL) {
                (*jEnv)->SetByteArrayRegion(jEnv, jByteArray, 0, length, (jbyte*) chars);
                result = newStringFromJByteArray(jEnv, jByteArray, length);
                (*jEnv)->DeleteLocalRef(jEnv, jByteArray);
            }
        }
    }
    
    return result;
}


jstring getStringWithLengthW(JNIEnv* jEnv, const wchar_t* chars, int length) {
    jstring result = NULL;
    
    if (chars != NULL) {
        if (length == 0) {
            result = (*jEnv)->NewString(jEnv, (const jchar *) L"", 0);
        } else {
            jcharArray jCharArray = (*jEnv)->NewCharArray(jEnv, length);
            
            if (jCharArray != NULL) {
                (*jEnv)->SetCharArrayRegion(jEnv, jCharArray, 0, length, (jchar*) chars);
                result = newStringFromJCharArray(jEnv, jCharArray, length);
                (*jEnv)->DeleteLocalRef(jEnv, jCharArray);
            }
        }
    }
    
    return result;
}


char* getChars(JNIEnv* jEnv, jstring jString) {
    char* result = NULL;
    
    jbyteArray jByteArray = getStringBytes(jEnv, jString);
    if (jByteArray != NULL) {
        jbyte* jBytes = (*jEnv)->GetByteArrayElements(jEnv, jByteArray, NULL);
        
        long index = 0;
        if (jBytes != NULL) {
            int length = (int) STRLEN((char*) jBytes);
            
            result = (char*) MALLOC(sizeof(char) * (length + 1));
            if (result != NULL) {
                ZERO(result, length);
                STRNCPY(result, (char*) jBytes, length);
                result[length] = 0;
            }
            (*jEnv)->ReleaseByteArrayElements(jEnv, jByteArray, jBytes, JNI_ABORT);
        }
        (*jEnv)->DeleteLocalRef(jEnv, jByteArray);
    }
    
    return result;
}

char* getStringFromMethod(JNIEnv* jEnv, jobject object, const char* methodName) {
    char* result = NULL;
    
    jclass clazz = (*jEnv)->GetObjectClass(jEnv, object);
    if (clazz != NULL) {
        jmethodID method = (*jEnv)->GetMethodID(jEnv, clazz, methodName, "()Ljava/lang/String;");
        if (method != NULL) {
            jstring string = (jstring) (*jEnv)->CallObjectMethod(jEnv, object, method);
            if (string != NULL) {
                result = getChars(jEnv, string);
                (*jEnv)->DeleteLocalRef(jEnv, string);
            }
            //(*jEnv)->DeleteLocalRef(jEnv, method);
        }
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
    
    return result;
}

wchar_t * getWideStringFromMethod(JNIEnv* jEnv, jobject object, const char* methodName) {
    wchar_t* result = NULL;
    
    jclass clazz = (*jEnv)->GetObjectClass(jEnv, object);
    if (clazz != NULL) {
        jmethodID method = (*jEnv)->GetMethodID(jEnv, clazz, methodName, "()Ljava/lang/String;");
        if (method != NULL) {
            jstring string = (jstring) (*jEnv)->CallObjectMethod(jEnv, object, method);
            if (string != NULL) {
                result = getWideChars(jEnv, string);
                (*jEnv)->DeleteLocalRef(jEnv, string);
            }
            //(*jEnv)->DeleteLocalRef(jEnv, method);
        }
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
    
    return result;
}

jboolean isInstanceOf(JNIEnv* jEnv, jobject object, const char* className) {
    jboolean result = 0;
    jclass clazz = clazz = (*jEnv)->FindClass(jEnv, className);
    
    if (clazz != NULL) {
        result = (*jEnv)->IsInstanceOf(jEnv, object, clazz);
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
    return result;
}

jint getIntFromMethod(JNIEnv* jEnv, jobject object, const char* methodName) {
    jint value = 0;
    
    jclass clazz = (*jEnv)->GetObjectClass(jEnv, object);
    if (clazz != NULL) {
        jmethodID method = (*jEnv)->GetMethodID(jEnv, clazz, methodName, "()I");
        if (method != NULL) {
            value = (*jEnv)->CallIntMethod(jEnv, object, method);
        }
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
    
    return value;
}

void throwException(JNIEnv* jEnv, const char* message) {
    jclass clazz = (*jEnv)->FindClass(jEnv, "org/netbeans/installer/utils/exceptions/NativeException");
    if (clazz != NULL) {
        (*jEnv)->ThrowNew(jEnv, clazz, message);
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
}

void writeLog(JNIEnv* jEnv, int level, const char* message) {
    const char* prefix = "[jni] ";
    
    jclass clazz = (*jEnv)->FindClass(jEnv, "org/netbeans/installer/utils/LogManager");
    if (clazz != NULL) {
        jmethodID method = (*jEnv)->GetStaticMethodID(jEnv, clazz, "log", "(ILjava/lang/String;)V");
        if (method != NULL) {
            jstring jMessage = NULL;
            int prefix_length = STRLEN(prefix);
            int message_length = STRLEN(message);
            char* string = (char*) MALLOC(sizeof(char) * (prefix_length + message_length + 1));
            int i = 0;
            for(i=0;i<prefix_length;i++) {
               string[i]=prefix[i];
            }
            
            for(i=0;i<message_length;i++) {
               string[i + prefix_length]=message[i];
            }
            string[i+prefix_length + message_length] = '\0';

            
            jMessage = getString(jEnv, string);
            
            if (jMessage != NULL) {
                (*jEnv)->CallStaticVoidMethod(jEnv, clazz, method, (jint) level, jMessage);
                (*jEnv)->DeleteLocalRef(jEnv, jMessage);
            }
            
            FREE(string);
            //(*jEnv)->DeleteLocalRef(jEnv, method);
        }
        (*jEnv)->DeleteLocalRef(jEnv, clazz);
    }
}

int createDirs(JNIEnv* jEnv, jstring jPath) {
    int result = 0;
    jclass jFileClass = (*jEnv)->FindClass(jEnv, "java/io/File");
    if (jFileClass != NULL) {
        jmethodID jFileConstructor     = (*jEnv)->GetMethodID(jEnv, jFileClass, "<init>", "(Ljava/lang/String;)V");
        jmethodID jGetParentFileMethod = (*jEnv)->GetMethodID(jEnv, jFileClass, "getParentFile", "()Ljava/io/File;");
        jmethodID jExistsMethod        = (*jEnv)->GetMethodID(jEnv, jFileClass, "exists", "()Z");
        jmethodID jMkdirsMethod        = (*jEnv)->GetMethodID(jEnv, jFileClass, "mkdirs", "()Z");
        
        if ((jFileConstructor != NULL) && (jGetParentFileMethod != NULL) && (jExistsMethod != NULL) && (jMkdirsMethod != NULL)) {            
            jobject jFile = (*jEnv)->NewObject(jEnv, jFileClass, jFileConstructor, jPath);
            if (jFile != NULL ) {
                if(!((*jEnv)->CallBooleanMethod(jEnv, jFile, jExistsMethod))) {
                    jobject jParent = (*jEnv)->CallObjectMethod(jEnv, jFile, jGetParentFileMethod);
                    if (jParent != NULL) {
                        result = (*jEnv)->CallBooleanMethod(jEnv, jParent, jExistsMethod);
                        if (!result) {
                            result = (*jEnv)->CallBooleanMethod(jEnv, jParent, jMkdirsMethod);
                        }
                        (*jEnv)->DeleteLocalRef(jEnv, jParent);
                    }
                } else {
                    result = 1;
                }
                (*jEnv)->DeleteLocalRef(jEnv, jFile);
            }
        }
        (*jEnv)->DeleteLocalRef(jEnv, jFileClass);
    }
    return result;
}

int mkdirs(JNIEnv* jEnv, const char *path) {
    int result = 1;
    jstring jPath  = getString(jEnv, path);
    if (jPath != NULL) {
        result = createDirs(jEnv, jPath);
        (*jEnv)->DeleteLocalRef(jEnv, jPath);
    }
    return result;
}


int mkdirsW(JNIEnv* jEnv, const wchar_t *path) {
    int result = 1;
    jstring jPath  = getStringW(jEnv, path);
    if (jPath != NULL) {        
        result = createDirs(jEnv, jPath);
        (*jEnv)->DeleteLocalRef(jEnv, jPath);
    }
    
    return result;
}



unsigned char* getByteFromMultiString(JNIEnv *jEnv, jobjectArray jObjectArray, unsigned long* size) {
    unsigned short * result = NULL;
    
    int     totalLength = 0;
    unsigned int arrayLength = (*jEnv)->GetArrayLength(jEnv, jObjectArray);
    jstring jString     = NULL;
    
    unsigned int i, j; // just counters
    
    for (i = 0; i < arrayLength; i++) {
        jString = (jstring) (*jEnv)->GetObjectArrayElement(jEnv, jObjectArray, i);
        totalLength += (*jEnv)->GetStringLength(jEnv, jString) + 1;
    }
    totalLength++; // add null to the end of array
      
    result = (unsigned short*) MALLOC(sizeof(unsigned short) * totalLength);
    if (result != NULL) {
        int index = 0 ; 
        
        for (i = 0; i < arrayLength; i++) {
            jString = (jstring) (*jEnv)->GetObjectArrayElement(jEnv, jObjectArray, i);
            
            if (jString != NULL) {
                wchar_t * chars = getWideChars(jEnv, jString);
                if (chars != NULL) {
                    for (j = 0; j < (WCSLEN(chars)); j++) {
                        result[index++] = chars[j];
                    }
                    
                    FREE(chars);
                }
            }
            result[index++] = '\0';            
        }
        result[index++] = '\0'; //double \0 at the end        
    }    
    * size = sizeof(unsigned short) * totalLength;
    return (unsigned char*)result;
}

wchar_t * getWideChars(JNIEnv *jEnv, jstring jString) {
    if(jString==NULL) {
        return NULL;
    } else {
        long length = (*jEnv)->GetStringLength( jEnv, jString);
        const jchar * unicodeStr = (*jEnv)->GetStringChars( jEnv, jString, 0 );
        wchar_t * copy = (wchar_t *) MALLOC(sizeof(wchar_t) * (length + 1));
        ZERO(copy, sizeof(wchar_t)*(length + 1));        
        WCSNCPY(copy, (const wchar_t *) unicodeStr, length + 1);
        (*jEnv)->ReleaseStringChars( jEnv, jString, unicodeStr);
        return copy;
    }
    
}
