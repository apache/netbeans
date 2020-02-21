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
#include "util.h"
#include "exitcodes.h"

#include <stdio.h> 
#include <stdarg.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <pwd.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <signal.h>
#include <limits.h>

static TraceLevel trace_level = TRACE_NONE;
static FILE *log_file = NULL;
static FILE *err_file = NULL;

static volatile bool broken_pipe = false;

bool is_broken_pipe() {
    return broken_pipe;
}

void set_broken_pipe() {
    broken_pipe = true;
}

static FILE *get_stderr() {
    return err_file ? err_file : stderr;
}

void redirect_err(const char* file_name) {
    if (file_name) {
        err_file = fopen600(file_name, O_WRONLY | O_APPEND | O_CREAT);
        if (!err_file) {
            err_file = stderr;
            // this is called from 
            char strerr_buf[PATH_MAX*2];
            strerror_r(errno, strerr_buf, sizeof(strerr_buf));
            report_error("Error redirecting stderr to %s: %s", file_name, strerr_buf);
        }
    } else {
        err_file = stderr;
    }
}

void my_fflush(std_stream stream) {
    if (!is_broken_pipe()) {
        fflush(stream == STDERR ? get_stderr() : stdout);
    }
}

void my_fprintf(std_stream stream, const char *format, ...) {
    if (!is_broken_pipe()) {
        va_list args;
        va_start(args, format);
        vfprintf(stream == STDERR ? get_stderr() : stdout, format, args);
        va_end(args);  
    }
}

void set_trace(TraceLevel new_level) {
    trace_level= new_level;
}

bool is_traceable(TraceLevel level) {
    return (trace_level >= level);
}

void report_error(const char *format, ...) {
    if (!is_broken_pipe()) {
        va_list args;
        va_start (args, format);
        fprintf(get_stderr(), "fs_server [%li %li]: ", (long) getpid(), (long) pthread_self());
        vfprintf(get_stderr(), format, args);
        va_end (args);
    }
}

void trace(TraceLevel level, const char *format, ...) {
    if (trace_level >= level && !is_broken_pipe()) {
        va_list args;
        va_start(args, format);
        //fprintf(stderr, "fs_server [pid=%li, tid=%li]: ", (long) getpid(), (long) pthread_self());
        fprintf(get_stderr(), "fs_server [%li %li]: ", (long) getpid(), (long) pthread_self());
        vfprintf(get_stderr(), format, args);
        va_end(args);  
        fflush(get_stderr());
    }
}

void log_and_err_print(const char *format, ...) {
    if (err_file) {
        va_list args;
        va_start(args, format);
        vfprintf(err_file, format, args);
        va_end(args);  
        fflush(err_file);
    }
    if (log_file) {
        va_list args;
        va_start(args, format);
        vfprintf(log_file, format, args);
        va_end(args);  
        fflush(log_file);
    }
}

void log_print(const char *format, ...) {
    if (log_file) {
        va_list args;
        va_start(args, format);
        vfprintf(log_file, format, args);
        va_end(args);  
        fflush(log_file);
    }
}

void log_open(const char* path) {
    int fd = open(path, O_WRONLY | O_APPEND | O_CREAT, 0600);
    if (fd == -1) {
        log_file = NULL;
    } else {
        log_file = fdopen(fd, "a");
    }
}

void log_close() {    
    if (log_file) {
        fclose(log_file);
    }
    log_file = NULL;
}

void soft_assert(int condition, char* format, ...) {
    if (!condition && !is_broken_pipe()) {
        va_list args;
        va_start(args, format);
        vfprintf(get_stderr(), format, args);
        va_end(args);
    }
}
char *strdup_wrapper(const char* str) {
    char* p = strdup(str);
    if (!p) {
        report_error("out of memory\n");
        exit(FAILURE_ALLOCATE_MEMORY);
    }
    return p;
}

void *malloc_wrapper(size_t size) {
    void *p = malloc(size);
    if (!p) {
        report_error("out of memory\n");
        exit(FAILURE_ALLOCATE_MEMORY);
    }
    return p;
}

void *realloc_wrapper(void *ptr, size_t size) {
    void *p = realloc(ptr, size);
    if (!p) {
        report_error("can not reallocate memory\n");
        exit(FAILURE_REALLOCATE_MEMORY);
    }
    return p;
}

void mutex_lock_wrapper(pthread_mutex_t *mutex) {
    int err = pthread_mutex_lock(mutex);
    if (err) {
        report_error("error locking mutex: %s\n", strerror(err));
        exit(FAILURE_LOCKING_MUTEX);
    }
}

void mutex_unlock_wrapper(pthread_mutex_t *mutex) {
    int err = pthread_mutex_unlock(mutex);
    if (err) {
        report_error("error unlocking mutex: %s\n", strerror(err));
        exit(FAILURE_UNLOCKING_MUTEX);
    }
}

bool get_home_dir(char* home, int size) {
    uid_t uid = getuid();
    int bufsize = sysconf(_SC_GETPW_R_SIZE_MAX);
    if (bufsize < 0) {
        return false;
    }
    char buffer[bufsize];
    struct passwd pwd;
    struct passwd *result;
    if(getpwuid_r(uid, &pwd, buffer, bufsize, &result) == 0) {
        strncpy_w_zero(home, pwd.pw_dir, size);
        return true;
    }
    return false;
}

/// NB: does not take settings->forbidden... into account
bool file_exists(const char* path) {
    struct stat stat_buf;
    if (lstat(path, &stat_buf) == -1 ) {
        return errno != ENOENT;        
    }
    return true;
}

/// NB: does not take settings->forbidden... into account
bool dir_exists(const char* path) {
    struct stat stat_buf;
    if (lstat(path, &stat_buf) == -1 ) {
        switch (errno) {
            case ENOENT:
            case ENOTDIR:
            case 0: // errno is often set to 0 if a file does not exist
                return false;
            default:
                return true;
        }
    }
    return S_ISDIR(stat_buf.st_mode);
}

int fclose_if_not_null(FILE* f) {
    return f ? fclose(f) : 0;
}

int closedir_if_not_null(DIR *d) {
    return d ? closedir(d) : 0;
}


static __thread long stopwatch_start_time;

void stopwatch_start() {
    if (trace_level) {
        struct timeval curr_time;
        gettimeofday(&curr_time, 0);
        stopwatch_start_time = curr_time.tv_sec * 1000 + curr_time.tv_usec / 1000;
    }
}

void stopwatch_stop(TraceLevel level, const char* message) {
    if (trace_level >= level) {
        struct timeval curr_time;
        gettimeofday(&curr_time, 0);
        long end_time = curr_time.tv_sec * 1000 + curr_time.tv_usec / 1000;
        trace(level, "%s took %d ms\n", message, end_time - stopwatch_start_time);
    }    
}

char *replace_first(char *s, char c, char replacement) {
    if (s) {
        char* p = strchr(s, c);
        if (p) {
            *(p) = replacement;
        }
    }
    return s;
}

FILE* fopen600(const char* path, int flags) {
    int fd = open(path, flags, 0600);
    if (fd == -1) {
        return NULL;
    } else {
        return fdopen(fd, "w");
    }
}

/**
 * escapre rules are:
 * "\n" -> "\\n"
 * "\" -> "\\\\"
 */
int escape_strlen(const char* s) {
    if (!s) {
        return 0;
    }
    int len = 0;  
    for (const char *p = s; *p; p++) {
        len += (*p == '\n' || *p == '\\') ? 2 : 1;
    }
    return len;
}

char *escape_strcpy(char *dst, const char *src) {
    char* d = dst;
    for (const char *p = src; *p; p++) {
        if (*p == '\n') {
            *d++ = '\\';
            *d++ = 'n';
        } else if (*p == '\\') {
            *d++ = '\\';
            *d++ = '\\';
        } else {
            *d++ = *p;
        }
    }
    *d = 0;
    return dst;
}

int unescape_strlen(const char* s) {    
    bool escape = false;
    int len = 0;
    for (const char *p = s; *p; p++) {
        if (escape) {
            escape = false;
            len++;
        } else {
            if (*p == '\\') {
                escape = true;
            } else {
                len++;
            }
        }
    }
    return len;
}

char *unescape_strcpy(char *dst, const char *src) {
    bool escape = false;
    char *d = dst;
    for (const char *p = src; *p; p++) {
        if (escape) {
            escape = false;
            if (*p == '\\') {
                *d++ = '\\';
            } else if (*p == 'n') {
                *d++ = '\n';
            } else {
                report_error("wrong character '%c' in line %s\n", *p, src);
                *d++ = *p;
            }
        } else {
            if (*p == '\\') {
                escape = true;
            } else {
                *d++ = *p;
            }
        }
    }
    *d = 0;
    return dst;
}

long long get_mtime(struct stat *stat_buf) {
    long long  result = stat_buf->st_mtime;
    result *= 1000;
#if __FreeBSD__
    #if __BSD_VISIBLE
        result += stat_buf->st_mtimespec.tv_nsec/1000000;
    #else
        result += stat_buf->__st_mtimensec/1000000;
    #endif
#elif __APPLE__
    result += stat_buf->st_mtimespec.tv_nsec/1000000;
#else
    result +=  stat_buf->st_mtim.tv_nsec/1000000;    
#endif
    return result;
}

long long get_curretn_time_millis() {
    struct timeval tm;
    gettimeofday(&tm, 0);
    return tm.tv_sec * 1000 + tm.tv_usec / 1000;
}

char* signal_name(int signal) {
    switch (signal) {
        case SIGHUP:    return "SIGHUP";
        case SIGINT:    return "SIGINT";
        case SIGQUIT:   return "SIGQUIT";
        case SIGILL:    return "SIGILL";
        case SIGTRAP:   return "SIGTRAP";
        case SIGABRT:   return "SIGABRT";
//        case SIGIOT:    return "SIGIOT";
        case SIGBUS:    return "SIGBUS";
        case SIGFPE:    return "SIGFPE";
        case SIGKILL:   return "SIGKILL";
        case SIGUSR1:   return "SIGUSR1";
        case SIGSEGV:   return "SIGSEGV";
        case SIGUSR2:   return "SIGUSR2";
        case SIGPIPE:   return "SIGPIPE";
        case SIGALRM:   return "SIGALRM";
        case SIGTERM:   return "SIGTERM";
#if __linux__  && ! __sparc__
        case SIGSTKFLT: return "SIGSTKFLT";
#endif        
//        case SIGCLD:    return "SIGCLD"; // dup
        case SIGCHLD:   return "SIGCHLD";
        case SIGCONT:   return "SIGCONT";
        case SIGSTOP:   return "SIGSTOP";
        case SIGTSTP:   return "SIGTSTP";
        case SIGTTIN:   return "SIGTTIN";
        case SIGTTOU:   return "SIGTTOU";
        case SIGURG:    return "SIGURG";
        case SIGXCPU:   return "SIGXCPU";
        case SIGXFSZ:   return "SIGXFSZ";
        case SIGVTALRM: return "SIGVTALRM";
        case SIGPROF:   return "SIGPROF";
        case SIGWINCH:  return "SIGWINCH";
//        case SIGPOLL:   return "SIGPOLL"; // dup
        case SIGIO:     return "SIGIO (SIGPOLL)";
#if __linux__ || __sun__        
        case SIGPWR:    return "SIGPWR";
#endif        
#if __FreeBSD__
        case SIGINFO:    return "SIGINFO";
#endif        
        default:        return "SIG???";
    }
}

static bool removing_visitor(char* name, struct stat *stat_buf, char* link, const char* path, void *p) {
    bool *success = p;
    if (S_ISDIR(stat_buf->st_mode)) {
        if (clean_dir(path)) {
            if (rmdir(path)) {
                report_error("error deleting '%s': %s\n", path, strerror(errno));
                *success = false;
            }
        } else {
            *success = false;
        }
    } else {
        if (unlink(path)) {
            report_error("error deleting '%s': %s\n", path, strerror(errno));
            *success = false;
        }
    }
    return true;    
}

bool clean_dir(const char* path) {
    bool res = true;
    visit_dir_entries(path, removing_visitor, NULL, NULL, &res);
    return res;
}

buffer buffer_alloc(int size) {
    buffer result = { size, malloc_wrapper(size) };
    return result;
}

void buffer_free(buffer* buf) {
    if (buf) {
        free(buf->data);
    }
}

void default_error_handler(bool dir_itself, const char* path, int err, const char* additional_message, void *data) {
    char buf[400];
    const char* emsg;
#if __linux__
    emsg = strerror_r(err, buf, 400);
#else
    emsg = strerror_r(err, buf, 256) ? "?" : buf;
#endif
    report_error("%s '%s': %s\n", additional_message, path, emsg);
}

/** returns 0 in the case of success or errno in the case of failure */
int visit_dir_entries(const char* path, 
        bool (*visitor) (char* name, struct stat *st, char* link, const char* abspath, void *data), 
        void (*error_handler) (bool dir_itself, const char* path, int err, const char* additional_message, void *data),
        const settings_str* settings,
        void *data) {
    if (!error_handler) {
        error_handler = default_error_handler;
    }
    DIR *d = opendir(path);
    if (d) {
        struct {
            struct dirent d;
            char b[MAXNAMLEN + 1];
        } entry_buf;
        entry_buf.d.d_reclen = MAXNAMLEN + sizeof (struct dirent);
        int buf_size = PATH_MAX + 1;
        char *abspath = malloc_wrapper(buf_size);
        char *link = malloc_wrapper(buf_size);
        int base_len;
        if (path[0] == '/' && path[1] == '\0') {
            base_len = 0;
        } else {
            base_len = strlen(path);
            strcpy(abspath, path);
        }
        abspath[base_len] = '/';
        struct dirent *entry;
        while (true) {
            if (readdir_r(d, &entry_buf.d, &entry)) {
                error_handler(true, path, errno, "error reading directory", data);
                break;
            }
            if (!entry) {
                break;
            }
            if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
                continue;
            }
            strcpy(abspath + base_len + 1, entry->d_name);
            struct stat stat_buf;
            if (lstat_wrapper(abspath, &stat_buf, settings) == 0) {
                bool is_link = S_ISLNK(stat_buf.st_mode);
                if (is_link) {
                    ssize_t sz = readlink(abspath, link, buf_size);
                    if (sz == -1) {
                        error_handler(false, abspath, errno, "error performing readlink", data);
                        strcpy(link, "?"); // strcpy is safe, data is more than 1 byte anyhow
                    } else {
                        link[sz] = 0;
                    }
                }
                if (!visitor(entry->d_name, &stat_buf, link, abspath, data)) {
                    break;
                }
            } else {
                error_handler(false, abspath, errno, "error getting stat", data);
            }
        }
        free(abspath);
        free(link);
        closedir(d);
        return true; // TODO: error processing: what some of them has errors?
    } else {
        error_handler(true, path, errno, "error opening directory", data);
        return false;
    }
}

const char* get_basename(const char *path) {
    const char* basename = strrchr(path, '/');
    if (basename) {
        basename++; // next after '/'
    } else {
        basename = path;
    }    
    return basename;
}

int utf8_bytes_count(const char *buffer, int char_count) {
    unsigned const char* p = (unsigned const char*) buffer;
    // 0x80 - middle byte
    while (*p && (char_count > 0)) {
        char_count -= (*p++ & 0xC0) != 0x80;
    }
    while (*p && ((*p & 0xC0) == 0x80)) {
        p++;
    }
    return (const char*) p - buffer;
}

int utf8_char_count(const char *buffer, int byte_count) {
    unsigned const char* p = (unsigned const char*) buffer;
    int len = 0;
    for (int i = 0; i < byte_count; i++) {
        len += (p[i] & 0xc0) != 0x80;
    }
    return len;
}

int utf8_strlen(const char *buffer) {
    int len = 0;
    unsigned const char* p = (unsigned const char*) buffer;
    while (*p) {
        len += (*p++ & 0xc0) != 0x80;    
    }
    return len;
}

static bool slashes_tail(const char* p) {
    while (*p) {
        if (*p != '/') {
            return false;
        }
        p++;
    }
    return true;
}

/** returns true if dir is subdirectory of parent OR if they are EQUAL */
bool is_subdir(const char* child, const char* parent) {
    const char *c = child;
    const char *p = parent;
    while (*p && *p == *c) {
        c++;
        p++;
    }
    // we are either at dir terminating '\0' or first differnce
    if (*p && *c) {
        return false; // current char differ
    } else if (!*p && !*c) {
        return true; // both ended => just equals
    } else if (*p) {
        // child ended first
        return slashes_tail(p);
    } else {
        // parent ended first
        if (*c == '/') {
            return true;
        } else if (c > child && *(c-1) == '/') {
            return true;
        }
        return false;
    } 
}

/** The same as strncpy, but stores trailing zero byte even in src len is more than limit */
char *strncpy_w_zero(char *dst, const char *src, size_t limit) {
    char * res = strncpy(dst, src, limit);
    dst[limit-1] = 0;
    return res;
}

char mode_to_file_type_char(int mode) {
    return (char) mode_to_file_type(mode);
}

file_type mode_to_file_type(int mode) {
    
    if (S_ISFIFO(mode)) {
        return FILETYPE_FIFO;
    } else if (S_ISCHR(mode)) {
        return FILETYPE_CHR;
    } else if(S_ISDIR(mode)) {
        return FILETYPE_DIR;
    } else if(S_ISBLK(mode)) {
        return FILETYPE_BLK;
    } else if (S_ISREG(mode)) {
        return FILETYPE_REG;
    } else if (S_ISLNK(mode)) {
        return FILETYPE_LNK;
    } else if(S_ISSOCK(mode)) {
        return FILETYPE_SOCK;
#if __sun__        
    } else if(S_ISDOOR(mode)) {
        return FILETYPE_DOOR;
    } else if(S_ISPORT(mode)) {
        return FILETYPE_PORT;
#endif        
    } else {
        return FILETYPE_UNKNOWN; // for other stat info to have a default
    }
}

static const short ACCESS_MASK = 0x1FF;
static const short USR_R = 256;
static const short USR_W = 128;
static const short USR_X = 64;
static const short GRP_R = 32;
static const short GRP_W = 16;
static const short GRP_X = 8;
static const short ALL_R = 4;
static const short ALL_W = 2;
static const short ALL_X = 1;    

static bool can(const struct stat *stat, short all_mask, short grp_mask, short usr_mask) {
    static bool first = true;
    static uid_t uid;
    static gid_t groups[100];
    static int group_count;
    if (first) {
        first = false;
        uid = getuid();
        group_count = getgroups(sizeof groups, groups);
    }
    unsigned int access = stat->st_mode & ACCESS_MASK;
    if (stat->st_uid == uid) {
        return (access & usr_mask) > 0;
    } else if (group_count) {
        bool group_found = false;
        for (int i = 0; i < group_count; i++) {
            int curr_grp = groups[i];
            if (curr_grp == stat->st_gid) {
                group_found = true;
                break;  
            }
        }
        if (group_found) {
            return (access & grp_mask) > 0;
        }
    }
    return (access & all_mask) > 0;
}

bool can_read(const struct stat *stat) {
    return can(stat, ALL_R, GRP_R, USR_R);
}

bool can_write(const struct stat *stat) {
    return can(stat, ALL_W, GRP_W, USR_W);
}

bool can_exec(const struct stat *stat) {
    return can(stat, ALL_X, GRP_X, USR_X);
}

static void fill_dummy_stat(struct stat *stat_buf) {
    memset(stat_buf, '\0', sizeof(struct stat));
    stat_buf->st_mode = S_IFDIR | S_IROTH | S_IXOTH;
}

int lstat_wrapper(const char *path, struct stat *stat_buf, const settings_str* settings) {
    if (is_dir_forbidden_to_stat(path, settings)) {
        trace(TRACE_FINEST, "Skipping forbidden lstat for %s\n", path);
        fill_dummy_stat(stat_buf);
        return 0;
    } else {
        return lstat(path, stat_buf);
    }
}

int stat_wrapper(const char *path, struct stat *stat_buf, const settings_str* settings) {
    if (is_dir_forbidden_to_stat(path, settings)) {
        trace(TRACE_FINEST, "Skipping forbidden stat for %s\n", path);
        fill_dummy_stat(stat_buf);
        return 0;
    } else {
        return stat(path, stat_buf);
    }
}
