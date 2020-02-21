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

#include "fs_common.h"
#include "fs_server.h"
#include "blocking_queue.h"
#include "exitcodes.h"
#include "dirtab.h"
#include "array.h"
#include "settings.h"

#include <pthread.h>
#include <stddef.h>
#include <dirent.h>
#include <limits.h>
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <ctype.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <grp.h>
#include <pwd.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <getopt.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/param.h>

#define	MIN(a,b) (((a)<(b))?(a):(b))
#define	MAX(a,b) (((a)>(b))?(a):(b))

#define MAX_THREAD_COUNT 80
#define DEFAULT_THREAD_COUNT 4

typedef struct {
    int no;
    pthread_t id;
} thread_info;

/** Current threads count. NOT synchronized. To be accessed only from main thread */
static int rp_thread_count = DEFAULT_THREAD_COUNT;

/** Threads. NOT synchronized. To be accessed only from main thread */
static thread_info rp_threads[MAX_THREAD_COUNT];

static blocking_queue req_queue;

static bool clear_persistence = false;
static bool log_flag = false;
static bool persistence = false;
static bool refresh = false;
static bool refresh_explicit = false;
static bool statistics = false;
static int refresh_sleep = 1;
static int kill_locker_and_wait = 0;
static unsigned long locker_pid_to_kill = 0;
const char* error_log = NULL;
bool redirect_err_flag = false;
//static bool shutting_down = false;

#define FS_SERVER_MAJOR_VERSION 1
#define FS_SERVER_MID_VERSION 12
#define FS_SERVER_MINOR_VERSION 8

typedef struct fs_entry {
    int /*short?*/ name_len;
    char* name;
    int /*short?*/ link_len;
    char* link;
    char file_type;
    long size;
    long long mtime;
    bool can_read : 1;
    bool can_write : 1;
    bool can_exec : 1;
    unsigned long int /*dev_t*/ st_dev;
    unsigned long int /*ino_t*/ st_ino;
    char data[];
} fs_entry;

static struct {
    /** This mutex to be used ONLY to guard access to proceed field
     * or busy_threads fiels
     * NO other activity should be done under this mutex. */
    pthread_mutex_t mutex;
    int busy_threads;
    bool proceed;
} state;

static __thread struct {
    int err_no;
    char* errmsg;
    char* strerr;
} err_info;

static struct {
    queue queue;
    pthread_mutex_t mutex;
} delete_on_exit_list;

static const int thread_emsg_bufsize = PATH_MAX * 2 + 128; // should it be less?
static const int strerr_bufsize = PATH_MAX * 2 + 128; // should it be less?

static int err_get_code() {
    return err_info.err_no;
}

static const char* err_get_message() {
    return err_info.errmsg;
}

static const char* err_to_string(int err_no) {
#if __linux__
    return strerror_r(err_no, err_info.strerr, strerr_bufsize);
#else
    if (strerror_r(err_no, err_info.strerr, strerr_bufsize)) {
        return "";
    } else {
        return err_info.strerr;
    }
#endif
}

static void err_set(int code, const char *format, ...) {
    err_info.err_no = code;
    va_list args;
    va_start (args, format);
    vsnprintf(err_info.errmsg, thread_emsg_bufsize, format, args);
    va_end (args);
}

static void delete_on_exit_list_init() {
    pthread_mutex_init(&delete_on_exit_list.mutex, NULL);
    mutex_lock_wrapper(&delete_on_exit_list.mutex);
    queue_init(&delete_on_exit_list.queue);
    mutex_unlock_wrapper(&delete_on_exit_list.mutex);
}

static void delete_on_exit_list_add(const char* p) {
    mutex_lock_wrapper(&delete_on_exit_list.mutex);
    const char* p2 = strdup_wrapper(p);
    queue_add(&delete_on_exit_list.queue, (void*) p2);
    mutex_unlock_wrapper(&delete_on_exit_list.mutex);
}

static void delete_on_exit_impl() {
    trace(TRACE_INFO, "Processing files that should be deleted on exit\n");
    mutex_lock_wrapper(&delete_on_exit_list.mutex);
    int success_cnt = 0;
    int err_cnt = 0;
    void* p;
    while((p = queue_poll(&delete_on_exit_list.queue))) {
        const char* path = (const char*) p;
        trace(TRACE_FINEST, "  removing %s...\n", path);
        if(unlink(path)) {
            err_cnt++;
            report_error("error deleting file %s: %s\n", path, err_to_string(errno));
        } else {
            success_cnt++;            
        }
        free(p);
    }
    mutex_unlock_wrapper(&delete_on_exit_list.mutex);    
    trace(TRACE_INFO, "Successfully removed %d files; error removing %d files\n", success_cnt, err_cnt);
}

static void err_init() {
    err_info.err_no = 0;
    err_info.errmsg = malloc_wrapper(thread_emsg_bufsize);
    err_info.strerr = malloc_wrapper(strerr_bufsize);
    *err_info.errmsg = 0; // just in case
}

static void err_redirect_init() {
    if (redirect_err_flag) {
        if (!error_log) {
            const char* cache_root = dirtab_get_basedir();
            if (cache_root) {
                const char* name = "/stderr.txt";
                size_t sz = strlen(cache_root) + strlen(name) + 1;
                char* path = malloc_wrapper(sz);
                strncpy(path, cache_root, sz);
                strncat(path, name, sz);
                error_log = path;
            } else {
                error_log = strdup("/tmp/fs_server_stderr.txt");
            }
        }
        redirect_err(error_log);
    } else {
        redirect_err(NULL);
    }
}

static void err_shutdown() {
    err_info.err_no = 0;
    free(err_info.errmsg);
    err_info.errmsg = NULL;
    free(err_info.strerr);
    err_info.strerr = NULL;
}

static bool state_get_proceed() {
    bool proceed;
    mutex_lock_wrapper(&state.mutex);
    proceed = state.proceed; // don't even think of doing smth else under this mutex!
    mutex_unlock_wrapper(&state.mutex);
    return proceed;
}

static void state_set_proceed(bool proceed) {
    mutex_lock_wrapper(&state.mutex);
    state.proceed = proceed; // don't even think of doing smth else under this mutex!
    mutex_unlock_wrapper(&state.mutex);
}

static bool need_to_proceed() {
    return !is_broken_pipe() && state_get_proceed();
}

static void state_init() {
    pthread_mutex_init(&state.mutex, NULL);
    mutex_lock_wrapper(&state.mutex);
    state.proceed = true;
    state.busy_threads = 0;
    mutex_unlock_wrapper(&state.mutex);
}

static void decrement_busy_threads() {
    mutex_lock_wrapper(&state.mutex);
    state.busy_threads--;
    mutex_unlock_wrapper(&state.mutex);
}

static void increment_busy_threads() {
    mutex_lock_wrapper(&state.mutex);
    state.busy_threads++;
    mutex_unlock_wrapper(&state.mutex);
}

static int get_busy_threads() {
    int result;
    mutex_lock_wrapper(&state.mutex);
    result = state.busy_threads;
    mutex_unlock_wrapper(&state.mutex);
    return result;
}

#define DECLARE_DECODE(type, type_name, maxlen) \
static const char* decode_##type_name (const char* text, type* result) { \
    *result = 0; \
    const char* p = text; \
    if (!isdigit(*p)) { \
        report_error("unexpected numeric value: '%c'\n", *p); \
        return NULL; \
    } \
    while (p - text < maxlen) { \
        char c = *(p++); \
        if (isdigit(c)) { \
            *result *= 10; \
            *result += c - '0'; \
        } else if (c == 0 || isspace(c)) { \
            return p; \
        } else { \
            report_error("unexpected numeric value: '%c'\n", c); \
            return NULL; \
        } \
    } \
    report_error("numeric value too long: '%s'\n", text); \
    return NULL; \
}

DECLARE_DECODE(int, int, 12)
//DECLARE_DECODE(unsigned int, uint, 12) unused so far
DECLARE_DECODE(long, long, 20)
DECLARE_DECODE(unsigned long, ulong, 20)
DECLARE_DECODE(long long, long_long, 20)

static bool is_prohibited(const char* abspath) {
    if (strcmp("/proc", abspath) == 0) {
        return true;
    } else if(strcmp("/dev", abspath) == 0) {
        return true;
    }
    #if linux
    if (strcmp("/run", abspath) == 0) {
        return true;
    }
    #endif
    return false;
}

static bool has_second_path(enum  fs_request_kind kind) {
    return kind == FS_REQ_COPY || kind == FS_REQ_MOVE;
}

static void clone_request(fs_request* dst, fs_request* src) {
    memcpy(dst, src, src->size);
    if (dst->path) {
        dst->path = dst->data + (src->path - src->data);
    }
    if (dst->path2) {
        dst->path2 = dst->data + (src->path2 - src->data);
    }
}

static int word_len(const char* p) {
    int len = 0;
    while (*p && ! isspace(*p)) {
        p++;
        len++;
    }
    return len;
}

static bool is_valid_request_kind(char c) {
    switch (c) {
        case FS_REQ_LS:
        case FS_REQ_RECURSIVE_LS:
        case FS_REQ_STAT:
        case FS_REQ_LSTAT:
        case FS_REQ_COPY:
        case FS_REQ_MOVE:
        case FS_REQ_QUIT:
        case FS_REQ_SLEEP:
        case FS_REQ_ADD_WATCH:
        case FS_REQ_REMOVE_WATCH:
        case FS_REQ_REFRESH:
        case FS_REQ_DELETE:
        case FS_REQ_DELETE_ON_DISCONNECT:
        case FS_REQ_SERVER_INFO:
        case FS_REQ_HELP:
        case FS_REQ_OPTION:
            return true;
        default:
            return false;
    }
}

/**
 * Decodes in-place fs_raw_request into fs_request
 */
static fs_request* decode_request(char* raw_request, fs_request* request, int request_max_size) {
    request->kind = raw_request[0];
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
    //p++;
    int id;
    int path_len;
    const char* p;
    if (*raw_request == FS_REQ_QUIT || *raw_request == FS_REQ_HELP) {
        id = 0;
        path_len = 0;
        p = "";
    } else {
        if(*raw_request == '\0' || *raw_request == '\n') {
            report_error("wrong (zero length) request\n");
            return NULL;
        }
        if(!is_valid_request_kind(*raw_request)) {
            report_error("wrong request kind: %s", raw_request);
            return NULL;
        }
        if(*(raw_request+1) != ' ') {
            report_error("wrong request, no space after request kind: %s", raw_request);
            return NULL;
        }
        p = raw_request + 2;
        p = decode_int(p, &id);
        if (*raw_request == FS_REQ_SERVER_INFO) {
            path_len=0;
            p = "";
        } else {
            if (p == NULL) {
                return NULL;
            }
            //soft_assert(*p == ' ', "incorrect request format: '%s'", request);
            p = decode_int(p, &path_len);
            if (p == NULL) {
                return NULL;
            }
            if (!path_len && *raw_request != FS_REQ_QUIT) {
                path_len = word_len(p);
                if (!path_len) {
                    report_error("wrong (zero path) request: %s", raw_request);
                    return NULL;
                }
            }
        }
    }
    //fs_request->kind = request->kind;
    //soft_assert(*p == ' ', "incorrect request format: '%s'", request);

    path_len = utf8_bytes_count(p, path_len);

    if (path_len > (request_max_size - sizeof (fs_request) - 1)) {
        report_error("wrong (too long path) request: %s", raw_request);
        return NULL;
    }

    request->path = request->data;
    strncpy(request->data, p, path_len);
    request->data[path_len] = 0;
    unescape_strcpy(request->data, request->data);
    path_len = strlen(request->data);
    request->id = id;
    request->len = path_len;
    if (has_second_path(request->kind)) {
        p += path_len + 1;
        p = decode_int(p, &path_len);
        if (p == NULL) {
            return NULL;
        }
        if (path_len == 0) {
            path_len = word_len(p);
        }
        path_len = utf8_bytes_count(p, path_len);
        if (path_len > (request_max_size - sizeof (fs_request) - 1)) {
            report_error("wrong (too long path) request: %s", raw_request);
            return NULL;
        }
        char *path2 = request->data + request->len + 1;
        strncpy(path2, p, path_len);
        path2[path_len] = 0;
        unescape_strcpy(path2, path2);
        request->len2 = path_len;
        request->path2 = path2;
        request->size = offsetof(fs_request, data)+request->len+request->len2+2;
    } else {
        request->len2 = 0;
        request->path2 = NULL;
        request->size = offsetof(fs_request, data)+path_len+1;
    }
    return request;
}

static fs_entry* create_fs_entry(fs_entry *entry2clone) {
    int sz = sizeof(fs_entry) + entry2clone->name_len + entry2clone->link_len + 2;
    fs_entry *entry = malloc_wrapper(sz);
    entry->name_len = entry2clone->name_len;
    entry->name = entry->data;
    strncpy(entry->name, entry2clone->name, entry->name_len);
    entry->name[entry->name_len] = 0;
    entry->link_len = entry2clone->link_len;
    if (entry->link_len) {
        entry->link = entry->data + entry->name_len + 1;
        strncpy((char*)entry->link, entry2clone->link, entry->link_len);
        entry->link[entry->link_len] = 0;
    } else {
        entry->link = "";
    }
    entry->file_type = entry2clone->file_type;
    entry->size = entry2clone->size;
    entry->mtime = entry2clone->mtime;
    entry->can_read = entry2clone->can_read;
    entry->can_write = entry2clone->can_write;
    entry->can_exec = entry2clone->can_exec;
    entry->st_dev = entry2clone->st_dev;
    entry->st_ino = entry2clone->st_ino;
    return entry;
}


/**
 * Creates a fs_entry on heap
 * from a line that is read from persistence
 * NB: modifies buf: can unescape and zero-terminate strings
 */
static fs_entry *decode_entry_response(char* buf, int buf_size) {

    // format:
    // Version 1:
    // name_len name uid gid mode size mtime link_len link
    // Version 1 example:
    // 20 abi-3.2.0-70-generic 0 0 33188 796014 1411590344000 10 lnk_target
    // Version 2:
    // name_len name uid gid mode size mtime access device inode link_len link
    // Version 2 example:
    // 20 abi-3.2.0-70-generic 0 0 33188 796014 1411590344000 r-x 123 45678 10 lnk_target

    fs_entry tmp; // a temporary one since we don't know names size

    const char* p = decode_int(buf, &tmp.name_len);
    if (!p) { return NULL; }; // decode_int already printed error message

    tmp.name = (char*) p;
    tmp.name_len = utf8_bytes_count(tmp.name, tmp.name_len);
    if (p + tmp.name_len >= buf + buf_size) {
        report_error("wrong entry format: too long (%i) name: %s", tmp.name_len, buf);
        return NULL;
    }
    tmp.name[tmp.name_len] = 0;
    unescape_strcpy(tmp.name, tmp.name);
    p += tmp.name_len + 1;
    tmp.name_len = strlen(tmp.name);

    tmp.file_type = *p;
    p += 2;

    p = decode_long(p, &tmp.size);
    if (!p) { return NULL; };

    p = decode_long_long(p, &tmp.mtime);
    if (!p) { return NULL; };

    // next segment is "rwx" for this user
    if (*p == 'r') {
        tmp.can_read = true;
    } else if (*p == '-') {
        tmp.can_read = false;
    } else {
        report_error("wrong can_read flag: %c\n", *p);
        return NULL;
    }
    p++;
    if (*p == 'w') {
        tmp.can_write = true;
    } else if (*p == '-') {
        tmp.can_write = false;
    } else {
        report_error("wrong can_write flag: %c\n", *p);
        return NULL;
    }
    p++;
    if (*p == 'x') {
        tmp.can_exec = true;
    } else if (*p == '-') {
        tmp.can_exec = false;
    } else {
        report_error("wrong can_exec flag: %c\n", *p);
        return NULL;
    }
    p += 2;
    p = decode_ulong(p, &tmp.st_dev);
    if (!p) { return NULL; };
    p = decode_ulong(p, &tmp.st_ino);
    if (!p) { return NULL; };

    p = decode_int(p, &tmp.link_len);
    if (!p) { return NULL; };

    if (tmp.link_len) {
        tmp.link = (char*) p;
        if (p + tmp.link_len >= buf + buf_size) {
            report_error("wrong entry format: too long (%i) link name: %s", tmp.link_len, buf);
            return NULL;
        }
        tmp.link[tmp.link_len] = 0;
        unescape_strcpy(tmp.link, tmp.link);
        tmp.link_len = strlen(tmp.link);
    } else {
        tmp.link = "";
    }
    if (tmp.name_len > MAXNAMLEN) {
        report_error("wrong entry format: too long (%i) file name: %s", tmp.name_len, buf);
        return NULL;
    }
    if (tmp.link_len > PATH_MAX) {
        report_error("wrong entry format: too long (%i) link name: %s", tmp.link_len, buf);
        return NULL;
    }
    return create_fs_entry(&tmp);
}

static const char* persistence_version_label = "VERSION=";
static const int persistence_version_curr = 2;

static bool read_entries_from_cache_impl(array/*<fs_entry>*/ *entries, FILE* cache_fp,
        const char *cache_path, char *buf, int buf_size, const char* path, int* persistence_version) {

    if (!fgets(buf, buf_size, cache_fp)) {
        if (feof(cache_fp)) {
            report_error("error reading cache from %s for %s: preliminary EOF\n", cache_path, path);
        } else {
            report_error("error reading cache from %s for %s: %s\n", cache_path, path, strerror(errno));
        }
        return false;
    }

    *persistence_version = 1;
    // in version 1 version string was not written into file
    size_t version_labe_len = strlen(persistence_version_label);
    if (strncmp(buf, persistence_version_label, version_labe_len) == 0) {
        if (!decode_int(buf + version_labe_len, persistence_version)) {
            report_error("error reading cache from %s for %s: wrong version format: %s\n", cache_path, path, buf);
            return false;
        }
        if (*persistence_version != persistence_version_curr) {
            return false;
        }
        if (!fgets(buf, buf_size, cache_fp)) {
            if (feof(cache_fp)) {
                report_error("error reading cache from %s for %s: preliminary EOF\n", cache_path, path);
            } else {
                report_error("error reading cache from %s for %s: %s\n", cache_path, path, strerror(errno));
            }
            return false;
        }
    }

    unescape_strcpy(buf, buf);
    if (strncmp(path, buf, strlen(path)) != 0) {
        report_error("error: first line in cache %s for %s is not '%s', but is '%s'", cache_path, path, path, buf);
            return false;
    }

    bool success = true;
    while (fgets(buf, buf_size, cache_fp)) {
        trace(TRACE_FINEST, "\tread entry: %s", buf);
        if (*buf == '\n' || *buf == 0) {
            trace(TRACE_FINEST, "an empty one; continuing...");
            continue;
        }
        fs_entry *entry = decode_entry_response(buf, buf_size);
        if (entry) {
            array_add(entries, entry);
        } else {
            report_error("error reading entry from cache (%s): %s\n", cache_path, buf);
            success = false;
            break;
        }
    }

    if (success && !feof(cache_fp)) { // we got here because fgets returned NULL, which means EOF or error
        report_error("error reading cache from %s for %s: %s\n", cache_path, path, strerror(errno));
        success = false;
    }

    return success;
}

static bool read_entries_from_cache(array/*<fs_entry>*/ *entries, dirtab_element* el,
        const char* path, int* persistence_version) {

    const char *cache_path = dirtab_get_element_cache_path(el);
    FILE* cache_fp = fopen(cache_path, "r");
    array_init(entries, 100);
    bool success = false;
    if (cache_fp) {
        int buf_size = PATH_MAX + 40;
        char *buf = malloc_wrapper(buf_size);
        success = read_entries_from_cache_impl(entries, cache_fp, cache_path, buf,
                buf_size, path, persistence_version);
        free(buf);
        fclose(cache_fp);
    }
    array_truncate(entries);
    return success;
}

static void set_full_access_check(bool full) {
    change_settings(NULL, &full);
}

static void get_access_c(const char *abspath, const struct stat *stat_buf, char *r, char *w, char *x, const settings_str* settings) {
    if (settings->full_access_check) {
        // NB: access() returns 0 if user can access file
        *r = access(abspath, R_OK) ? '-' : 'r';
        *w = access(abspath, W_OK) ? '-' : 'w';
        *x = access(abspath, X_OK) ? '-' : 'x';
    } else {
        *r = can_read(stat_buf) ?  'r' : '-';
        *w = can_write(stat_buf) ? 'w' : '-';
        *x = can_exec(stat_buf) ?  'x' : '-';
    }
}

static void get_access_e(const char *abspath, const struct stat *stat_buf, fs_entry *entry, const settings_str* settings) {
    if (settings->full_access_check) {
        entry->can_read = !access(abspath, R_OK);
        entry->can_write = !access(abspath, W_OK);
        entry->can_exec = !access(abspath, X_OK);
    } else {
        entry->can_read = can_read(stat_buf);
        entry->can_write = can_write(stat_buf);
        entry->can_exec = can_exec(stat_buf);
    }
}

typedef struct {
    array* array;
    const settings_str* settings;
} entry_creating_visitor_data;

static bool fs_entry_creating_visitor(char* name, struct stat *stat_buf, char* link, const char* abspath, void *data) {
    entry_creating_visitor_data* my_data = (entry_creating_visitor_data*) data;
    fs_entry tmp;
    tmp.name_len = strlen(name);
    tmp.name = name;
    tmp.file_type = mode_to_file_type_char(stat_buf->st_mode);
    tmp.size = stat_buf->st_size;
    tmp.mtime = get_mtime(stat_buf);
    tmp.st_dev = stat_buf->st_dev;
    tmp.st_ino = stat_buf->st_ino;
    bool is_link = S_ISLNK(stat_buf->st_mode);
    if (is_link) {
        // links themselves always has rwx (by definition)
        tmp.can_read = true;
        tmp.can_write = true;
        tmp.can_exec = true;
    } else {
        get_access_e(abspath, stat_buf, &tmp, my_data->settings);
    }
    tmp.link_len = is_link ? strlen(link) : 0;
    tmp.link = is_link ? link : "";
    fs_entry* new_entry = create_fs_entry(&tmp);
    if (new_entry) {
        array_add(my_data->array, new_entry);
    } else {
        report_error("error creating entry for %s\n", abspath);
    }
    return need_to_proceed();
}

static void read_entries_from_dir(array/*<fs_entry>*/ *entries, const char* path, const settings_str* settings) {
    array_init(entries, 100);
    entry_creating_visitor_data data;
    data.array = entries;
    data.settings = settings;
    visit_dir_entries(path, fs_entry_creating_visitor, NULL, settings, &data);
    array_truncate(entries);
}

static bool response_entry_create(buffer response_buf,
        const char *abspath, const char *basename,
        buffer work_buf, const settings_str* settings) {
    struct stat stat_buf;
    if (lstat_wrapper(abspath, &stat_buf, settings) == 0) {

        //int escaped_name_size = escape_strlen(entry->d_name);
        escape_strcpy(work_buf.data, basename);
        char *escaped_name = work_buf.data;
        int escaped_name_size = strlen(escaped_name);
        int work_buf_size = work_buf.size - (escaped_name_size + 1);

        bool link_flag = S_ISLNK(stat_buf.st_mode);

        int escaped_link_size = 0;
        char* escaped_link = "";

        char can_read, can_write, can_exec;
        if (link_flag) {
            can_read = 'r';     // the permissions of symbolic links are never used
            can_write = 'w';    // symbolic links always have rwxrwxrwx permissions
            can_exec = 'x';
            char* link = work_buf.data + escaped_name_size + 1;
            ssize_t sz = readlink(abspath, link, work_buf_size);
            if (sz == -1) {
                report_error("error performing readlink for %s: %s\n", abspath, strerror(errno));
                err_set(errno, "error performing readlink for %s: %s", abspath, err_to_string(errno));
                strcpy(work_buf.data, "?"); // strcpy is safe, data is more than 1 byte anyhow
            } else {
                link[sz] = 0;
                escaped_link_size = escape_strlen(link);
                work_buf_size -= (sz + escaped_link_size + 1);
                if (work_buf_size < 0) {
                    report_error("insufficient space in buffer for %s\n", abspath);
                    err_set(-1, "insufficient space in buffer for %s", abspath);
                    return false;
                }
                escaped_link = link + sz + 1;
                escape_strcpy(escaped_link, link);
            }
        } else {
            get_access_c(abspath, &stat_buf, &can_read, &can_write, &can_exec, settings);
        }

        // name_len name type size mtime access device inode link_len link
        snprintf(response_buf.data, response_buf.size, "%i %s %c %lu %llu %c%c%c %lu %lu %i %s\n",
                utf8_char_count(escaped_name, escaped_name_size),
                escaped_name,
                mode_to_file_type_char(stat_buf.st_mode),
                (unsigned long) stat_buf.st_size,
                get_mtime(&stat_buf),
                can_read, can_write, can_exec,
                (unsigned long) stat_buf.st_dev,
                (unsigned long) stat_buf.st_ino,
                utf8_char_count(escaped_link, escaped_link_size),
                escaped_link);
        return true;
    } else {
        err_set(errno, "error getting lstat for '%s': %s", abspath, err_to_string(errno));
        //report_error("error getting lstat for '%s': %s\n", abspath, err_get_message());
        return false;
    }
}

typedef struct {
    const int request_id;
    buffer response_buf;
    buffer work_buf;
    FILE *cache_fp;
    const settings_str* settings;
} response_ls_data;

static void response_end(int request_id, const char* path);
static bool response_ls_plain_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p);
static bool response_ls_recursive_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p);
static void response_error(int request_id, const char* path, int err_code, const char *err_msg);

static void ls_error_handler(bool dir_itself, const char* path, int err, const char* additional_message, void *data) {
    if (dir_itself && data) {
        response_ls_data *rsp_data = data;
        response_error(rsp_data->request_id, path, err, additional_message);
    } else {
        default_error_handler(dir_itself, path, err, additional_message,data);
    }
}

static void response_ls(int request_id, const char* path, bool recursive, bool inner, const settings_str* settings) {

    if (is_broken_pipe() || !state_get_proceed()) {
        return;
    }

    my_fprintf(STDOUT, "%c %d %li %s\n", (recursive ? FS_RSP_RECURSIVE_LS : FS_RSP_LS),
            request_id, (long) utf8_strlen(path), path);

    buffer response_buf = buffer_alloc(PATH_MAX * 2); // TODO: accurate size calculation
    buffer work_buf = buffer_alloc((PATH_MAX + MAXNAMLEN) * 2 + 2);

    FILE *cache_fp = NULL;
    dirtab_element *el = NULL;
    if (persistence) {
        el = dirtab_get_element(path);
        dirtab_lock(el);
        dirtab_set_watch_state(el, DE_WSTATE_POLL);
        cache_fp = fopen600(dirtab_get_element_cache_path(el), O_WRONLY | O_TRUNC | O_CREAT);
        if (cache_fp) {
            fprintf(cache_fp, "%s%i\n", persistence_version_label, persistence_version_curr);
            escape_strcpy(response_buf.data, path);
            fprintf(cache_fp, "%s\n", response_buf.data);
        } else {
            report_error("error opening cache file for %s: %s\n", path, strerror(errno));
            dirtab_unlock(el);
        }
    }

    response_ls_data data = { request_id, response_buf, work_buf, cache_fp, settings };
    visit_dir_entries(path, response_ls_plain_visitor, ls_error_handler, settings, &data);

    response_end(request_id, path);

    if (el) {
        if (cache_fp) {
            fclose(cache_fp);
        }
        dirtab_set_state(el, DE_STATE_LS_SENT);
        dirtab_unlock(el);
    }

    if (recursive) {
        visit_dir_entries(path, response_ls_recursive_visitor, NULL, settings, &data);
        if (!inner) {
            response_end(request_id, path);
        }
    }

    buffer_free(&response_buf);
    buffer_free(&work_buf);
}

static void response_end(int request_id, const char* path) {
    my_fprintf(STDOUT, "%c %d %li %s\n", FS_RSP_END, request_id, (long) utf8_strlen(path), path);
    my_fflush(STDOUT);
}

static void response_error(int request_id, const char* path, int err_code, const char *err_msg) {
    my_fprintf(STDOUT, "%c %i %i %s: %s: %s\n", FS_RSP_ERROR, request_id, err_code, err_msg, (err_code) ? err_to_string(err_code) : "", path);
    my_fflush(STDOUT);
}

static void response_info(int request_id) {
    my_fprintf(STDOUT, "%c %i %i.%i.%i\n", FS_RSP_SERVER_INFO, request_id, FS_SERVER_MAJOR_VERSION, FS_SERVER_MID_VERSION, FS_SERVER_MINOR_VERSION);
    my_fflush(STDOUT);
}

static void response_delete_on_disconnect(int request_id, const char* path) {
    if (request_id != 0) {
        response_error(request_id, path, 0, "FS_REQ_DELETE_ON_DISCONNECT request should have zero ID!");
    }
    delete_on_exit_list_add(path);
}

static void response_delete(int request_id, const char* path, const settings_str* settings) {

    const char* last_slash = strrchr(path, '/');
    if (!last_slash) {
        response_error(request_id, path, 0, "wrong path");
        return;
    }
    if (last_slash == path) {
        response_error(request_id, path, 0, "won't remove '/'");
        return;
    }
    int parent_len = last_slash - path;
    char parent[parent_len + 1];
    strncpy(parent, path, parent_len);
    parent[parent_len] = 0;
    char canonical_parent[PATH_MAX];
    if (!realpath(parent, canonical_parent)) {
        response_error(request_id, path, errno, "can't resolve parent canonical path");
        return;
    }

    struct stat stat_buf;
    if (lstat_wrapper(path, &stat_buf, settings) == 0) {
        if (S_ISDIR(stat_buf.st_mode)) {
            if (!clean_dir(path)) {
                response_error(request_id, path, errno, "can't remote directory content");
                return;
            }
            if (rmdir(path)) {
                response_error(request_id, path, errno, "can't remove directory");
            }
        } else {
            if (unlink(path)) {
                response_error(request_id, path, errno, "can't remove file");
                return;
            }
        }
    } else {
        response_error(request_id, path, errno, "error getting stat");
        return;
    }

    // the file or directory successfully removed
    response_ls(request_id, canonical_parent, false, false, settings);
}

static bool response_ls_plain_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p) {

    response_ls_data *data = p;
    //trace("\tentry: '%s'\n", entry->d_name);
    // on NFS entry->d_name may contain '/' or even be absolute!
    // for example, "/ws" directory can contain
    // "bb-11u1", /ws/bb-11u1/packages" and "bb-11u1/packages" entries!
    // TODO: investigate how to process this properly
    // for now just ignoring such entries
    if (strchr(name, '/')) {
        report_error("skipping entry %s\n", name);
        return need_to_proceed();
    }
    if (response_entry_create(data->response_buf, child_abspath, name, data->work_buf, data->settings)) {
        my_fprintf(STDOUT, "%c %d %s", FS_RSP_ENTRY, data->request_id, data->response_buf.data);
        if (data->cache_fp) {
            fprintf(data->cache_fp, "%s", data->response_buf.data); // trailing '\n' already there, added by form_entry_response
        }
    } else {
        report_error("error formatting response for '%s'\n", child_abspath);
    }

    return need_to_proceed();
}

static bool response_ls_recursive_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p) {
    response_ls_data *data = p;
    if (S_ISDIR(stat_buf->st_mode)) {
        response_ls(data->request_id, child_abspath, true, true, data->settings);
    }
    return need_to_proceed();
}


static void response_stat(int request_id, const char* path, const settings_str* settings) {
    struct stat stat_buf;
    if (stat_wrapper(path, &stat_buf, settings) == 0) {
        int buf_size = MAXNAMLEN * 2 + 80; // *2 because of escaping. TODO: accurate size calculation
        char* escaped_name = malloc_wrapper(buf_size);
        const char* basename = get_basename(path);
        escape_strcpy(escaped_name, basename);
        int escaped_name_size = strlen(escaped_name);

        // in contrary to lstat, we call access() even for links)
        char can_read, can_write, can_exec;
        get_access_c(path, &stat_buf, &can_read, &can_write, &can_exec, settings);

        // name_len name type size mtime access device inode link_len link
        my_fprintf(STDOUT, "%c %i %i %s %c %lu %llu %c%c%c %lu %lu %i %s\n",
                FS_RSP_ENTRY,
                request_id,
                utf8_char_count(escaped_name, escaped_name_size),
                escaped_name,
                mode_to_file_type_char(stat_buf.st_mode),
                (unsigned long) stat_buf.st_size,
                get_mtime(&stat_buf),
                can_read, can_write, can_exec,
                (unsigned long) stat_buf.st_dev,
                (unsigned long) stat_buf.st_ino,
                0, "");
        my_fflush(STDOUT);
        free(escaped_name);
    }  else {
        int err_code = errno;
        const char* strerr = err_to_string(err_code);
        my_fprintf(STDOUT, "%c %i %i %s: %s\n", FS_RSP_ERROR, request_id, err_code, strerr, path);
        my_fflush(STDOUT);
    }
}

static bool copy_symlink(const char* path, const char* path2,int id);
static bool copy_plain_file(const char* path, const char* path2, int id);
static bool copy_dir(const char* path, const char* path2, int id);

static void response_copy(const fs_request* request, const settings_str* settings) {

    // if file exists, return error
    struct stat stat_buf;
    if (lstat_wrapper(request->path2, &stat_buf, settings) == 0) {
        response_error(request->id, request->path2, 0, "file already exists");
        response_end(request->id, request->path2);
        return;
    }
    if (lstat_wrapper(request->path, &stat_buf, settings) == -1) {
        response_error(request->id, request->path, errno, err_to_string(errno));
        response_end(request->id, request->path2);
        return;
    }
    bool success = false;
    if (S_ISDIR(stat_buf.st_mode)) {
        success = copy_dir(request->path, request->path2, request->id);
    } else if (S_ISREG(stat_buf.st_mode)) {
        success = copy_plain_file(request->path, request->path2, request->id);
    } else if (S_ISLNK(stat_buf.st_mode)) {
        success = copy_symlink(request->path, request->path2, request->id);
    } else {
        response_error(request->id, request->path2, 0, "don't know how to copy a special file");
    }
    if (success) {
        char *dst_parent = strdup(request->path2);
        char *last_slash = strrchr(dst_parent, '/');
        if (last_slash) {
            *last_slash = 0;
            response_ls(request->id, dst_parent, false, true, settings);
            // response_end() is called from response_ls
        } else {
            response_error(request->id, dst_parent, 0, "path does not contain '/'");
            response_end(request->id, request->path2);
        }
        free(dst_parent);
    } else {
        response_end(request->id, request->path2);
    }
}

typedef struct {
    bool success;
    int id;
    char* dst_path_base_end;
    int dst_max_append_size;
    char dst_path[PATH_MAX+1];
} copy_dir_struct;

static bool copy_dir_visitor(char* name, struct stat *stat_buf, char* link, const char* child_abspath, void *p) {
    copy_dir_struct *cds = p;
    strncpy_w_zero(cds->dst_path_base_end, name, cds->dst_max_append_size);

    bool success = false;
    if (S_ISDIR(stat_buf->st_mode)) {
        success = copy_dir(child_abspath, cds->dst_path, cds->id);
    } else if (S_ISREG(stat_buf->st_mode)) {
        success = copy_plain_file(child_abspath, cds->dst_path, cds->id);
    } else if (S_ISLNK(stat_buf->st_mode)) {
        success = copy_symlink(child_abspath, cds->dst_path, cds->id);
    } else {
        response_error(cds->id, child_abspath, 0, "don't know how to copy a special file");
    }
    return success;
}

static bool copy_dir(const char* path, const char* path2, int id) {
    if (mkdir(path2, 0700) != 0) {
        response_error(id, path2, errno, err_to_string(errno));
        return false;
    }
    copy_dir_struct* cds = malloc_wrapper(sizeof(copy_dir_struct));
    cds->success = true;
    cds->id = id;

    int len = strlen(path2);
    len = (len > PATH_MAX) ? PATH_MAX : len;
    strncpy(cds->dst_path, path2, len);
    cds->dst_path[len] = '/';
    cds->dst_path_base_end = cds->dst_path + len + 1;
    cds->dst_max_append_size = PATH_MAX - len - 1;

    visit_dir_entries(path, copy_dir_visitor, NULL, NULL, cds);

    bool success = cds->success;
    free(cds);
    return success;
}

static bool copy_plain_file(const char* path, const char* path2, int id) {

    FILE *src = NULL;
    FILE *dst = NULL;
    char *buf = NULL;

    src = fopen(path, "r");
    if (!src) {
        response_error(id, path, errno, err_to_string(errno));
        return false;
    }

    dst = fopen(path2, "w");
    if (!dst) {
        response_error(id, path2, errno, err_to_string(errno));
        fclose(src);
        return false;
    }

    bool success = false;

    const int buf_size = 16 * 1024;
    buf = malloc_wrapper(buf_size);
    if (buf) {
        int read_cnt;
        while ((read_cnt = fread(buf, 1, buf_size, src))) {
            size_t write_cnt = fwrite(buf, 1, read_cnt, dst);
            if (write_cnt != read_cnt) {
                int errcode = ferror(dst);
                if (errcode) {
                    response_error(id, path2, errcode, err_to_string(errcode));
                } else {
                    response_error(id, path2, 0, "error writing");
                }
                break;
            }
        }
        if (feof(src)) {
            success = true;
        } else {
            int errcode = ferror(src);
            if (errcode) {
                response_error(id, path, errcode, err_to_string(errcode));
            } else {
                response_error(id, path, 0, "error reading");
            }
        }
        free(buf);
    } else {
        response_error(id, path2, 0, "not enough memory");
    }

    fclose(src);
    fclose(dst);

    return success;
}

static bool copy_symlink(const char* path, const char* path2, int id) {
    bool success = false;
    const int buf_size = PATH_MAX;
    char *link_dst = malloc_wrapper(buf_size);
    ssize_t sz = readlink (path, link_dst, buf_size);
    if (sz == -1) {
        response_error(id, path, errno, err_to_string(errno));
    } else {
        link_dst[sz] = 0;
        errno = 0;
        if (symlink(link_dst, path2) == -1) {
            response_error(id, path2, errno, err_to_string(errno));
        } else {
           success = true;
        }
    }
    free(link_dst);
    return success;
}

static void response_lstat(int request_id, const char* path, const settings_str* settings) {
    buffer response_buf = buffer_alloc(PATH_MAX * 2); // *2 because of escaping. TODO: accurate size calculation
    buffer work_buf = buffer_alloc((PATH_MAX + MAXNAMLEN) * 2 + 2);
    const char* basename = get_basename(path);
    if (response_entry_create(response_buf, path, basename, work_buf, settings)) {
        my_fprintf(STDOUT, "%c %d %s", FS_RSP_ENTRY, request_id, response_buf.data);
        my_fflush(STDOUT);
//        if (cache_fp) {
//            fprintf(cache_fp, "%s",response_buf); // trailing '\n' already there, added by form_entry_response
//        }
    } else {
        //TODO: pass error message from response_entry_create
        my_fprintf(STDOUT, "%c %i %i %s\n", FS_RSP_ERROR, request_id, err_get_code(), err_get_message());
        my_fflush(STDOUT);
    }
    buffer_free(&response_buf);
    buffer_free(&work_buf);
}

static void response_move(const fs_request* request, const settings_str* settings) {
    // check whether it is directory and print clear error message
    // (copy_plain_file will print unclear "Operation not permitted" in this case)
    struct stat stat_buf;
    if (lstat_wrapper(request->path, &stat_buf, settings) == -1) {
        response_error(request->id, request->path, errno, err_to_string(errno));
        return;
    }
    if (S_ISDIR(stat_buf.st_mode)) {
        response_error(request->id, request->path2, 0, "can not move directory");
        return;
    }
    if (copy_plain_file(request->path, request->path2, request->id)) {
        if (unlink(request->path)) {
            response_error(request->id, request->path, errno, "can't remove file");
            return;
        }
        response_lstat(request->id, request->path2, settings);
    }
    // in the case of failure, copy_plain_file() already reported it)
}

static void response_add_or_remove_watch(int request_id, const char* path, bool add) {
    dirtab_element *el = dirtab_get_element(path);
    dirtab_lock(el);
    dirtab_set_watch_state(el, add ? DE_WSTATE_POLL : DE_WSTATE_NONE);
    dirtab_set_state(el, DE_STATE_INITIAL);
    dirtab_unlock(el);
}

static int entry_comparator(const void *element1, const void *element2) {
    const fs_entry *e1 = *((fs_entry**) element1);
    const fs_entry *e2 = *((fs_entry**) element2);
    int res = strcmp(e1->name, e2->name);
    return res;
}

static bool entries_differ(fs_entry *new_entry, fs_entry *old_entry, int persistence_version, const char* path) {
    if (new_entry->name_len != old_entry->name_len) {
        trace(TRACE_FINE, "refresh manager: names differ (1) in directory %s: %s vs %s\n", path, new_entry->name, old_entry->name);
        return true;
    }
    if (strcmp(new_entry->name, old_entry->name) != 0) {
        trace(TRACE_FINE, "refresh manager: names differ (2) in directory %s: %s vs %s\n", path, new_entry->name, old_entry->name);
        return true;
    }
    if (new_entry->file_type != old_entry->file_type) {
        trace(TRACE_FINE, "refresh manager: file types differ for %s/%s: %c vs %c\n", path, new_entry->name, new_entry->file_type, old_entry->file_type);
        return true;
    }
    // if links, then check links
    if (new_entry->file_type == FILETYPE_LNK) {
        if (new_entry->link_len != old_entry->link_len) {
            trace(TRACE_FINE, "refresh manager: links differ (1) for %s/%s: %s vs %s\n", path, new_entry->name, new_entry->link, old_entry->link);
            return true;
        }
        if (strcmp(new_entry->link, old_entry->link) != 0) {
            trace(TRACE_FINE, "refresh manager: links differ (2) for %s/%s: %s vs %s\n", path, new_entry->name, new_entry->link, old_entry->link);
            return true;
        }
    }
    // names, modes and link targets are same
    if (new_entry->file_type == FILETYPE_REG) {
        if (new_entry->size != old_entry->size) {
            trace(TRACE_FINE, "refresh manager: sizes differ for %s/%s: %d vs %d\n", path, new_entry->name, new_entry->size, old_entry->size);
            return true;
        }
        if (new_entry->mtime != old_entry->mtime) {
            trace(TRACE_FINE, "refresh manager: times differ for %s/%s: %lld vs %lld\n", path, new_entry->name, new_entry->mtime, old_entry->mtime);
            return true;
        }
    }
    if (persistence_version > 1) {
        if (new_entry->can_read != old_entry->can_read) {
            trace(TRACE_FINE, "refresh manager: can_read differ for %s/%s: %c vs %c\n", path, new_entry->name,
                    new_entry->can_read ? 'T' : 'F', old_entry->can_read ? 'T' : 'F');
            return true;
        }
        if (new_entry->can_write != old_entry->can_write) {
            trace(TRACE_FINE, "refresh manager: can_write differ for %s/%s: %c vs %c\n", path, new_entry->name,
                    new_entry->can_write ? 'T' : 'F', old_entry->can_write ? 'T' : 'F');
            return true;
        }
        if (new_entry->can_exec != old_entry->can_exec) {
            trace(TRACE_FINE, "refresh manager: can_exec differ for %s/%s: %c vs %c\n", path, new_entry->name,
                    new_entry->can_exec ? 'T' : 'F', old_entry->can_exec ? 'T' : 'F');
            return true;
        }
        if (new_entry->st_dev != old_entry->st_dev) {
            trace(TRACE_FINE, "refresh manager: st_dev differ for %s/%s: %lld vs %lld\n", path, new_entry->name,
                    (long long) new_entry->st_dev, (long long) old_entry->st_dev);
            return true;
        }
        if (new_entry->st_ino != old_entry->st_ino) {
            trace(TRACE_FINE, "refresh manager: st_ino differ for %s/%s: %lld vs %lld\n", path, new_entry->name,
                    (long long) new_entry->st_ino, (long long) old_entry->st_ino);
            return true;
        }
    }
    return false;
}

typedef struct {
    fs_request *request;
    const settings_str* settings;
} refresh_visitor_data;

static bool refresh_visitor(const char* path, int index, dirtab_element* el, void *data) {
    refresh_visitor_data *my_data = data;
    fs_request *request = my_data->request;
    if (is_prohibited(path)) {
        trace(TRACE_FINER, "refresh manager: skipping %s\n", path);
        return need_to_proceed();
    }
    if (request) {
        if (!is_subdir(path, request->path)) {
            trace(TRACE_FINER, "refresh manager: skipping %s\n", path);
            return need_to_proceed();
        }
    }
    dirtab_lock(el);
    if (!request && dirtab_get_watch_state(el) != DE_WSTATE_POLL) {
        dirtab_unlock(el);
        trace(TRACE_FINER, "refresh manager: not polling %s\n", path);
        return need_to_proceed();
    }
    trace(TRACE_FINER, "refresh manager: visiting %s\n", path);

    array/*<fs_entry>*/ old_entries;
    array/*<fs_entry>*/ new_entries;
    dirtab_state state = dirtab_get_state(el);
    if (state == DE_STATE_REMOVED) {
        dirtab_unlock(el);
        trace(TRACE_FINER, "refresh manager: already marked as removed %s\n", path);
        return need_to_proceed();
    }
    if (!dir_exists(path)) { // that's OK to call lstat here: few lines below we'll even read this dir!
        dirtab_set_state(el, DE_STATE_REMOVED);
        dirtab_unlock(el);
        trace(TRACE_FINER, "refresh manager: does not exist, marking as removed %s\n", path);
        return need_to_proceed();
    }
    if (!request && state == DE_STATE_REFRESH_SENT) {
        dirtab_unlock(el);
        trace(TRACE_FINER, "refresh notification already sent for %s\n", path);
        return need_to_proceed();
    }

    int persistence_version = -1;
    bool success = read_entries_from_cache(&old_entries, el, path, &persistence_version);
    bool differs;
    if (success) {
        read_entries_from_dir(&new_entries, path, my_data->settings);
        array_qsort(&old_entries, entry_comparator);
        array_qsort(&new_entries, entry_comparator);
        differs = false;
    } else {
        array_init(&new_entries, 4);
        if (persistence_version == -1 ||  persistence_version == persistence_version_curr) {
            // -1 means that there was error reading,
            // version equals to current => there was an unexpected error => report
            // version != current => just old cache version found => do not report
            report_error("error refreshing %s: error reading cache\n", path);
        } else {
            trace(TRACE_FINEST, "invalid cache (incorrect version: %d) for %s\n", persistence_version, path);
        }
        differs = true;
    }

    if (!differs) {
        differs = array_size(&new_entries) != array_size(&old_entries);
    }
    if (!differs) {
        for (int i = 0; i < new_entries.size; i++) {
            fs_entry *new_entry = array_get(&new_entries, i);
            fs_entry *old_entry = array_get(&old_entries, i);
            if (entries_differ(new_entry, old_entry, persistence_version, path)) {
                differs = true;
                break;
            }
        }
    }

    if (differs) {
        trace(TRACE_INFO, "refresh manager: sending notification for %s\n", path);
        // trailing '\n' already there, added by form_entry_response
        my_fprintf(STDOUT, "%c %d %li %s\n", FS_RSP_CHANGE,
                request ? request->id : 0, (long) utf8_strlen(path), path);
        my_fflush(STDOUT);
        dirtab_set_state(el, DE_STATE_REFRESH_SENT);
    }
    dirtab_unlock(el);
    array_free(&old_entries);
    array_free(&new_entries);
    return need_to_proceed();
}

static void thread_init() {
    err_init();    
    sigset_t set;
    sigfillset(&set);
    sigdelset(&set, SIGUSR1);
    int res = pthread_sigmask(SIG_BLOCK, &set, NULL);
    if (res) {
        report_error("error blocking signals for thread: %s\n", strerror(res));
    }
}
static void thread_shutdown() {
    err_shutdown();
}

static void refresh_cycle_impl(fs_request* request, const settings_str* settings) {
    stopwatch_start();
    if (request && request->id) { // zero id means nobody is waiting, so no need for header and end marker
        my_fprintf(STDOUT, "%c %d %li %s\n", FS_RSP_REFRESH, request->id, (long) utf8_strlen(request->path), request->path);
    }
    refresh_visitor_data data;
    data.request = request;
    data.settings = settings;
    dirtab_visit(refresh_visitor, &data);
    if (request && request->id) { // zero id means nobody is waiting, so no need for header and end marker
        response_end(request->id, request->path);
    }
    stopwatch_stop(TRACE_FINE, "refresh cycle");
}

static void refresh_cycle(fs_request* request, const settings_str* settings) {

    dirtab_flush(); // TODO: find the appropriate place
    const char* path = request ? request->path : "/";
    dirtab_element* el = dirtab_get_element(path);

    // analyze and set refresh_state;
    dirtab_lock(el);
    dirtab_refresh_state refresh_state = dirtab_get_refresh_state(el);
    if (refresh_state == DRS_REFRESHING) {
        // already refreshing => set pending flag,
        // so the thread that refreshes will repeat once again
        trace(TRACE_FINEST, "refresh[1] %s already refreshing: setting pending flag\n", path);
        dirtab_set_refresh_state(el, DRS_PENDING_REFRESH);
        dirtab_unlock(el);
        return;
    } else if (refresh_state == DRS_PENDING_REFRESH) {
        // nothing: the thread that refreshes will repeat once again
        trace(TRACE_FINEST, "refresh[1] %s already pending: nothing to do\n", path);
        dirtab_unlock(el);
        return;
    } else {
        soft_assert(refresh_state == DRS_NONE, "unexpected refresh_state for %s : %d\n", path, refresh_state);
        dirtab_set_refresh_state(el, DRS_REFRESHING);
        dirtab_unlock(el);
    }

    while (true) {
        trace(TRACE_FINEST, "refreshing %s...\n", path);
        refresh_cycle_impl(request, settings);
        trace(TRACE_FINEST, "refreshing %s completed\n", path);

        dirtab_lock(el);
        dirtab_refresh_state refresh_state = dirtab_get_refresh_state(el);
        if (refresh_state == DRS_REFRESHING) {
            // no new refresh request has come => just exit
            trace(TRACE_FINEST, "refresh[2] %s no new request => exiting\n", path);
            dirtab_set_refresh_state(el, DRS_NONE);
            dirtab_unlock(el);
            return;
        } else if (refresh_state == DRS_PENDING_REFRESH) {
            // while we refreshed, new request has come
            trace(TRACE_FINEST, "refresh[2] %s pending request => once more\n", path);
            dirtab_set_refresh_state(el, DRS_REFRESHING);
            dirtab_unlock(el);
            continue;
        } else {
            soft_assert(false, "unexpected refresh_state for %s : %d, should be either %d or %d\n",
                    path, refresh_state, DRS_REFRESHING, DRS_PENDING_REFRESH);
            dirtab_unlock(el);
            return;
        }
    }
}

static void *refresh_loop(void *data) {
    trace(TRACE_INFO, "Refresh manager started; sleep interval is %d\n", refresh_sleep);
    thread_init();
    int pass = 0;
    while (!is_broken_pipe() && dirtab_is_empty() && state_get_proceed()) { //TODO: replace with notification?
        sleep(refresh_sleep ? refresh_sleep : 2);
    }
    while (need_to_proceed()) {
        pass++;
        trace(TRACE_FINE, "refresh manager, pass %d\n", pass);
        settings_str settings;
        clone_global_settings(&settings);
        refresh_cycle(NULL, &settings);
        if (refresh_sleep) {
            sleep(refresh_sleep);
        }
    }
    trace(TRACE_INFO, "Refresh manager stopped\n");
    thread_shutdown();
    return NULL;
}

static void response_refresh(fs_request* request, const settings_str* settings) {
    refresh_cycle(request, settings);
}

static void process_option(const char* option, const char* value) {
    //trace(TRACE_FINEST, "Processing option %s=%s\n", option, value);
    if (strcmp(option, "access") == 0) {
        if (strcmp(value, "fast") == 0) {
            set_full_access_check(false);
        } else if (strcmp(value, "full") == 0) {
            set_full_access_check(true);
        } else {
            report_error("Unexpected option value: %s=%s\n", option, value);
        }
    } else if (strcmp(option, "dirs-forbidden-to-stat") == 0) {
        set_dirs_forbidden_to_stat(value);        
    } else {
        report_error("Unexpected option key: %s\n", option);
    }
}

static void response_options(fs_request* request) {
    char* options = request->data;
    // request->data contains a zero-terminated string
    // we own it, so we are going to modify it in place
    trace(TRACE_FINEST, "Processing options \"%s\"\n", options);
    char* option = options;
    char* value = NULL;
    char* p = options;
    while (true) {
        if (*p ==',' || *p == '\0') {
            bool last = (*p == '\0');
            if (value) {
                *p = '\0';
                process_option(option, value);
            } else {
                report_error("Wrong options format: %s\n", options);
            }
            if (last) {
                break;
            }
            option = p + 1;
            value = NULL;
        } else if (*p == '=') {
            *p = '\0';
            value = p+1;
        }
        p++;
    };
}

static void response_help() {
    my_fprintf(STDOUT, "Help on request kinds\n");
    #define help_req_kind(kind) my_fprintf(STDOUT, "%c - %s\n", kind, #kind)
    help_req_kind(FS_REQ_LS);
    help_req_kind(FS_REQ_RECURSIVE_LS);
    help_req_kind(FS_REQ_STAT);
    help_req_kind(FS_REQ_LSTAT);
    help_req_kind(FS_REQ_COPY);
    help_req_kind(FS_REQ_MOVE);
    help_req_kind(FS_REQ_QUIT);
    help_req_kind(FS_REQ_SLEEP);
    help_req_kind(FS_REQ_ADD_WATCH);
    help_req_kind(FS_REQ_REMOVE_WATCH);
    help_req_kind(FS_REQ_REFRESH);
    help_req_kind(FS_REQ_DELETE);
    help_req_kind(FS_REQ_DELETE_ON_DISCONNECT);
    help_req_kind(FS_REQ_SERVER_INFO);
    help_req_kind(FS_REQ_OPTION);
    help_req_kind(FS_REQ_HELP);
    #undef  help_req_kind
}

static void process_request(fs_request* request) {
    settings_str settings;
    clone_global_settings(&settings);
    switch (request->kind) {
        case FS_REQ_DELETE:
            response_delete(request->id, request->path, &settings);
            break;
        case FS_REQ_DELETE_ON_DISCONNECT:
            response_delete_on_disconnect(request->id, request->path);
            break;
        case FS_REQ_SERVER_INFO:
            response_info(request->id);
            break;
        case FS_REQ_LS:
            response_ls(request->id, request->path, false, false, &settings);
            break;
        case FS_REQ_RECURSIVE_LS:
            response_ls(request->id, request->path, true, false, &settings);
            break;
        case FS_REQ_STAT:
            response_stat(request->id, request->path, &settings);
            break;
        case FS_REQ_LSTAT:
            response_lstat(request->id, request->path, &settings);
            break;
        case FS_REQ_ADD_WATCH:
            response_add_or_remove_watch(request->id, request->path, true);
            break;
        case FS_REQ_REMOVE_WATCH:
            response_add_or_remove_watch(request->id, request->path, false);
            break;
        case FS_REQ_REFRESH:
            response_refresh(request, &settings);
            break;
        case FS_REQ_COPY:
            response_copy(request, &settings);
            break;
        case FS_REQ_MOVE:
            response_move(request, &settings);
            break;
        case FS_REQ_HELP:
            response_help();
            break;
        case FS_REQ_OPTION:
            response_options(request);
            break;
        default:
            report_error("unexpected mode: '%c'\n", request->kind);
    }
}

static void *rp_loop(void *data) {
    thread_info *ti = (thread_info*) data;
    trace(TRACE_FINE, "Thread #%d started\n", ti->no);
    thread_init();
    while (!is_broken_pipe()) {
        fs_request* request = blocking_queue_poll(&req_queue);
        if (request) {
            trace(TRACE_FINE, "thread[%d] request #%d sz=%d kind=%c len=%d path=%s\n",
                    ti->no, request->id, request->size, request->kind, request->len, request->path);
            increment_busy_threads();
            process_request(request);
            free(request);
            decrement_busy_threads();
        } else {
            if (!state_get_proceed()) {
                break;
            }
        }
    }
    trace(TRACE_FINE    , "Thread #%d done\n", ti->no);
    thread_shutdown();
    return NULL;
}

static void write_pid(int fd) {
    char buf[40];
    int sz = snprintf(buf, sizeof buf, "PID=%lu\n", (unsigned long) getpid());
    if (sz > 0) {
        write(fd, buf, sz);
    }
}

static unsigned long read_pid(int fd) {
    char buf[40];
    ssize_t sz = read(fd, buf, sizeof buf);
    if (sz > 0) {
        buf[sz] = 0;
        if (strncmp("PID=", buf, 4) == 0) {
            unsigned long pid = atol(buf + 4);
            return pid;
        }
    }
    return 0;
}

static void lock_or_unlock(bool lock) {
    if (!persistence) {
        return;
    }
    const char* lock_file_name = "lock";
    static int lock_fd = -1;
    if (lock) {
        lock_fd = open(lock_file_name, O_RDWR | O_CREAT, 0600);
        if (lock_fd < 0) {
            report_error("error opening lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_OPENING_LOCK_FILE);
        }
        if(lockf(lock_fd, F_TLOCK, 0)) {
            int lockf_errno = errno;
            unsigned long pid = read_pid(lock_fd);
            if (pid != 0) {
                if (kill_locker_and_wait && (locker_pid_to_kill == 0 || locker_pid_to_kill == pid)) {
                    // first try SIGTERM, then SIGKILL
                    for (int round = 0; round < 2; round++) {
                        int signal = (round == 0) ? SIGTERM : SIGKILL;
                        trace(TRACE_INFO, "Killing pid=%lu via sending signal %i...\n", pid, signal);
                        kill(pid, signal);
                        int cnt = MAX(kill_locker_and_wait / 10, 1);
                        for (int i = 0; i < cnt; i++) {
                            if (lockf(lock_fd, F_TLOCK, 0)) {
                                lockf_errno = errno;
                                trace(TRACE_FINE, "waiting for pid=%lu to terminate...\n", pid);
                                usleep(10000);
                            } else {
                                write_pid(lock_fd);
                                return;
                            }
                        }
                    }
                }
                report_error("lock file already locked by PID=%lu %s/%s: %s\n", pid, dirtab_get_basedir(), lock_file_name, strerror(lockf_errno));
                exit(FAILURE_LOCKING_LOCK_FILE);
            }
            report_error("error locking lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(lockf_errno));
            exit(FAILURE_LOCKING_LOCK_FILE);
        } else {
            write_pid(lock_fd);
        }
    } else {
        if (lockf(lock_fd, F_ULOCK, 0)) {
            report_error("error unlocking lock file %s/%s: %s\n", dirtab_get_basedir(), lock_file_name, strerror(errno));
            exit(FAILURE_LOCKING_LOCK_FILE);
        }
        close(lock_fd);
    }
}

static void exit_function() {
    dirtab_flush();
    lock_or_unlock(false);
}

static void main_loop() {
    //TODO: handshake with version
    int buf_size = 256 + 2 * (PATH_MAX * 2);
    char *raw_req_buffer = malloc_wrapper(buf_size);
    char *req_buffer = malloc_wrapper(buf_size);
    while(!is_broken_pipe() &&fgets(raw_req_buffer, buf_size, stdin)) {
        trace(TRACE_FINE, "request: %s", raw_req_buffer); // no LF since buffer ends it anyhow
        log_print(raw_req_buffer);
        fs_request* request = decode_request(raw_req_buffer, (fs_request*) req_buffer, buf_size);
        if (request) {
            trace(TRACE_FINEST, "decoded request #%d sz=%d kind=%c len=%d path=%s\n", request->id, request->size, request->kind, request->len, request->path);
            if (request->kind == FS_REQ_QUIT) {
                break;
            }
            if (request->kind == FS_REQ_SLEEP) {
                int interval = 0;
                for (int i = 0; i < request->len; i++) {
                    char c = request->path[i];
                    if (isdigit(c)) {
                        interval = (interval*10) + (c - '0');
                    } else {
                        break;
                    }
                }
                if (interval) {
                    my_fprintf(STDERR, "fs_server: sleeping %i seconds\n", interval);
                    sleep(interval);
                    my_fprintf(STDERR, "fs_server: awoke\n");
                }
                continue;
            }
            if (rp_thread_count > 1) {
                int busy = get_busy_threads();
                if (busy >= rp_thread_count && rp_thread_count < MAX_THREAD_COUNT) {
                    int curr_thread = rp_thread_count++;
                    trace(TRACE_FINE, "Starting thread #%d...\n", curr_thread);
                    rp_threads[curr_thread].no = curr_thread;
                    pthread_create(&rp_threads[curr_thread].id, NULL, &rp_loop, &rp_threads[curr_thread]);
                }
                fs_request* new_request = malloc_wrapper(request->size);
                clone_request(new_request, request);
                blocking_queue_add(&req_queue, new_request);
            } else {
                process_request(request);
            }
       } else {
            report_error("incorrect request: %s", raw_req_buffer);
       }
    }
    free(req_buffer);
    free(raw_req_buffer);
}

static void usage(char* argv[]) {
    char *prog_name = strrchr(argv[0], '/');
    if (prog_name) {
        prog_name++;
    }
    my_fprintf(STDERR,
            "%s %i.%i.%i  [built %s %s]\n"
            "Usage: %s [-t nthreads] [-v] [-p] [-r]\n"
            "   -t <nthreads> response processing threads count (default is %d)\n"
            "   -p log responses into persisnence\n"
            "   -r <nsec>  set refresh ON and sets refresh interval in seconds\n"
            "   -e [<file>]  redirect trace and error messages to file; by default file is ${cache}/stderr.txt\n"
            "   -R <i|e>  refresh mode: i - implicit, e - explicit\n"
            "   -v <verbose-level>: print trace messages\n"
            "   -l log all requests into log file\n"
            "   -s statistics: print some statistics output to stderr\n"
            "   -d persistence directory: where to log responses (valid only if -p is set)\n"
            "   -c cleanup persistence upon startup\n"
            "   -K <PID>:<msec>|<msec> kill another fs_server process that locks the cache via sending SIGTERM and\n"
            "      wait maximum <msec> microseconds until it releases the lock\n"
            "      <msec> should be less than 1000000\n"
            "      if <PID> is specified then only the process with this PID can be killed\n"
            , prog_name ? prog_name : argv[0], FS_SERVER_MAJOR_VERSION, FS_SERVER_MID_VERSION, FS_SERVER_MINOR_VERSION, __DATE__, __TIME__
            , prog_name ? prog_name : argv[0], DEFAULT_THREAD_COUNT);
}

void process_options(int argc, char* argv[]) {
    int opt;
    int new_thread_count, new_refresh_sleep, new_trace_level;
    TraceLevel default_trace_leve = TRACE_INFO;
    while ((opt = getopt(argc, argv, "r:pv:t:lsd:cR:K:e:")) != -1) {
        switch (opt) {
            case 'R':
                if (optarg) {
                    if (*optarg == 'i') {
                        refresh_explicit = false;
                    } else if (*optarg == 'e') {
                        refresh_explicit = true;
                    } else {
                        report_error("incorrect value of -R flag: %s\n", optarg);
                        usage(argv);
                        exit(WRONG_ARGUMENT);
                    }
                }
                break;
            case 'd':
                if (optarg) {
                    dirtab_set_persistence_dir(optarg);
                }
                break;
            case 'e':
                redirect_err_flag = true;
                if (optarg) {
                    error_log = strdup(optarg);
                }
                break;
            case 'c':
                clear_persistence = true;
                break;
            case 's':
                statistics = true;
                break;
            case 'r':
                refresh  = true;
                new_refresh_sleep = atoi(optarg);
                if (new_refresh_sleep >= 0) {
                    refresh_sleep = new_refresh_sleep;
                }
                break;
            case 'l':
                log_flag = true;
                break;
            case 'p':
                persistence  = true;
                break;
            case 'v':
                new_trace_level = atoi(optarg);
                switch (new_trace_level) {
                    case TRACE_NONE:
                    case TRACE_INFO:
                    case TRACE_FINE:
                    case TRACE_FINER:
                    case TRACE_FINEST:
                        set_trace(new_trace_level);
                        break;
                    default:
                        report_error("incorrect value of -v flag: %d. Defaulting to %d\n",
                                new_trace_level, new_trace_level > TRACE_FINEST ? TRACE_FINEST : default_trace_leve);
                        set_trace(new_trace_level);
                        break;
                }
                break;
            case 't':
                new_thread_count = atoi(optarg);
                if (new_thread_count > 0) {
                    if (new_thread_count > MAX_THREAD_COUNT) {
                        report_error("incorrect value of -t flag: %d. Should not exceed %d.\n", new_thread_count, MAX_THREAD_COUNT);
                        rp_thread_count = MAX_THREAD_COUNT;
                    } else {
                        rp_thread_count = new_thread_count;
                    }
                }
                break;
            case 'K':
                kill_locker_and_wait = 5;
                if (optarg) {
                    // can be in the form <msec> or <PID>:<msec>
                    char* p = optarg;
                    // try finding ':'
                    while (*p && *p != ':') {
                        p++;
                    }
                    // *p is either ':' or '\0'
                    if (p) {
                        // *p is ':'
                        *p++ = 0;
                        locker_pid_to_kill = atol(optarg);
                        kill_locker_and_wait = atoi(p);
                    } else {
                        locker_pid_to_kill = 0;
                        kill_locker_and_wait = atoi(optarg);
                    }
                }
                break;
            default: /* '?' */
                usage(argv);
                exit(WRONG_ARGUMENT);
                break;
        }
    }
    if (refresh && !persistence) {
        report_error("incorrect parameters combination: refresh without persistence does not work\n");
        usage(argv);
        exit(WRONG_ARGUMENT);
    }
}

static bool print_visitor(const char* path, int index, dirtab_element* el, void *data) {
    trace(TRACE_INFO, "%d %s\n", index, path);
    return true;
}

static void sigaction_wrapper(int sig, const struct sigaction* new_action, struct sigaction *old_action) {
    int rc = sigaction(sig, new_action, old_action);
    if (rc) {
        report_error("error setting signal handler\n");
        exit(FAILURE_SETTING_SIGNAL_HANDLER);
    }
}

static void shutdown();

static void signal_handler(int signal) {
    trace(TRACE_INFO, "exiting by signal %s (%d)\n", signal_name(signal), signal);
    shutdown();
}

static void signal_empty_handler(int signal) {
    trace(TRACE_FINE, "got signal %s (%d)\n", signal_name(signal), signal);
}

static void sigpipe_handler(int signal) {
    set_broken_pipe();
    log_print("exiting by signal %s (%d)\n", signal_name(signal), signal);
}

static void startup() {
    err_init();
    dirtab_init(clear_persistence, refresh_explicit ? DE_WSTATE_NONE : DE_WSTATE_POLL);
    const char* basedir = dirtab_get_basedir();
    if (chdir(basedir)) {
        report_error("cannot change current directory to %s: %s\n", basedir, strerror(errno));
        exit(FAILED_CHDIR);
    }
    if (persistence) {
        trace(TRACE_INFO, "Cache location: %s\n", dirtab_get_basedir());
    } else {
        trace(TRACE_INFO, "peristence is OFF\n");
    }
    lock_or_unlock(true);
    state_init();
    if (is_traceable(TRACE_FINER) && ! dirtab_is_empty()) {
        trace(TRACE_INFO, "loaded dirtab\n");
        dirtab_visit(print_visitor, NULL);
    }
    delete_on_exit_list_init();    
    int curr_thread = 0;
    if (rp_thread_count > 1) {
        blocking_queue_init(&req_queue);
        for (curr_thread = 0; curr_thread < rp_thread_count; curr_thread++) {
            trace(TRACE_FINE, "Starting thread #%d...\n", curr_thread);
            rp_threads[curr_thread].no = curr_thread;
            pthread_create(&rp_threads[curr_thread].id, NULL, &rp_loop, &rp_threads[curr_thread]);
        }
        trace(TRACE_INFO, "Started %d response threads\n", rp_thread_count);
    } else {
        trace(TRACE_INFO, "Starting in single-thread mode\n");
    }

    if (refresh) {
        pthread_create(&rp_threads[curr_thread].id, NULL, &refresh_loop, &rp_threads[curr_thread]);
    }
    if (atexit(exit_function)) {
        report_error("error setting exit function: %s\n", strerror(errno));
        exit(FAILURE_SETTING_EXIT_FUNCTION);
    }

    struct sigaction new_sigaction;
    new_sigaction.sa_handler = signal_handler;
    new_sigaction.sa_flags = SA_RESTART;
    sigemptyset(&new_sigaction.sa_mask);
    sigaction_wrapper(SIGHUP, &new_sigaction, NULL);
    sigaction_wrapper(SIGQUIT, &new_sigaction, NULL);
    sigaction_wrapper(SIGINT, &new_sigaction, NULL);

    new_sigaction.sa_handler = signal_empty_handler;
    new_sigaction.sa_flags = SA_RESTART;
    sigemptyset(&new_sigaction.sa_mask);
    sigaction_wrapper(SIGUSR1, &new_sigaction, NULL);

    new_sigaction.sa_handler = sigpipe_handler;
    new_sigaction.sa_flags = 0;
    sigemptyset(&new_sigaction.sa_mask);
    sigaction_wrapper(SIGPIPE, &new_sigaction, NULL);
}

static void *killer(void *data) {
    pthread_t victim;
    victim = *((pthread_t*) data);
    sleep(2);
    //pthread_kill(victim, SIGKILL);
    pthread_kill(victim, SIGTERM);
    return NULL;
}

static void shutdown() {
    state_set_proceed(false);
    blocking_queue_shutdown(&req_queue);
    trace(TRACE_INFO, "Max. requests queue size: %d\n", blocking_queue_max_size(&req_queue));
    if (statistics) {
        my_fprintf(STDERR, "Max. requests queue size: %d\n", blocking_queue_max_size(&req_queue));
    }
    trace(TRACE_INFO, "Shutting down. Joining threads...\n");
    pthread_t killer_thread;
    static pthread_t self; // we need to pass a pointer => can't use auto
    self = pthread_self();
    pthread_create(&killer_thread, NULL, killer, &self);

    trace(TRACE_INFO, "Shutting down. Joining threads...\n");
    // NB: we aren't joining refresh thread; it's safe
    for (int i = 0; i < rp_thread_count; i++) {
        trace(TRACE_FINE, "Shutting down. Joining thread #%i [%ui]\n", i, rp_threads[i].id);
        pthread_join(rp_threads[i].id, NULL);
    }
    delete_on_exit_impl();
    if (refresh) {
        int refresh_thread_idx = rp_thread_count;
        pthread_kill(rp_threads[refresh_thread_idx].id, SIGUSR1);
        trace(TRACE_FINE, "Shutting down. Joining refresh thread #%i [%ui]\n", refresh_thread_idx, rp_threads[refresh_thread_idx].id);
        pthread_join(rp_threads[refresh_thread_idx].id, NULL);
    }

    if (!dirtab_flush()) {
        report_error("error storing dirtab\n");
    }
    dirtab_free();
    log_close();
    err_shutdown();
    free_settings();
    if (error_log) {
        free((void*)error_log);
    }    
    trace(TRACE_INFO, "Shut down.\n");
    exit(0);
}

static void log_header(int argc, char* argv[]) {
    if (log_flag) {
        log_open("log") ;
        log_and_err_print("\n--------------------------------------\nfs_server version %d.%d.%d [built %s %s] started on ",
                FS_SERVER_MAJOR_VERSION, FS_SERVER_MID_VERSION, FS_SERVER_MINOR_VERSION, __DATE__, __TIME__);
        time_t t = time(NULL);
        struct tm *tt = localtime(&t);
        if (tt) {
            log_and_err_print("%d/%02d/%02d at %02d:%02d:%02d\n",
                    tt->tm_year+1900, tt->tm_mon + 1, tt->tm_mday,
                    tt->tm_hour, tt->tm_min, tt->tm_sec);
        } else {
            log_and_err_print("<error getting time: %s>\n", strerror(errno));
        }
        for (int i = 0; i < argc; i++) {
            log_and_err_print("%s ", argv[i]);
        }
        log_and_err_print("\n");
    }
}

int main(int argc, char* argv[]) {
    process_options(argc, argv);
    err_redirect_init();
    trace(TRACE_INFO, "Version %d.%d.%d (%s %s)\n", FS_SERVER_MAJOR_VERSION,
            FS_SERVER_MID_VERSION, FS_SERVER_MINOR_VERSION, __DATE__, __TIME__);
    startup();
    log_header(argc, argv);
    main_loop();
    shutdown();
    return 0;
}

