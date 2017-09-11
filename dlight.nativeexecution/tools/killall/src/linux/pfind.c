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
#include <limits.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include "../pfind.h"

#define MAX_LEN 4048

static char *procdir = "/proc";

pid_t* pfind(const char* magicenv) {
    pid_t *result = NULL;
    char fname[PATH_MAX];
    char buffer[MAX_LEN];
    DIR *dirp;
    struct dirent *dentp;
    int fd, res = 0, ressize = 0;

    if ((dirp = opendir(procdir)) == NULL) {
        return NULL;
    }

    while (dentp = readdir(dirp)) {
        if (dentp->d_name[0] < '0' || dentp->d_name[0] > '9') {
            continue;
        }

        char* pid = dentp->d_name;

        snprintf(fname, PATH_MAX, "%s/%s/environ", procdir, pid);

        if ((fd = open(fname, O_RDONLY)) == -1) {
            continue; // iterate over /proc
        }

        int size;
        if ((size = read(fd, buffer, MAX_LEN)) == -1) {
            goto next;
        }

        char* p = buffer;

        while (p < buffer + size) {
            if (strcmp(magicenv, p) == 0) {
                if (res >= ressize) {
                    ressize += 10;
                    result = realloc(result, ressize * sizeof (pid_t));
                }
                result[res++] = (pid_t) atol(pid);
                break;
            }
            p += strlen(p) + 1;
        }
next:
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

