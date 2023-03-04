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
 */

#ifdef WIN32
#include <Windows.h>
#else
#define _GNU_SOURCE 
#include <dlfcn.h>
#endif
#include <stdio.h>
#include "jni.h"
#include "jvmti.h"

#include "org_netbeans_lib_profiler_server_system_HeapDump.h"


jint (JNICALL *JVM_DumpHeap15)(JNIEnv* env, jstring outputfile, jboolean live);

/*
 * Class:     org_netbeans_lib_profiler_server_system_HeapDump
 * Method:    initialize15
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_netbeans_lib_profiler_server_system_HeapDump_initialize15
  (JNIEnv *env, jclass clz) {
#ifdef WIN32
  /* Get the address of JVM_DumpHeap function */
   HMODULE hModule = GetModuleHandle("jvm.dll");
  if (hModule == NULL) {
    fprintf(stderr, "Profiler Agent Error: Unable to get handle to jvm.dll\n");
    return ; /* Unable to get handle to jvm.dll */
  }
  JVM_DumpHeap15 = (jint (JNICALL *)(JNIEnv*,jstring,jboolean)) GetProcAddress(hModule, "JVM_DumpHeap");
  if (JVM_DumpHeap15 == NULL) {
    fprintf(stderr, "Profiler Agent Error: Unable to get address of JVM_DumpHeap function\n");
    return; /* Unable to get address of JVM_DumpHeap function */
  }
#else  
  JVM_DumpHeap15 = (jint (JNICALL *)(JNIEnv*,jstring,jboolean)) dlsym(RTLD_DEFAULT, "JVM_DumpHeap");
  if (JVM_DumpHeap15 == NULL)  {
    //fprintf (stderr, "Profiler Agent: %s\n", dlerror());
    return;
  }
#endif

  //fprintf(stderr, "Profiler Agent: JVM_DumpHeap %p\n",JVM_DumpHeap15);
}

/*
 * Class:     org_netbeans_lib_profiler_server_system_HeapDump
 * Method:    takeHeapDump15Native
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_lib_profiler_server_system_HeapDump_takeHeapDump15Native
(JNIEnv *env, jclass clz, jstring outputfile) {
  jint error = -1;

  if (JVM_DumpHeap15 != NULL) {
    fprintf(stdout,"Profiler Agent: Heap dump..");
    error = (*JVM_DumpHeap15)(env,outputfile,JNI_TRUE);
    fprintf(stdout," end, status %d\n",(int)error);
  }
  return error;
}
