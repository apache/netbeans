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

#include "error.h"
#include "options.h"
#include <string.h>
#include <stdlib.h>
#include <signal.h>

extern int str2sig(const char *name, int *sig_ret);

static void setsig(options_t* params, char* opt, char* sigstr) {
    if (sigstr == NULL || sigstr[0] == '\0') {
        err_quit("missing signal after %s\n", opt);
    }

    if (strcmp("NULL", sigstr) == 0) {
        params->sig = 0;
    } else if (str2sig(sigstr, &params->sig) == -1) {
        err_quit("Unknown signal %s\n", sigstr);
    }
}

int readopts(int argc, char** argv, options_t* opts) {
    int idx;
    int nopt = 1;

    memset(opts, 0, sizeof (options_t));

    for (idx = 1; idx < argc; idx++) {
        if (argv[idx][0] == '-') {
            if (strcmp(argv[idx], "-p") == 0) {
                opts->scope = S_PID;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-g") == 0) {
                opts->scope = S_PGID;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-s") == 0) {
                opts->scope = S_SID;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-e") == 0) {
                opts->scope = P_ENV;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-q") == 0) {
                opts->scope = P_QUEUE;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-n") == 0) {
                opts->nosignal = 1;
                nopt += 1;
            } else {
                printf("ERROR unrecognized option '%s'\n", argv[idx]);
                exit(-1);
            }
        } else {
            break;
        }
    }

    return nopt;
}
