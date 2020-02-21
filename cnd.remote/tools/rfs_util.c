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

#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

#include "rfs_util.h"

__attribute__ ((visibility ("hidden")))
void report_error(const char *format, ...) {
    va_list args;
    va_start (args, format);
    vfprintf(stderr, format, args);
    va_end (args);
}

__attribute__ ((visibility ("hidden")))
bool trace_flag = 0;

static const char* pattern = "%u #%s[%d]: ";
static const char* prefix = "";
static FILE *trace_file;

static unsigned long get_timestamp() {
    struct timespec tp;
    clock_gettime(CLOCK_REALTIME, &tp);
    return tp.tv_sec*1000000000+tp.tv_nsec;
}

__attribute__ ((visibility ("hidden")))
void _trace(const char *format, ...) {
    if (trace_flag) {
        if (!trace_file) {
            trace_file = stderr;
        }
        fprintf(trace_file, pattern, get_timestamp(), prefix, getpid());
        va_list args;
        va_start (args, format);
        vfprintf(trace_file, format, args);
        va_end (args);
        fflush(trace_file);
    }
}

__attribute__ ((visibility ("hidden")))
void _trace_startup(const char* _prefix, const char* env_var, const char* binary) {
    if (trace_flag) {
        prefix = _prefix;
        char *file_name = env_var ? getenv(env_var) : NULL;
        binary = binary ? binary : "";
        if (file_name) {
            trace_file = fopen(file_name, "a");
            if (trace_file) {
                fprintf(stderr, "Redirecting trace to %s\n", file_name);
                fprintf(trace_file, "\n\n--------------------\n");
                fflush(trace_file);
            } else {
                fprintf(stderr, "Redirecting trace to %s failed.\n", file_name);
                trace_file = stderr;
            }
        } else {
            trace_file = stderr;
        }
        char dir[PATH_MAX + 1];
        getcwd(dir, sizeof dir);
        trace("%s started in %s\n", binary, dir);
    }
}

__attribute__ ((visibility ("hidden")))
void _trace_shutdown() {
    if (trace_flag) {
        if (trace_file && trace_file != stderr) {
            fclose(trace_file);
        }
    }
}

__attribute__ ((visibility ("hidden")))
void init_trace_flag(const char* env_var) {
    char *env = getenv(env_var);
    trace_flag = env && *env == '1';
}

__attribute__ ((visibility ("hidden")))
void report_unresolved_path(const char* path) {
    char pwd[PATH_MAX + 1];
    getcwd(pwd, sizeof pwd);
    report_error("Can not resolve path: %s  cwd: %s\n", path, pwd);
}


__attribute__ ((visibility ("hidden")))
void _trace_unresolved_path(const char* path, const char* action) {
    if (trace_flag) {
        char pwd[PATH_MAX + 1];
        getcwd(pwd, sizeof pwd);
        trace("Can not resolve path on %s: %s pwd: %s\n", action, path, pwd);
    }
}

__attribute__ ((visibility ("hidden")))
void _dbg_sleep(int time) {
    if (trace_flag) {
        trace("Sleeping %d sec...\n", time);
        sleep(time);
        trace("Awoke\n");
    }
}

// NB: don't use malloc_wrapper in preload!!!
void *malloc_wrapper(size_t size) {
    void *p = malloc(size);
    if (!p) {
        report_error("Not enough of memory for auto-copy harness\n");
        exit(12);
    }
    return p;
}
