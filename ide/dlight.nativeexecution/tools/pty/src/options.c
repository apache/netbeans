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

#include <string.h>
#include <stdlib.h>
#include "error.h"
#include "options.h"

int readopts(int argc, char** argv, options_t* opts) {
    int idx;
    int nopt = 1;
    int envsize = 0;

    memset(opts, 0, sizeof (options_t));

    for (idx = 1; idx < argc; idx++) {
        if (argv[idx][0] == '-') {
            if (strcmp(argv[idx], "--no-pty") == 0) {
                opts->nopty = 1;
                nopt += 1;
            } else if (strcmp(argv[idx], "--set-erase-key") == 0) {
                opts->set_erase_key = 1;
                nopt += 1;
            } else if (strcmp(argv[idx], "--redirect-error") == 0) {
                opts->redirect_error = 1;
                nopt += 1;
            } else if (strcmp(argv[idx], "--report") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing envfile after -report\n");
                }
                opts->reportfile = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "--dumpenv") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing envfile after --dumpenv\n");
                }
                opts->envfile = argv[idx];
                return argc; // pretend that everything is parsed ... 
            } else if (strcmp(argv[idx], "--") == 0) {
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "--readenv") == 0) {
                argv[idx] = "--";
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing envfile after --readenv\n");
                }
                opts->envfile = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "-p") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing pty after -p\n");
                }
                opts->pty = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "--dir") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing dir after --dir\n");
                }
                opts->wdir = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "--env") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing variable=value pair after --env\n");
                    exit(-1);
                }

                // Cannot put environment here as in case of fork these 
                // variables will affect us... 
                // Will do this only before real execv
                // putenv(argv[idx]);

                if (envsize == opts->envnum) {
                    envsize += 10;
                    opts->envvars = realloc(opts->envvars, sizeof (char*) * envsize);
                }

                opts->envvars[opts->envnum++] = argv[idx];
                nopt += 2;
            } else if (strcmp(argv[idx], "-e") == 0) {
                opts->noecho = 1;
                nopt += 1;
            } else if (strcmp(argv[idx], "-w") == 0) {
                opts->waitSignal = 1;
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
