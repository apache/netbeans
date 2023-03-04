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
#include <sys/stat.h>

void usage() {
    fprintf(stderr, "Usage: \n");
    fprintf(stderr, "   ./stat filename\n");
}

/*
 * CLI wrapper for stat.
 * Usage:
 *    stat filename
 *
 */

int main(int argc, char** argv) {
    if (argc < 2) {
        usage();
        exit(1);
    }

    int err;

    #ifdef __linux__
        struct stat st;
    #else
        struct stat64 st;
    #endif


    #ifdef __linux__
        err = stat(argv[1], &st);
    #else
        err = stat64(argv[1], &st);
    #endif

    printf("inode: %ld\n", st.st_ino);
    printf("ctime: %ld\n\n", st.st_ctime * 1000);
    fflush(stdout);

    return err;
}

