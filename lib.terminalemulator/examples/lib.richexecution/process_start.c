/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <signal.h>

#include <sys/ioctl.h>

/*
 * 
 */
int main(int argc, char* argv[])
{
    int cx;
    int nopt = 1;       /* account for argv[0] */
    int saved_stdout = -1;
    int saved_errno;

    char *pty = NULL;
    char *executable = NULL;

    printf("PID %d\n", getpid());

    /* process options */
    for (cx = 1; cx < argc; cx++) {
        if (strcmp(argv[cx], "-pty")== 0) {
            cx++;
            if (argv[cx] == NULL || argv[cx][0] == '\0') {
                printf("ERROR missing pty after -pty\n");
                exit(-1);
            }
            pty = argv[cx];
            nopt += 2;
        } else if (argv[cx][0] == '-') {
            printf("ERROR unrecognized option '%s'\n", argv[cx]);
            exit(-1);
        } else {
            break;
        }
    }
    argv += nopt;
    argc -= nopt;
    /* now argv points to the executable */

    if (argc == 0) {
        printf("ERROR missing executable\n");
        exit(-1);
    }

    executable = argv[0];
    /* now argv points to <argv0> */

    if (!pty) {
        printf("ERROR -pty required\n");
        exit(-1);
    }

    if (pty) {
        int pty_fd;

        // Remember stdout so that if exec fails we can still output.
        // Mark the remembered fd as close-on-exec so that it's not
        // inherited past the exec.
        saved_stdout = fcntl(1, F_DUPFD, 3);
        fcntl(saved_stdout, F_SETFD, FD_CLOEXEC);

        // If we don't do this ^C/^Z etc won't work.
        // We used to use 'setpgrp()' but it was only effective on Solaris
        // and not Linux. 'setsid()' seems to work for both.
        if (setsid() == -1) {
            printf("ERROR setsid() failed -- %s\n", strerror(errno));
            exit(-1);
        }

        // Ensure SIGINT isn't being ignored
        struct sigaction act;
        sigaction(SIGINT, NULL, &act);
        act.sa_handler = SIG_DFL;
        sigaction(SIGINT, &act, NULL);

        pty_fd = open(pty, O_RDWR);
        if (pty_fd == -1) {
            printf("ERROR cannot open pty \"%s\" -- %s\n",
                pty, strerror(errno));
            exit(-1);
        }

	// setsid() leaves us w/o a controlling terminal.
	// On Linux and Solaris the first open makes whatever we opened
	// our controlling terminal.
	// On BSD/Mac we need to explicitly assign a controlling terminal
	// using TIOCSCTTY. It does no harm on Linux either.

#if defined(TIOCSCTTY)
	if (ioctl(pty_fd, TIOCSCTTY, 0) == -1) {
            printf("ERROR ioctl(TIOCSCTTY) failed on \"%s\" -- %s\n",
                pty, strerror(errno));
            exit(-1);
	}
#endif

        // Flush out the PID message before we take away stdout
        fflush(stdout);

        // redirect stdio through the pty
        dup2(pty_fd, 0);
        dup2(pty_fd, 1);
        dup2(pty_fd, 2);
        close(pty_fd);
    }

    execvp(executable, argv);

    // we get here only if execvp fails
    saved_errno = errno;        // save errno around close() and dup()

    if (saved_stdout != -1) {
        // restore stdout so the below printf works
        close(1);
        dup(saved_stdout);
    }
    printf("ERROR exec failed -- %s\n", strerror(saved_errno));
    return -1;
}

