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

#include <stdio.h>
#include <unistd.h>
#include <limits.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>

typedef int bool;

enum {
    true = 1,
    false = 0
};

extern bool trace_flag;

void init_trace_flag(const char* env_var);

void report_error(const char *format, ...);

void report_unresolved_path(const char* path);

#define trace(...) if (trace_flag) { _trace(__VA_ARGS__); }
void _trace(const char *format, ...);

#define trace_startup(prefix, env_var, binary) if (trace_flag) { _trace_startup(prefix, env_var, binary); }
void _trace_startup(const char* prefix, const char* env_var, const char* binary);

#define trace_shutdown() if (trace_flag) { _trace_shutdown(); }
void _trace_shutdown();

#define trace_unresolved_path(path, action) if (trace_flag) { _trace_unresolved_path(path, action); }
void _trace_unresolved_path(const char* path, const char* action);

#define dbg_sleep(time) if (trace_flag) { _dbg_sleep(time); }
void _dbg_sleep(int time);

void *malloc_wrapper(size_t size);
