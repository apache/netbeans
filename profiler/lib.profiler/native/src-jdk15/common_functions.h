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

extern jvmtiEnv            *_jvmti;
extern jvmtiEventCallbacks *_jvmti_callbacks;

jlong get_nano_time();

void JNICALL class_file_load_hook(
            jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jclass class_being_redefined,
            jobject loader,
            const char* name,
            jobject protection_domain,
            jint class_data_len,
            const unsigned char* class_data,
            jint* new_class_data_len,
            unsigned char** new_class_data);

void JNICALL native_method_bind_hook(
            jvmtiEnv *jvmti_env,
            JNIEnv* env,
            jthread thread,
            jmethodID method,
            void* address,
            void** new_address_ptr);

void JNICALL monitor_contended_enter_hook(
            jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jthread thread,
            jobject object);

void JNICALL monitor_contended_entered_hook(
            jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jthread thread,
            jobject object);

void JNICALL vm_object_alloc(
            jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jthread thread,
            jobject object,
            jclass object_klass,
            jlong size);

typedef void (JNICALL *waitCall) (JNIEnv *env, jobject obj, jlong arg);
typedef void (JNICALL *sleepCall) (JNIEnv *env, jclass clazz, jlong arg);
typedef void (JNICALL *parkCall) (JNIEnv *env, jclass clazz, jboolean arg0, jlong arg1);

void JNICALL waitInterceptor(JNIEnv *env, jobject obj, jlong arg);
void JNICALL sleepInterceptor(JNIEnv *env, jclass clazz, jlong arg);
void JNICALL parkInterceptor(JNIEnv *env, jclass clazz, jboolean arg0, jlong arg1);

void get_saved_class_file_bytes(JNIEnv *env, char *name, jobject loader, jint *class_data_len, unsigned char **class_data);

void try_removing_bytes_for_unloaded_classes(JNIEnv *env);

void cache_loaded_classes(jvmtiEnv *jvmti_env,jclass *classes,jint class_count); 

void JNICALL vm_init_hook(jvmtiEnv *jvmti_env, JNIEnv* jni_env, jthread thread);

void parse_options_and_extract_params(char *options);

