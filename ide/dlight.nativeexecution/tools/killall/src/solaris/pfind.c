/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
