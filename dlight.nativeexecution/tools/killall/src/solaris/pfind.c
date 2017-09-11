/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <procfs.h>
#include <fcntl.h>
#include <sys/param.h>
#include <limits.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <dirent.h>
#include "../pfind.h"

#define MAX_LEN 4048

#ifdef _LP64

static int getshift() {
    int test = 1;
    char *chk = (char *) &test;
    return (chk[0] == 1) ? 0 : 32;
}
#endif

pid_t* pfind(const char* magicenv) {

#ifdef _LP64
    int shift = getshift();
#endif

    pid_t *result = NULL;
    char fname[PATH_MAX];
    char envvar[MAX_LEN];
    char* pid;
    DIR *dirp;
    struct dirent *dentp;
    int fd, res = 0, ressize = 0;
    psinfo_t procinfo;

    if ((dirp = opendir("/proc")) == NULL) {
        return NULL;
    }

    while (dentp = readdir(dirp)) {
        if (dentp->d_name[0] < '0' || dentp->d_name[0] > '9') {
            continue;
        }

        pid = dentp->d_name;

        snprintf(fname, PATH_MAX, "/proc/%s/psinfo", pid);
        if ((fd = open(fname, O_RDONLY)) == -1) {
            continue; // next pid
        }

        if (read(fd, &procinfo, sizeof (psinfo_t)) == -1) {
            goto next_pid;
        }

        close(fd);

        uintptr_t envptr = procinfo.pr_envp;

        if (envptr == NULL) {
            continue; // next pid
        }

        snprintf(fname, PATH_MAX, "/proc/%s/as", pid);
        if ((fd = open(fname, O_RDONLY)) == -1) {
            continue; // next pid
        }

        off_t env_offset = 0;
        off_t next_offset = 0;

        if (lseek(fd, procinfo.pr_envp, SEEK_SET) == -1) {
            goto next_pid;
        }

        int pointer_size = procinfo.pr_dmodel == PR_MODEL_ILP32 ? 4 : 8;

        if (read(fd, &env_offset, pointer_size) == -1) {
            goto next_pid;
        }

        if (env_offset == 0) {
            goto next_pid;
        }

#ifdef _LP64
        if (sizeof (uintptr_t) == 8 && procinfo.pr_dmodel == PR_MODEL_ILP32) {
            env_offset = 0xFFFFFFFF & (env_offset >> shift);
        }
#endif

        do {
            if (read(fd, &next_offset, pointer_size) == -1) {
                goto next_pid;
            }

#ifdef _LP64
            if (sizeof (uintptr_t) == 8 && procinfo.pr_dmodel == PR_MODEL_ILP32) {
                next_offset = 0xFFFFFFFF & (next_offset >> shift);
            }
#endif

            int toread = next_offset == 0 ? MAX_LEN - 1 : next_offset - env_offset;
            if (toread > MAX_LEN) {
                toread = MAX_LEN - 1;
            }

            int sz;
            if ((sz = pread(fd, envvar, toread, env_offset)) == -1) {
                goto next_pid;
            }

            if (sz == 0) {
                goto next_pid;
            }

            if (strcmp(magicenv, envvar) == 0) {
                if (res >= ressize) {
                    ressize += 10;
                    result = realloc(result, ressize * sizeof (pid_t));
                }
                result[res++] = (pid_t) atol(pid);
                goto next_pid;
            }

            env_offset = next_offset;
        } while (env_offset != 0);
next_pid:
        (void) close(fd);
    }

    if (dirp != NULL) {
        (void) closedir(dirp);
    }

    if (res >= ressize) {
        ressize++;
        result = realloc(result, ressize * sizeof (pid_t));
    }

    result[res] = 0;

    return result;
}
