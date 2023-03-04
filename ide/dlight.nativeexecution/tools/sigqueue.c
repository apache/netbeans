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
#include <signal.h>


void usage() {
    fprintf(stderr, "Usage: \n");
    fprintf(stderr, "   ./sigqueue pid signo value\n");
}

/*
 * CLI wrapper for sigqueue.
 * Usage:
 *    sigqueue pid signo value
 *
 */

int main(int argc, char** argv) {
    if (argc < 4) {
        usage();
        exit(1);
    }

    //setting pid
    pid_t pid = atoi(argv[1]);

    //setting signo
    int signo = atoi(argv[2]);

    // setting value
    union sigval value;
    value.sival_int = atoi(argv[3]);

    return sigqueue(pid, signo, value);
}

