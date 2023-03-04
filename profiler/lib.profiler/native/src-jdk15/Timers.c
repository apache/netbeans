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
 * author Tomas Hurka
 *        Ian Formanek
 *        Misha Dmitriev
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
#include <unistd.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "jni.h"
#include "jvmti.h"

#include "org_netbeans_lib_profiler_server_system_Timers.h"

#include "common_functions.h"

#ifdef CVM
/*
 * Class:     org_netbeans_lib_profiler_server_system_Timers
 * Method:    getCurrentTimeInCounts
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_lib_profiler_server_system_Timers_getCurrentTimeInCounts
  (JNIEnv *env, jclass clz)
{
        jlong time;
        jvmtiError res;

        res = (*_jvmti)->GetTime(_jvmti,&time);
        if (res != JVMTI_ERROR_NONE) fprintf(stderr, "Profiler Agent Error: GetTime failed with %d\n",res);
        assert(res == JVMTI_ERROR_NONE);
        return time;
}

#endif

/*
 * Class:     org_netbeans_lib_profiler_server_system_Timers
 * Method:    getThreadCPUTimeInNanos
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_lib_profiler_server_system_Timers_getThreadCPUTimeInNanos
  (JNIEnv *env, jclass clz)
{   
	jlong threadTime;
	jvmtiError res;
	
	res = (*_jvmti)->GetCurrentThreadCpuTime(_jvmti,&threadTime);
	if (res != JVMTI_ERROR_NONE) fprintf(stderr, "Profiler Agent Error: GetCurrentThreadCpuTime failed with %d\n",res);
	assert(res == JVMTI_ERROR_NONE);
	return threadTime;
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Timers
 * Method:    osSleep
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_Timers_osSleep
  (JNIEnv *env, jclass clz, jint ns)
{
#ifndef WIN32
    struct timespec rqtp;
    rqtp.tv_sec = 0;
    rqtp.tv_nsec = ns;
    nanosleep(&rqtp, NULL);
#endif
}


/*
 * Class:     org_netbeans_lib_profiler_server_system_Timers
 * Method:    enableMicrostateAccounting
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_Timers_enableMicrostateAccounting
  (JNIEnv *env, jclass clz, jboolean enable)
{
#ifdef SOLARIS
    int ctlfd;
    long ctl[2];
    char procname[1024];

    sprintf(procname, "/proc/%d/ctl", getpid());
    ctlfd = open(procname, O_WRONLY);
    if (ctlfd < 0) {
        /*fprintf(stderr, "open %s failed, errno = %d\n", procname, errno);*/
        return;
    }

    if (enable) {
        ctl[0] = PCSET;
    } else {
        ctl[0] = PCUNSET;
    }
    ctl[1] = PR_MSACCT;
    if (write(ctlfd, ctl, 2*sizeof(long)) < 0) {
    /*
        fprintf(stderr, "write failed, errno = %d\n", errno);
    */
    }
    close(ctlfd);
#endif
}
