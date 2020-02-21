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

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

#define test_open(function) \
    char* filename_##function = "/tmp/" #function; \
    fprintf(stdout, "RFS_TEST_CLIENT %s\n", filename_##function); \
    int fd_##function = function(filename_##function, O_RDONLY); \
    if (fd_##function != -1) { \
        close(fd_##function); \
    }

#define test_fopen(function) \
    char* filename_##function = "/tmp/" #function; \
    fprintf(stdout, "RFS_TEST_CLIENT %s\n", filename_##function); \
    FILE* file_##function = function(filename_##function, "r"); \
    if (file_##function) { \
        fclose(file_##function); \
    }

int main(int argc, char** argv) {
    test_open(open)
#ifdef __sun__
    test_open(_open)
#endif
    test_open(open64)
#if _FILE_OFFSET_BITS != 64
#ifdef __sun__
    test_open(_open64)
#endif
#endif
    test_fopen(fopen)
    test_fopen(fopen64)
    return 0;
}
