/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/* 
 * File:   execint.c
 * Author: ll155635
 *
 * Created on July 11, 2010, 3:10 PM
 */

#include <dlfcn.h>
#include <limits.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifdef __APPLE__
#include <unistd.h>
#endif
#include <errno.h>
#include <sys/stat.h>
#include <spawn.h>

#ifdef TRACE
#define LOG(args...) fprintf(stderr, ## args)
#else
#define LOG(...)
#endif

/****************************************************************/
extern char *getcwd (char *__buf, size_t __size);
static struct stat buffer;

static void __logprint(const char* fname, char *const argv[], ...) {
    char* tools = getenv("__CND_TOOLS__");
    if (tools == NULL) {
        LOG("\nBuildTrace ERROR: __CND_TOOLS__ is not set!!!\n");
        return;
    }
    char* filters = strdup(tools);

    char* log = getenv("__CND_BUILD_LOG__");
    if (log == NULL) {
        LOG("\nBuildTrace ERROR: __CND_BUILD_LOG__ is not set!!!\n");
        free(filters);
        return;
    }
    LOG("\nBuildTrace: TOOLS=%s\n\t    LOG=%s\n", filters, log);

    int shortName = 0;
    const char* key = strrchr(fname, '/');
    if (key == NULL) {
        key = fname;
        shortName = 1;
    } else {
        key++;
    }
    LOG("\nBuildTrace: key = %s\n", key);

    int found = 0;
    char* token;
    for(token = strtok(filters, ":"); token; token = strtok(NULL, ":")) {
        if(strcmp(token, key) == 0) {
            found = 1;
            break;
        }
    }
    free(filters);
    
    if (found) {
        if (shortName == 0) {
            int status = stat(fname, &buffer);
            if (status != 0) {
                return;
            }
        }
        
        LOG("\nBuildTrace: found %s\n", key);
        FILE* flog = fopen(log, "a");
        LOG("\nBuildTrace: opened file %s\n", log);

        if (flog == NULL) {
            LOG("\nBuildTrace ERROR: can not open %s!!!\n", log);
            return;
        }

        char *buf = malloc(1024);
        if (buf == NULL) {
            LOG("\nBuildTrace ERROR: can not get cwd!!!\n");
        } else {
            fprintf(flog, "called: %s\n", fname);
            getcwd(buf, 1024);
            fprintf(flog, "\t%s\n", buf);
            free(buf);
            char** par = (char**) argv;
            for (; *par != 0; par++) {
                fprintf(flog, "\t%s\n", *par);
            }
        }
        fprintf(flog, "\n");
        fflush(flog);
        fclose(flog);
        LOG("\nBuildTrace: log closed\n");
    }
    return;
}

#define ORIG(func) _orig_##func
#define QUOTE(nm) #nm

// dirty hack
#define PARG , char** arg
#define PENV  , char** arg, const char** env
#define PVAR  , ...
#define ARG , arg
#define ENV , arg, env

#ifdef __APPLE__
#define INSTRUMENT(func, param, actual) \
__attribute__ ((visibility ("hidden"))) \
int my_##func (const char * p_original param) { \
    LOG("BuildTrace: func1\n");\
    int prev_errno = errno; \
    char * p = strdup(p_original); \
    LOG("BuildTrace: %s called. PATH=%s\n", QUOTE(func), p); \
    __logprint(p actual); \
    errno = prev_errno; \
    int ret = func(p actual); \
    prev_errno = errno; \
    LOG("BuildTrace: %s returned\n", QUOTE(func)); \
    free(p); \
    errno = prev_errno; \
    return ret; \
}
#else
#define INSTRUMENT(func, param, actual) \
int func (const char * p_original param) { \
    LOG("BuildTrace: func1\n");\
    int prev_errno = errno; \
    char * p = strdup(p_original); \
    static int (* ORIG(func))(const char* p param) = NULL; \
    INIT(func); \
    LOG("BuildTrace: %s called. PATH=%s\n", QUOTE(func), p); \
    __logprint(p actual); \
    errno = prev_errno; \
    int ret = ORIG(func) (p actual); \
    prev_errno = errno; \
    LOG("BuildTrace: %s returned\n", QUOTE(func)); \
    free(p); \
    errno = prev_errno; \
    return ret; \
}
#endif

#define INIT(func) \
    if(!ORIG(func)) { \
        ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(ORIG(func) && ORIG(func)==func) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)-1 /*RTLD_NEXT*/, QUOTE(func)); \
        if(!ORIG(func)) \
            ORIG(func) = (typeof(ORIG(func)))dlsym((void*)0 /*RTLD_DEFAULT*/, QUOTE(func)); \
    }

#define GETENV

INSTRUMENT(execv, PARG, ARG)
INSTRUMENT(execve, PENV, ENV)
INSTRUMENT(execvp, PARG, ARG)

#define RETURN(f) return f(name, (char **)argv)
#ifdef __APPLE__
#define CONVERT(from_func, to_func) \
__attribute__ ((visibility ("hidden"))) \
int my_##from_func(char *name, const char *first, ...) { \
    LOG("BuildTrace: func2\n");\
    va_list args; \
    char* argv[128]; \
    char**  p; \
    char**  env; \
    va_start(args, first); \
    p = (char**)argv; \
    *p++ = (char*) first; \
    do { \
        *p = va_arg(args, char*); \
    } while(*p++); \
    GETENV; \
    va_end(args); \
    LOG("BuildTrace: %s converted to %s\n", QUOTE(from_func), QUOTE(to_func)); \
    RETURN(my_##to_func); \
}
#else
#define CONVERT(from_func, to_func) \
int from_func(char *name, const char *first, ...) { \
    LOG("BuildTrace: func2\n");\
    va_list args; \
    char* argv[128]; \
    char**  p; \
    char**  env; \
    va_start(args, first); \
    p = (char**)argv; \
    *p++ = (char*) first; \
    do { \
        *p = va_arg(args, char*); \
    } while(*p++); \
    GETENV; \
    va_end(args); \
    LOG("BuildTrace: %s converted to %s\n", QUOTE(from_func), QUOTE(to_func)); \
    RETURN(to_func); \
}
#endif
CONVERT(execl, execv)
CONVERT(execlp, execvp)

#undef RETURN
#undef GETENV
#define GETENV env = va_arg(args, char **)
#define RETURN(f) return f(name, (char **)argv, (const char**)env)

CONVERT(execle, execve)

#ifdef __APPLE__
typedef struct interpose_s {
    void *new_func;
    void *orig_func;
} interpose_t;
static const interpose_t interposers[] 
        __attribute__ ((section("__DATA, __interpose"))) = {
            {(void*)my_execv, (void*)execv},
            {(void*)my_execve, (void*)execve},
            {(void*)my_execvp, (void*)execvp},
            {(void*)my_execl, (void*)execl},
            {(void*)my_execlp, (void*)execlp},
            {(void*)my_execle, (void*)execle}
        };
#endif

int posix_spawn(
	pid_t * pid,
	const char * path,
	const posix_spawn_file_actions_t *file_actions,
	const posix_spawnattr_t * attrp,
	char *const *argv,
	char *const *envp) {
    LOG("BuildTrace: posix_spawn\n");
    static int (* _orig_posix_spawn)(pid_t *,
	const char *,
	const posix_spawn_file_actions_t *,
	const posix_spawnattr_t *,
	char *const*,
	char *const*) = 0;
    if (!_orig_posix_spawn) {
        _orig_posix_spawn = (typeof ( _orig_posix_spawn))dlsym((void*) - 1, "posix_spawn");
    }
    __logprint(path, argv, envp);
    return _orig_posix_spawn(pid, path, file_actions, attrp, argv, envp);   
}

int posix_spawnp(
	pid_t * pid,
	const char * file,
	const posix_spawn_file_actions_t *file_actions,
	const posix_spawnattr_t * attrp,
	char *const *argv,
	char *const *envp) {
    LOG("BuildTrace: posix_spawnp\n");
    static int (* _orig_posix_spawnp)(pid_t *,
	const char *,
	const posix_spawn_file_actions_t *,
	const posix_spawnattr_t *,
	char *const*,
	char *const*) = 0;
    if (!_orig_posix_spawnp) {
        _orig_posix_spawnp = (typeof ( _orig_posix_spawnp))dlsym((void*) - 1, "posix_spawnp");
    }
    __logprint(file, argv, envp);
    return _orig_posix_spawnp(pid, file, file_actions, attrp, argv, envp);   
}

static void
__attribute((constructor))
init_function(void) {
}

static void
__attribute((destructor))
fini_function(void) {
}
