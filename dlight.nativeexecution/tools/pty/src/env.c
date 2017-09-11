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

#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <libgen.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <sys/fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include "env.h"
#include "util.h"

extern char** environ;

// Allocates a buffer and fills it with environment variables
// Sets total_size and count to appropriate values

static char* pack_env(int *total_size) {
    // first calculate a size of all envs
    int size = 0;
    int count = 0;
    char** env = environ;
    while (*env != NULL) {
        size += strlen(*env) + 1;
        count++;
        env++;
    }

    size += sizeof (int); // first int - number of variables 

    char *data = (char*) malloc(size);
    if (data == NULL) {
        return NULL;
    }

    *((int*) data) = count;

    char *p = data + sizeof (int);

    env = environ;
    while (*env != NULL) {
        strcpy(p, *env);
        p += strlen(*env) + 1;
        env++;
    }

    *total_size = size;
    return data;
}

int dumpenv(const char* fname) {
    int fd = open(fname, O_TRUNC | O_CREAT | O_WRONLY, S_IRUSR | S_IWUSR);
    if (fd == -1) {
        printf("Failed to open");
        return -1;
    }

    struct flock lock;
    lock.l_type = F_WRLCK;
    lock.l_whence = 0;
    lock.l_start = SEEK_SET;
    lock.l_len = 0;

    if (fcntl(fd, F_SETLK, &lock) == -1) {
        return (errno == EACCES || errno == EAGAIN) ? 1 : -1;
    }

    if (ftruncate(fd, 0) == -1) {
        return -1;
    }

    int total_size;
    char* packed_env = pack_env(&total_size);

    if (packed_env == NULL) {
        return -1;
    }

    if (writen(fd, packed_env, total_size) == -1) {
        return -1;
    }

    close(fd);
    return 0;
}

char** readenv(const char* fname) {
    int fd = open(fname, O_RDONLY);
    if (fd == -1) {
        return NULL;
    }

    struct flock lock;
    lock.l_type = F_RDLCK;
    lock.l_whence = 0;
    lock.l_start = SEEK_SET;
    lock.l_len = 0;

    if (fcntl(fd, F_SETLK, &lock) == -1) {
        return NULL;
        //        return (errno == EACCES || errno == EAGAIN) ? 1 : -1;
    }

    struct stat statbuf;

    if (fstat(fd, &statbuf) == -1) {
        return NULL;
    }

    int count;
    if (read(fd, &count, sizeof (count)) == -1) {
        return NULL;
    }

    int datasize = statbuf.st_size - sizeof (int);
    char* env_data = malloc(datasize);
    if (read(fd, env_data, datasize) == -1) {
        return NULL;
    }
    close(fd);

    char**env = malloc((count + 1) * sizeof (char*));
    char*p = env_data;
    for (int i = 0; i < count; i++) {
        env[i] = p;
        p += strlen(p) + 1;
    }

    return env;
}
