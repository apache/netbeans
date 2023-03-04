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
/*
 * author Ian Formanek 
 *        Misha Dmitriev
 *        Tomas Hurka
 */

#ifdef WIN32
#include <Windows.h>
#else
#include <sys/time.h>
#include <fcntl.h>
#include <time.h>
#endif

#ifdef SOLARIS
#define _STRUCTURED_PROC 1
#include <sys/procfs.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "jni.h"
#include "jvmti.h"

#include "org_netbeans_lib_profiler_server_system_Threads.h"

#include "common_functions.h"

/* The following is copied from org.netbeans.lib.profiler.global.CommonConstants */
#define THREAD_STATUS_UNKNOWN  -1
#define THREAD_STATUS_ZOMBIE    0
#define THREAD_STATUS_RUNNING   1
#define THREAD_STATUS_SLEEPING  2
#define THREAD_STATUS_MONITOR   3
#define THREAD_STATUS_WAIT      4
#define THREAD_STATUS_PARK      5

/* These masks essentially map JVMTI thread states into the above states */
static jint JF_THREAD_STATE_MASK       = JVMTI_THREAD_STATE_TERMINATED |
                                         JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_RUNNABLE |
                                         JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER |
                                         JVMTI_THREAD_STATE_WAITING |
                                         JVMTI_THREAD_STATE_WAITING_INDEFINITELY |
                                         JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT |
                                         JVMTI_THREAD_STATE_SLEEPING |
                                         JVMTI_THREAD_STATE_PARKED;
static jint JF_THREAD_STATE_NEW        = 0;
static jint JF_THREAD_STATE_TERMINATED = JVMTI_THREAD_STATE_TERMINATED;
static jint JF_THREAD_STATE_RUNNABLE   = JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_RUNNABLE;
static jint JF_THREAD_STATE_BLOCKED    = JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER;
static jint JF_THREAD_STATE_WAITING    = JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_WAITING |
                                         JVMTI_THREAD_STATE_WAITING_INDEFINITELY;
static jint JF_THREAD_STATE_TIMED_WAITING = JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_WAITING |
                                         JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT;
static jint JF_THREAD_STATE_SLEEPING   = JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_WAITING |
                                         JVMTI_THREAD_STATE_SLEEPING |
                                         JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT;
static jint JF_THREAD_STATE_TIMED_PARKED = JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_WAITING |
                                         JVMTI_THREAD_STATE_PARKED |
                                         JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT;
static jint JF_THREAD_STATE_PARKED = JVMTI_THREAD_STATE_ALIVE |
                                         JVMTI_THREAD_STATE_WAITING |
                                         JVMTI_THREAD_STATE_PARKED |
                                         JVMTI_THREAD_STATE_WAITING_INDEFINITELY;


static int nProfThreads;
static jthread *profThreads = NULL;
static jthread mainThread = NULL, singleProfThread = NULL, additionalProfThread = NULL;
static jclass threadType = NULL;


jint convert_JVMTI_thread_status_to_jfluid_status(jint jvmtiThreadStatus) {
    jint status = jvmtiThreadStatus & JF_THREAD_STATE_MASK;
  
    if      (status == JF_THREAD_STATE_RUNNABLE)      return THREAD_STATUS_RUNNING;
    else if (status == JF_THREAD_STATE_BLOCKED)       return THREAD_STATUS_MONITOR;
    else if (status == JF_THREAD_STATE_WAITING)       return THREAD_STATUS_WAIT;
    else if (status == JF_THREAD_STATE_TIMED_WAITING) return THREAD_STATUS_WAIT;
    else if (status == JF_THREAD_STATE_PARKED)        return THREAD_STATUS_PARK;
    else if (status == JF_THREAD_STATE_TIMED_PARKED)  return THREAD_STATUS_PARK;
    else if (status == JF_THREAD_STATE_SLEEPING)      return THREAD_STATUS_SLEEPING;
    else if (status == JF_THREAD_STATE_NEW)           return THREAD_STATUS_ZOMBIE;
    else if (status == JF_THREAD_STATE_TERMINATED)    return THREAD_STATUS_ZOMBIE;
    else                                              return THREAD_STATUS_UNKNOWN;
}


static int isProfilerThread(JNIEnv *env, jthread thread) {
    int i;
    if (additionalProfThread != NULL) {
        if ((*env)->IsSameObject(env, thread, additionalProfThread)) {
            return 1;
         }
    }
    if (profThreads != NULL) {
        for (i = 0; i < nProfThreads; i++) {
            if ((*env)->IsSameObject(env, thread, profThreads[i])) return 1;
        }
        return 0;
    } else {
        return ((*env)->IsSameObject(env, thread, singleProfThread));
    }
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    recordProfilerOwnThreads
 * Signature: (ZLjava/lang/Thread;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_recordProfilerOwnThreads
    (JNIEnv *env, jclass clz, jboolean excludeSpecialThread, jobject specialThread) 
{
    jvmtiError res;
    int i, j;
  
    if (profThreads != NULL) {
        for (i = 0; i < nProfThreads; i++) {
            (*env)->DeleteGlobalRef(env, profThreads[i]);
         }
        (*_jvmti)->Deallocate(_jvmti, (unsigned char*) profThreads);
    }
    
    profThreads = NULL;
    if (singleProfThread != NULL) {
        (*env)->DeleteGlobalRef(env, singleProfThread);
    }
    singleProfThread = NULL;
    if (mainThread != NULL) {
        (*env)->DeleteGlobalRef(env, mainThread);
    }
    mainThread = NULL;
  
    if (excludeSpecialThread) {
        res = (*_jvmti)->GetAllThreads(_jvmti, (jint*)&nProfThreads, &profThreads);
        assert(res == JVMTI_ERROR_NONE);
        if (specialThread != NULL) {
            for (i = 0; i < nProfThreads; i++) {
                if ((*env)->IsSameObject(env, specialThread, profThreads[i])) {
                    for (j = i+1; j < nProfThreads; j++) {
                        profThreads[j-1] = profThreads[j];
                    }
                    nProfThreads--;
                    break;
                }
            }
            mainThread = (*env)->NewGlobalRef(env, specialThread);
        }
        /* Results were returned as local references; make them global to survive across native calls */
        for (i = 0; i < nProfThreads; i++) {
            profThreads[i] = (*env)->NewGlobalRef(env, profThreads[i]);
        }
        return nProfThreads;
    } else {
        singleProfThread = (*env)->NewGlobalRef(env, specialThread);
        return 1;
    }
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    recordAdditionalProfilerOwnThread
 * Signature: (Ljava/lang/Thread;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_recordAdditionalProfilerOwnThread
    (JNIEnv *env, jclass clz, jobject specialThread) 
{
    if (additionalProfThread != NULL) {
        (*env)->DeleteGlobalRef(env, additionalProfThread);
    }
    additionalProfThread = (*env)->NewGlobalRef(env, specialThread);
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    getTotalNumberOfThreads
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_getTotalNumberOfThreads
    (JNIEnv *env, jclass clz) 
{
    jvmtiError res;
    int nThreads;
    jthread *threads = NULL;
  
    res = (*_jvmti)->GetAllThreads(_jvmti, (jint*)&nThreads, &threads);
    assert(res == JVMTI_ERROR_NONE);
    if (threads != NULL) {
        (*_jvmti)->Deallocate(_jvmti, (unsigned char*) threads);
    }
    return nThreads;
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    suspendTargetAppThreads
 * Signature: (Ljava/lang/Thread;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_suspendTargetAppThreads
    (JNIEnv *env, jclass clz, jobject excludedThread) 
{
    jvmtiError res;
    int i;
    int nThreads;
    jthread *threads = NULL;
  
    res = (*_jvmti)->GetAllThreads(_jvmti, (jint*)&nThreads, &threads);
    assert(res == JVMTI_ERROR_NONE);
  
    for (i = 0; i < nThreads; i++) {
        if (! (isProfilerThread(env, threads[i]) || (*env)->IsSameObject(env, excludedThread, threads[i]))) {
            (*_jvmti)->SuspendThread(_jvmti, threads[i]);
        }
    }
  
    (*_jvmti)->Deallocate(_jvmti, (unsigned char*) threads);
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    resumeTargetAppThreads
 * Signature: (Ljava/lang/Thread;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_resumeTargetAppThreads
    (JNIEnv *env, jclass clz, jobject excludedThread) 
{
    jvmtiError res;
    int i;
    int nThreads;
    jthread *threads = NULL;
  
    res = (*_jvmti)->GetAllThreads(_jvmti, (jint*)&nThreads, &threads);
    assert(res == JVMTI_ERROR_NONE);
  
    for (i = 0; i < nThreads; i++) {
        if (! (isProfilerThread(env, threads[i]) || (*env)->IsSameObject(env, excludedThread, threads[i]))) {
            (*_jvmti)->ResumeThread(_jvmti, threads[i]);
        }
    }
  
    (*_jvmti)->Deallocate(_jvmti, (unsigned char*) threads);
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    terminateTargetAppThreads
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_terminateTargetAppThreads
    (JNIEnv *env, jclass clz, jobject excObject) 
{
    jvmtiError res;
    int i;
    int nThreads;
    jthread *threads;
  
    res = (*_jvmti)->GetAllThreads(_jvmti, (jint*)&nThreads, &threads);
    assert(res == JVMTI_ERROR_NONE);
  
    for (i = 0; i < nThreads; i++) {
        if (! isProfilerThread(env, threads[i])) {
            (*_jvmti)->StopThread(_jvmti, threads[i], excObject);
        }
    }
  
    (*_jvmti)->Deallocate(_jvmti, (unsigned char*) threads);
    if (mainThread != NULL) {
        (*env)->DeleteGlobalRef(env, mainThread);
    }
    mainThread = NULL;
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    targetAppThreadsExist
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_targetAppThreadsExist
    (JNIEnv *env, jclass clz) 
{
    jvmtiError res;
    int i;
    jboolean exist;
    int nThreads;
    jthread *threads;
  
    res = (*_jvmti)->GetAllThreads(_jvmti, (jint*)&nThreads, &threads);
    assert(res == JVMTI_ERROR_NONE);
  
    exist = 0;
    for (i = 0; i < nThreads; i++) {
        if (! isProfilerThread(env, threads[i]) && ! (*env)->IsSameObject(env, threads[i], mainThread)) {
            exist = 1;
            break;
        }
    }
  
    (*_jvmti)->Deallocate(_jvmti, (unsigned char*) threads);
    return exist;
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    getAllThreads
 * Signature: ([Ljava/lang/Thread;)[Ljava/lang/Thread;
 */
JNIEXPORT jobjectArray JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_getAllThreads
    (JNIEnv *env , jclass clz, jobjectArray jthreads) 
{
    int nThreads, arrayLen, i;
    jthread *threads = NULL;
    jvmtiError res;

    res = (*_jvmti)->GetAllThreads(_jvmti, (jint*)&nThreads, &threads);
    assert(res == JVMTI_ERROR_NONE);
	  
    arrayLen = (jthreads != NULL) ? (*env)->GetArrayLength(env, jthreads) : 0;
    if (nThreads > arrayLen) {
        if (threadType == NULL) {
            threadType = (*env)->FindClass(env, "java/lang/Thread");
            threadType = (*env)->NewGlobalRef(env, threadType);
        }
        jthreads = (*env)->NewObjectArray(env, nThreads, threadType, NULL);
    }
  
    for (i = 0; i < nThreads; i++) {
        (*env)->SetObjectArrayElement(env, jthreads, i, threads[i]);
    }
    for (i = nThreads; i < arrayLen; i++) {
        (*env)->SetObjectArrayElement(env, jthreads, i, NULL);
    }
  
    (*_jvmti)->Deallocate(_jvmti, (unsigned char*) threads);
    return jthreads;
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    getThreadsStatus
 * Signature: ([Ljava/lang/Thread;[I)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_getThreadsStatus
    (JNIEnv *env, jclass clz, jobjectArray jthreads, jintArray jstatus) 
{
    int nThreads, i;
    jint threadStatus;
    jint *status;
  
    nThreads = (*env)->GetArrayLength(env, jthreads);
    status = malloc(nThreads * sizeof(jint));
  
    for (i = 0; i < nThreads; i++) {
        jthread thread = (*env)->GetObjectArrayElement(env, jthreads, i);
        if (thread == NULL) {
            continue;
         }
        (*_jvmti)->GetThreadState(_jvmti, thread, &threadStatus);
        status[i] = convert_JVMTI_thread_status_to_jfluid_status(threadStatus);
    }
  
    (*env)->SetIntArrayRegion(env, jstatus, 0, nThreads, status);
    free(status);
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    getJVMArguments
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_getJVMArguments
    (JNIEnv *env, jclass clz) 
{
    jstring ret = (*env)->NewStringUTF(env, "*NOT PROVIDED IN THIS JVM VERSION*");
    return ret;
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Threads
 * Method:    getJavaCommand
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_netbeans_lib_profiler_server_system_Threads_getJavaCommand
    (JNIEnv *env, jclass clz) 
{
    jstring ret = (*env)->NewStringUTF(env, "*NOT PROVIDED IN THIS JVM VERSION*");
    return ret;
}
