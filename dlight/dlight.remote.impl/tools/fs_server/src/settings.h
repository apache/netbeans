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

#ifndef SETTINGS_H
#define SETTINGS_H

#ifdef __cplusplus
extern "C" {
#endif

/// To lower concurrency between threads
/// settings use copy-on-write pattern.
/// To change settings:
///     create a new instance on the heap, fill it, then
///     under settings_mutex:
///         store previous instance in prev field of new one
///         change settings variable
/// To use settings:
///     under settings_mutex make a shallow copy,
///     use without mutex
/// Settings are freed only on shutdown.
/// settings pointer is never null, set it to default_settings instead of null

typedef struct {
    /// directories that are forbidden to stat; the last element is null
    const char** dirs_forbidden_to_stat;
    /// true if full access check via access() function is needed
    bool full_access_check;
    /// points to previous instance
    void* prev;
} settings_str;

void clone_global_settings(settings_str* dst);
void change_settings(const char** dirs_forbidden_to_stat, bool *full_access_check);
void free_settings();
bool is_dir_forbidden_to_stat(const char* path2check, const settings_str* settings);
void set_dirs_forbidden_to_stat(const char* dir_list);

#ifdef __cplusplus
}
#endif

#endif /* SETTINGS_H */

