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

#if !defined (__SVR4) || !defined (__sun)
#define _XOPEN_SOURCE 600
#define _BSD_SOURCE
#include <termios.h>
#else
#include <stropts.h>
#endif

#if defined(TIOCSCTTY)
#include <sys/ioctl.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/termios.h>

static pid_t pty_fork();

#ifdef __CYGWIN__
//added for compatibility with cygwin 1.5
#define WCONTINUED 0
#endif

int main(int argc, char* argv[]) {
    pid_t pid, w;
    int status;
    char *pty = NULL;
    int c;
    int err_flag = 0;
    int close_term = 0;

    while (pty == NULL && (c = getopt(argc, argv, "qp:")) != EOF) {
        switch (c) {
            case 'p':
                pty = optarg;
                break;
            case 'q':
                close_term = 1;
                break;
            default:
                err_flag++;
                break;
        }
    }

    if (err_flag || (optind >= argc)) {
        printf("ERROR: Usage: process_start [-q] -p pty command [arg ...]\n");
        exit(1);
    }

    if (!pty) {
        printf("ERROR: -p required\n");
        exit(-1);
    }

    pid = fork();

    if (pid == -1) {
        printf("ERROR: fork failed\n");
        exit(1);
    }

    int pty_fd = open(pty, O_RDWR);

    if (pid == 0) {
        int saved_stdout = -1;
        int saved_errno;

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

        // redirect stdio through the pty
        dup2(pty_fd, 0);
        dup2(pty_fd, 1);
        dup2(pty_fd, 2);
        close(pty_fd);

        if (execvp(argv[optind], &argv[optind]) == -1) {
            printf("ERROR: %s\n", argv[optind]);
            exit(1);
        }

        saved_errno = errno; // save errno around close() and dup()

        if (saved_stdout != -1) {
            // restore stdout so the below printf works
            close(1);
            dup(saved_stdout);
        }

        printf("ERROR exec failed -- %s\n", strerror(saved_errno));
    }

    printf("%d\n", pid);
    // Flush out the PID message before we take away stdout
    fflush(stdout);

    w = waitpid(pid, &status, WUNTRACED | WCONTINUED);

    if (close_term) {
#if defined _XOPEN_STREAMS && _XOPEN_STREAMS != -1
        tcsendbreak(pty_fd, 0);
#endif
    }

    if (w != -1 && WIFEXITED(status)) {
        exit(WEXITSTATUS(status));
    }


    exit(EXIT_FAILURE);
}
