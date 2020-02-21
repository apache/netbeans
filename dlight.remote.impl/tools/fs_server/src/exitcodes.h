/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#ifndef EXITCODES_H
#define	EXITCODES_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#define FAILURE_LOCKING_MUTEX                   201
#define FAILURE_UNLOCKING_MUTEX                 202
#define WRONG_ARGUMENT                          203
#define FAILURE_GETTING_HOME_DIR                204
#define FAILURE_CREATING_STORAGE_SUPER_DIR      205
#define FAILURE_ACCESSING_STORAGE_SUPER_DIR     206
#define FAILURE_CREATING_STORAGE_DIR            207
#define FAILURE_ACCESSING_STORAGE_DIR           208
#define FAILURE_CREATING_TEMP_DIR               209
#define FAILURE_ACCESSING_TEMP_DIR              210
#define FAILURE_CREATING_CACHE_DIR              211
#define FAILURE_ACCESSING_CACHE_DIR             212
#define NO_MEMORY_EXPANDING_DIRTAB              213
#define FAILED_CHDIR                            214
#define FAILURE_OPENING_LOCK_FILE               215
#define FAILURE_LOCKING_LOCK_FILE               216
#define FAILURE_DIRTAB_DOUBLE_CACHE_OPEN        217
#define DIRTAB_DOUBLE_INITIALIZATION                218
#define DIRTAB_SET_PERSIST_DIR_AFTER_INITIALIZATION 219
//#define DIRTAB_SET_PERSIST_INEXISTENT               220
#define FAILURE_SETTING_SIGNAL_HANDLER              221
#define FAILURE_SETTING_EXIT_FUNCTION               222
#define FAILURE_ILLEGAL_ARGUMENT                    223
#define FAILURE_ALLOCATE_MEMORY                     224
#define FAILURE_REALLOCATE_MEMORY                   225

#endif	/* EXITCODES_H */

