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
