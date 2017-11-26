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

