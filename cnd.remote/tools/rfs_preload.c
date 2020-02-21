/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

#include <dlfcn.h>
#include <stdarg.h>

#include <fcntl.h>
#include <alloca.h>

#include "rfs_protocol.h"
#include "rfs_preload_socks.h"
#include "rfs_util.h"

/** the name of the directory under control, including trailing "\" */
static char my_dir[PATH_MAX + 3]; // static => zero-initialized
static const int my_dir_capacity = PATH_MAX + 1; // less by 2: reserve space for trailing '/' and '\0'
static int my_dir_len;
static bool initialized = false;

static int test_env = 0;

static int __thread inside_open = 0;

#define get_real_addr(name) _get_real_addr(#name, (void*)name);

static inline void *_get_real_addr(const char *name, void* wrapper_addr) {
    void *res;
    int saved_errno = errno;
    res = dlsym(RTLD_NEXT, name);
    if (res && (res == wrapper_addr)) {
        res = dlsym(RTLD_NEXT, name);
    }
    if (!res) {
        res = dlsym(RTLD_DEFAULT, name);
    }
    errno = saved_errno;
    return res;
}

/*--------------------------------------------------
static void dbg_print_addr(const char* name) {
    void* addr = dlsym(RTLD_NEXT, name);
    trace("\t%s=%X\n", name, addr);
}

static inline void print_dlsym() {
    const char* names[] = {
        "lstat", "_lstat", "stat", "_lxstat", "fstat", "lstat64", "fstat64", "_fxstat",
        "open", "fopen", "open64", "pthread_self",
        "readdir", "access", "utime", 0
    };
    int i = 0;
    while (names[i]) {
        dbg_print_addr(names[i]);
        i++;
    }
}
--------------------------------------------------*/

static bool is_writing(int flags) {
    return flags & (O_TRUNC |  O_WRONLY | O_RDWR | O_CREAT);
}

static void post_open(const char *path, int flags) {
    if (inside_open != 1) {
        trace("post open: %s inside_open == %d   returning\n", path, inside_open);
        return; // recursive call to open
    }
    static int __thread inside = 0;
    if (inside) {
        trace("post open: %s recursive post open - returning\n", path);
        return; // recursive!
    }
    if (!is_writing(flags)) {
        trace("post open: %s not writing - returning\n", path);
        return;
    }
    if (!initialized || *my_dir == '\0') { // isn't yet initialized? (checking my_dir check is a paranoia)
        trace("post open: %s not yet initialized - returning\n", path);
        return;
    }
    inside = 1;

    static __thread char real_path[PATH_MAX + 1];
    if ( realpath(path, real_path)) {
        path = real_path;
    } else {
        trace_unresolved_path(path, "post_open");
        inside = 0;
        return;
    }

    if (strncmp(my_dir, path, my_dir_len) != 0) {
        trace("post open: %s is not mine\n", path);
        inside = 0;
        return;
    }
    int sd = get_socket(true);
    if (sd == -1) {
        trace("post open: %s: sd == -1\n", path);
    } else {
        trace_sd("post open: sending request");
        trace("post open: sending %s \"%s\" to sd=%d\n", pkg_kind_to_string(pkg_written),  path, sd);
        enum sr_result send_res = pkg_send(sd, pkg_written, path);
        if (send_res == sr_failure) {
            perror("send");
        } else if (send_res == sr_reset) {
            perror("Connection reset by peer when sending request");
        }
    }
    inside = 0;
}
/**
 * Called upon opening a file; returns "boolean" success
 * @return true means "ok" (either file is in already sync,
 * or it has been just synched, or or the path isn't under control;
 * false means that the file is ourm, but can't be synched
 */
static bool pre_open(const char *path, int flags) {
    if (test_env) {
        fprintf(stdout, "RFS_TEST_PRELOAD %s\n", path);
        return true;
    }
    if (inside_open != 1) {
        trace("pre open: %s inside_open == %d   returning\n", path, inside_open);
        return true; // recursive call to open
    }
    static int __thread inside = 0;
    if (inside) {
        trace("pre open: %s recursive - returning\n", path);
        return true; // recursive!
    }
    if (is_writing(flags)) { // don't need existent content
        // #196728 Remote build problem with touch command on remote Linux host 
        if (flags & O_TRUNC) {
            trace("pre open: %s is writing and truncating - returning\n", path);
            return true;
        } else {
            trace("pre open: %s is writing, but not truncating - proceed\n", path);
        }
    }
    if (!initialized || *my_dir == '\0') { // isn't yet initialized? (checking my_dir check is a paranoia)
        trace("pre open: %s not yet initialized - returning\n", path);
        return true;
    }
    inside = 1;

    static __thread char real_path[PATH_MAX + 1];
    if ( !realpath(path, real_path)) {
        trace_unresolved_path(path, "pre_open");
        inside = 0;
        // We usually get here if file does not exist.
        // Don't return false - let real open apply its logic
        return true;
    }

    if (strncmp(my_dir, real_path, my_dir_len) != 0) {
        trace("pre open: %s is not mine\n", real_path);
        inside = 0;
        return true;
    }
    bool result = false;
    int sd = get_socket(true);
    if (sd == -1) {
        trace("On open %s: sd == -1\n", real_path);
    } else {
        trace_sd("sending request");
        trace("Sending %s \"%s\" to sd=%d\n", pkg_kind_to_string(pkg_request),  real_path, sd);
        enum sr_result send_res = pkg_send(sd, pkg_request, real_path);
        if (send_res == sr_failure) {
            perror("send");
        } else if (send_res == sr_reset) {
            perror("Connection reset by peer when sending request");
        } else { // success
            trace("Request for \"%s\" sent to sd=%d\n", real_path, sd);
            const int maxsize = 256;
            char buffer[maxsize + sizeof(int)];
            struct package *pkg = (struct package *) &buffer;
            enum sr_result recv_res = pkg_recv(sd, pkg, maxsize);
            if (recv_res == sr_failure) {
                perror("Error receiving response");
            } else if (recv_res == sr_reset) {
                perror("Connection reset by peer when receiving response");
            } else { // success
                if (pkg->kind == pkg_reply) {
                    trace("Got %s for %s, flags=%d, sd=%d\n", pkg->data, real_path, flags, sd);
                    if (pkg->data[0] == response_ok) {
                        result = true;
                    } else if (pkg->data[0] == response_failure) {
                        result = false;
                    } else {
                        trace("Protocol error, sd=%d\n", sd);
                        result = false;
                    }
                } else {
                    trace("Protocol error: get pkg_kind %d instead of %d\n", pkg->kind, pkg_reply);
                }
            }
        }
    }
    inside = 0;
    return result;
}

pid_t fork() {
    pid_t result;
    static pid_t (*prev)(void);
        if (!prev) {
            prev = (pid_t (*)(void)) get_real_addr(fork);
            }
        if (prev) {
            result = prev();
        } else {
            trace("Could not find original \"%s\" function\n", "fork");
            errno = EFAULT;
            result = -1;
        }
    if (result == 0) {
        release_socket(); // child
    } else {
        trace("%s -> %ld\n", "fork", result);
    }
    return result;
}

//pid_t vfork() {
//    real_fork("vfork", vfork);
//}

/** gets current process short name */
static char* get_procname(char* name, int len) {
    #ifdef __linux__
    char path[PATH_MAX + 1];
    if (readlink ("/proc/self/exe", path, sizeof path) != -1) {
        trace("0 %d\n", path);
        char *res = basename(path);
        if (res) {
            strncpy(name, res, len);
            return name;
        }
    }
    return 0;
    #else
    // not yet implemented for Solaris
    return 0;
    #endif
}

static void sleep_if_need() {
    const char* env_sleep_var = "RFS_PRELOAD_SLEEP";
    const char *env_sleep = getenv(env_sleep_var);
    // examples are:
    // RFS_PRELOAD_SLEEP=*,20
    //      means that we sleep 20 seconds on any process
    // RFS_PRELOAD_SLEEP=CC,10
    //      means that we sleep 10 seconds in CC
    if (env_sleep) {
        char trace_procname[80]; // name of the process
        const char *strtime; // time to sleep string representation
        if (*env_sleep == '*') {
            strtime = env_sleep + 2; // skip ','
            trace_procname[0] = 0;
        } else  {
            strtime = strchr(env_sleep, ',');            
            if (strtime) {
                int size = strtime - env_sleep;
                if (size + 1 < sizeof trace_procname) {
                    strncpy(trace_procname, env_sleep, size);
                    trace_procname[size] = 0;
                    strtime++;
                } else {
                    strtime = 0; // the case when strtime==0 is processed lower
                }
            }
            // the case when strtime==0 is processed lower
        }

        if (*trace_procname) {
            char curr_procname[80];
            if (!get_procname(curr_procname, sizeof curr_procname)) {
                return;
            }
            if (strcmp(trace_procname, curr_procname) != 0) {
                return;
            }
        }

        int time = strtime ? atoi(strtime) : -1;
        if (time > 0) {
            fprintf(stderr, "%s=%s is set. Process %d, sleeping %d seconds...\n", env_sleep_var, env_sleep, getpid(), time);
            fflush(stderr);
            sleep(time);
            fprintf(stderr, "... awoke.\n");
            fflush(stderr);
        } else {
            fprintf(stderr, "Incorrect value, should be a positive integer: %s=%s\n", env_sleep_var, env_sleep);
            fflush(stderr);
        }
    }
}

#pragma init(rfs_startup)
static void
__attribute__((constructor))
rfs_startup(void) {
    init_trace_flag("RFS_PRELOAD_TRACE");
    trace_startup("RFS_P", "RFS_PRELOAD_LOG", NULL);
    test_env = getenv("RFS_TEST_ENV") ? true : false; // like #ifdef :)
    trace("test_env %s\n", test_env ? "ON" : "OFF");
    
    char* dir = getenv("RFS_CONTROLLER_DIR");
    if (dir) {
        strncpy(my_dir, dir, my_dir_capacity);
    } else {
        getcwd(my_dir, my_dir_capacity);
    }
    char real_dir[PATH_MAX + 1];
    if ( realpath(my_dir, real_dir)) {
        strncpy(my_dir, real_dir, my_dir_capacity);
    } else {
        trace_unresolved_path(dir, "RFS startup");
    }
    my_dir_len = strlen(my_dir);
    if (dir[my_dir_len-1] != '/') {
        my_dir_len++;
        strcat(my_dir, "/");
    }
    initialized = true;

    static int startup_count = 0;
    startup_count++;
    trace("RFS startup (%d) my dir: %s\n", startup_count, dir);

    release_socket();
    trace_sd("startup");
    sleep_if_need();
}

#pragma init(rfs_shutdown)
static void
__attribute__((destructor))
rfs_shutdown(void) {
    static int shutdown_count = 0;
    shutdown_count++;
    trace("RFS shutdown (%d)\n", shutdown_count);
    trace_shutdown();
    release_socket();
}

typedef struct pthread_routine_data {
    void *(*user_start_routine) (void *);
    void* arg;
} pthread_routine_data;

static void* pthread_routine_wrapper(void* data) {
    pthread_routine_data *prd = (pthread_routine_data*) data;
    trace("Starting user thread routine.\n");
    prd->user_start_routine(prd->arg);
    trace("User thread routine finished. Performing cleanup\n");
    free(data);
    release_socket();
    return 0;
}

int pthread_create(void *newthread,
        void *attr,
        void *(*user_start_routine) (void *),
        void *arg) {
    trace("pthread_create\n");
    static int (*prev)(void *, void*, void * (*)(void *), void*);
    if (!prev) {
        prev = (int (*)(void*, void*, void * (*)(void *), void*)) get_real_addr(pthread_create);
    }
    pthread_routine_data *data = malloc_wrapper(sizeof (pthread_routine_data));
    // TODO: check for null???
    data->user_start_routine = user_start_routine;
    data->arg = arg;
    return prev(newthread, attr, pthread_routine_wrapper, data);
}

#define real_open(function_name, path, flags) \
    inside_open++; \
    trace("%s %s %d\n", #function_name, path, flags); \
    va_list ap; \
    mode_t mode; \
    va_start(ap, flags); \
    mode = va_arg(ap, mode_t); \
    va_end(ap); \
    int result = -1; \
    if (pre_open(path, flags)) { \
        static int (*prev)(const char *, int, ...); \
        if (!prev) { \
            prev = (int (*)(const char *, int, ...)) get_real_addr(function_name); \
        } \
        if (prev) {\
            result = prev(path, flags, mode); \
            if (result != -1) { \
                post_open(path, flags); \
            } \
        } else { \
            trace("Could not find original \"%s\" function\n", #function_name); \
            errno = EFAULT; \
            result = -1; \
        } \
    } \
    trace("%s %s -> %d\n", #function_name, path, result); \
    inside_open--; \
    return result;

#define real_fopen(function, path, mode) \
    inside_open++; \
    trace("%s %s %s\n", #function, path, mode); \
    FILE* result = NULL; \
    int int_mode = (strchr(mode, 'w') || strchr(mode, '+'))  ? O_WRONLY : O_RDONLY; \
    if (pre_open(path, int_mode)) { \
        static FILE* (*prev)(const char *, const char *); \
        if (!prev) { \
            prev = (FILE* (*)(const char *, const char *)) get_real_addr(function); \
        } \
        if (prev) { \
            result = prev(path, mode); \
            if (result) { \
                post_open(path, int_mode); \
            } \
        } else { \
            trace("Could not find original \"%s\" function\n", #function); \
            errno = EFAULT; \
            result = NULL; \
        } \
    } \
    trace("%s %s -> %d\n", #function, path, result); \
    inside_open--; \
    return result;
    //result ? -12345 : fileno(result)

int open(const char *path, int flags, ...) {
    real_open(open, path, flags);
}

#if _FILE_OFFSET_BITS != 64
int open64(const char *path, int flags, ...) {
    real_open(open64, path, flags);
}
#endif

int _open(const char *path, int flags, ...) {
    real_open(_open, path, flags);
}

int _open64(const char *path, int flags, ...) {
    real_open(_open64, path, flags);
}

int __open(const char *path, int flags, ...) {
    real_open(__open, path, flags);
}

int __open64(const char *path, int flags, ...) {
    real_open(__open64, path, flags);
}

#ifdef __sun
int execve(const char *path, char *const argv[], char *const envp[]) {
    inside_open++;
    int path_size = strlen(path) + 1;
    char *temp_path = alloca(path_size);
    strncpy(temp_path, path, path_size);
    path = temp_path;
    const char *function_name = "execve";
    trace("%s %s %d\n", function_name, path);
    int result = -1;
    int flags = 0;
    if (pre_open(path, flags)) {
        static int (*prev)(const char *, char *const *, char *const *);
        if (!prev) {
            prev = (int (*)(const char *, char *const *, char *const *)) _get_real_addr(function_name, (void*) execve);
        }
        if (prev) {
            result = prev(path, argv, envp);
            // no post_open here since execve never returns in the case of success
            //post_open(path, flags);
        } else {
            trace("Could not find original \"%s\" function\n", function_name);
            errno = EFAULT;
            result = -1;
        }
    }
    trace("%s %s -> %d\n", function_name, path, result);
    inside_open--;
    return result;
}
#endif // __sun

int rename(const char *oldpath, const char *path) {
    inside_open++;
    const char *function_name = "rename";
    trace("%s %s %s\n", function_name, oldpath, path);
    int result = -1;
    if (pre_open(oldpath, 0)) {
        static int (*prev)(const char *, const char *);
        if (!prev) {
            prev = (int (*)(const char *, const char *)) _get_real_addr(function_name, (void*) rename);
        }
        if (prev) {
            result = prev(oldpath, path);
            if (result == -1) {
                trace("Errno=%d %s\n", errno, strerror(errno));
                // why should we call perror here? it's up to caller
                // perror("RENAMING ");
            } else {
                post_open(path, O_TRUNC | O_CREAT | O_WRONLY);
            }
        } else {
            trace("Could not find original \"%s\" function\n", function_name);
            errno = EFAULT;
            result = -1;
        }
    }
    trace("%s %s %s -> %d\n", function_name, oldpath, path, result);
    inside_open--;
    return result;
}

//#ifdef __linux__
FILE *fopen(const char * filename, const char * mode) {
    real_fopen(fopen, filename, mode);
}

#if _FILE_OFFSET_BITS != 64
FILE *fopen64(const char * filename, const char * mode) {
    real_fopen(fopen64, filename, mode);
}
#endif
//#endif

#define real_freopen(function, path, mode, stream) \
    inside_open++; \
    trace("%s %s %s\n", #function, path, mode); \
    FILE* result = NULL; \
    int int_mode = (strchr(mode, 'w') || strchr(mode, '+'))  ? O_WRONLY : O_RDONLY; \
    if (pre_open(path, int_mode)) { \
        static FILE* (*prev)(const char *, const char *, FILE *); \
        if (!prev) { \
            prev = (FILE* (*)(const char *, const char *, FILE *)) get_real_addr(function); \
        } \
        if (prev) { \
            result = prev(path, mode, stream); \
            if (result) { \
                post_open(path, int_mode); \
            } \
        } else { \
            trace("Could not find original \"%s\" function\n", #function); \
            errno = EFAULT; \
            result = NULL; \
        } \
    } \
    trace("%s %s -> %d\n", #function, path, result); \
    inside_open--; \
    return result;
    //result ? -12345 : fileno(result)

FILE *freopen(const char *path, const char *mode, FILE *stream) {
    real_freopen(freopen, path, mode, stream)
}

#if _FILE_OFFSET_BITS != 64
FILE *freopen64(const char *path, const char *mode, FILE *stream) {
    real_freopen(freopen64, path, mode, stream)
}
#endif

// TODO: int openat(int fd, const char *path, int flags, ...);
// TODO: int openat64(int fd, const char *path, int flags, ...);


