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

#ifndef UTIL_H
#define	UTIL_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#include <pthread.h>
#include <stdio.h>
#include <dirent.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "settings.h"

#ifdef	__cplusplus
extern "C" {
#endif

bool is_broken_pipe();
void set_broken_pipe();

typedef enum  {
    STDOUT,
    STDERR
} std_stream;

void my_fflush(std_stream stream);
void my_fprintf(std_stream stream, const char *format, ...);

typedef enum TraceLevel {
    TRACE_NONE = 0,
    TRACE_INFO = 1,
    TRACE_FINE = 2,
    TRACE_FINER = 3,
    TRACE_FINEST = 4
} TraceLevel;

void set_trace(TraceLevel new_level);
bool is_traceable(TraceLevel level);
void trace(TraceLevel level, const char *format, ...);
void redirect_err(const char* file_name);

void log_and_err_print(const char *format, ...);

void log_print(const char *format, ...);
void log_open(const char* path);
void log_close();

void report_error(const char *format, ...);
void soft_assert(int condition, char* format, ...);

void mutex_unlock_wrapper(pthread_mutex_t *mutex);
void mutex_lock_wrapper(pthread_mutex_t *mutex);
void *malloc_wrapper(size_t size);
void *realloc_wrapper(void *ptr, size_t size);
char *strdup_wrapper(const char* str);

bool get_home_dir(char* home, int size);
bool file_exists(const char* path);
bool dir_exists(const char* path);

int fclose_if_not_null(FILE* f);
int closedir_if_not_null(DIR *d);

void stopwatch_start();
void stopwatch_stop(TraceLevel level, const char* message);

char *replace_first(char *s, char c, char replacement);

/** opens a file in write-only exclusive mode with O_CREAT flag and mode 600 */
FILE* fopen600(const char* path, int flags);

int escape_strlen(const char* s);

char *escape_strcpy(char *dst, const char *src);

int unescape_strlen(const char* s);

char *unescape_strcpy(char *dst, const char *src);

char* signal_name(int signal);

long long get_mtime(struct stat *stat_buf);
long long get_curretn_time_millis();

bool clean_dir(const char* path);

/** 
 * I often have to pass one or more pairs (char *buffer, int buffer_size) to a function.
 * struct buffer and buffer_alloc/buffer_free functions help to simplify this
 */

typedef struct {
    const int size;
    char* data;
} buffer;

buffer buffer_alloc(int size);

void buffer_free(buffer* buf);

bool visit_dir_entries(
        const char* path, 
        bool (*visitor) (char* name, struct stat *st, char* link, const char* abspath, void *data), 
        void (*error_handler) (bool dir_itself, const char* path, int err, const char* additional_message, void *data),
        const settings_str* settings,
        void *data);

void default_error_handler(bool dir_itself, const char* path, int err, const char* additional_message, void *data);

const char* get_basename(const char *path);

int utf8_bytes_count(const char *buffer, int char_count);
int utf8_char_count(const char *buffer, int byte_count);
int utf8_strlen(const char *buffer);

bool is_subdir(const char* child, const char* parent);

/** The same as strncpy, but stores trailing zero byte even in src len is more than limit */
char *strncpy_w_zero(char *dst, const char *src, size_t limit);

typedef enum file_type {
    FILETYPE_FIFO = 'p',
    FILETYPE_CHR = 'c',
    FILETYPE_DIR = 'd',
    FILETYPE_BLK = 'b',
    FILETYPE_REG = '-',
    FILETYPE_LNK = 'l',
    FILETYPE_SOCK = 's',
    FILETYPE_DOOR = 'D',
    FILETYPE_PORT = 'P', 
    FILETYPE_UNKNOWN = 'u'
} file_type;

char mode_to_file_type_char(int mode);
file_type mode_to_file_type(int mode);

bool can_read(const struct stat *stat);
bool can_write(const struct stat *stat);
bool can_exec(const struct stat *stat);

int lstat_wrapper(const char *path, struct stat *stat_buf, const settings_str* settings);
int stat_wrapper(const char *path, struct stat *stat_buf, const settings_str* settings);

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */

