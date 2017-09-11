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
