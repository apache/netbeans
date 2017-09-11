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
