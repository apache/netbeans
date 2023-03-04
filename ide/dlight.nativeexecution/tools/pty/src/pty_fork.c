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

#include "pty_fork.h"
#include "error.h"

#include <unistd.h>
#include <termios.h>
#include <stdarg.h>
#ifdef SOLARIS
#include <stropts.h>
#else
#include <sys/ioctl.h>
#endif
#include <errno.h>
#include <fcntl.h>
#include <signal.h>

#ifdef __CYGWIN__
//added for compatibility with cygwin 1.5

int posix_openpt(int flags) {
    return open("/dev/ptmx", flags);
}

#endif
extern int grantpt(int);
extern int unlockpt(int);
extern char *ptsname(int);

static void dup_fd(int pty_fd);
static int ptm_open(void);
static int pts_open(int masterfd);

int ptm_open(void) {
    int masterfd;

    /*
     * O_NOCTTY (?) we'll be a group leader in any case (?)
     * So will get a controlling terminal.. O_NOCTTY - do we need it?
     * 
     */
    if ((masterfd = posix_openpt(O_RDWR | O_NOCTTY)) == -1) {
        return -1;
    }

    if (grantpt(masterfd) == -1 || unlockpt(masterfd) == -1) {
        close(masterfd);
        return -1;
    }

    return masterfd;
}

int pts_open(int masterfd) {
    int slavefd;
    char* name;

    if ((name = ptsname(masterfd)) == NULL) {
        close(masterfd);
        return -1;
    }

    if ((slavefd = open(name, O_RDWR)) == -1) {
        close(masterfd);
        return -1;
    }

#if defined (__SVR4) && defined (__sun)
    if (ioctl(slavefd, I_PUSH, "ptem") == -1) {
        close(masterfd);
        close(slavefd);
        return -1;
    }

    if (ioctl(slavefd, I_PUSH, "ldterm") == -1) {
        close(masterfd);
        close(slavefd);
        return -1;
    }

    if (ioctl(slavefd, I_PUSH, "ttcompat") == -1) {
        close(masterfd);
        close(slavefd);
        return -1;
    }
#endif

    return slavefd;
}

pid_t pty_fork(int *ptrfdm) {
    pid_t pid;
    char* name;
    int master_fd, pty_fd;
    struct termios termios;
    struct termios* ptermios = NULL;
    struct winsize wsize;
    struct winsize* pwinsize = NULL;

    // If we are in a terminal - get it's params and set them to 
    // a newly allocated one...

    if (isatty(STDIN_FILENO)) {
        ptermios = &termios;
        if (tcgetattr(STDIN_FILENO, ptermios) == -1) {
            err_sys("tcgetattr failed");
        }

        pwinsize = &wsize;
        if (ioctl(STDIN_FILENO, TIOCGWINSZ, pwinsize) == -1) {
            err_sys("ioctl(TIOCGWINSZ) failed");
        }
    }

    if ((master_fd = ptm_open()) < 0) {
        err_sys("ERROR: ptm_open() failed [%d]\n", master_fd);
    }

    if ((name = ptsname(master_fd)) == NULL) {
        close(master_fd);
        return -1;
    }

    // Put values to the output params
    *ptrfdm = master_fd;

    if ((pid = fork()) < 0) {
        err_sys("fork failed");
        return (-1);
    }

    if (pid == 0) { /* child */
        if (setsid() < 0) {
            err_sys("setsid error");
        }

        if ((pty_fd = pts_open(master_fd)) < 0) {
            err_sys("can't open slave pty");
        }

        if (ptermios != NULL) {
            if (tcsetattr(pty_fd, TCSANOW, ptermios) == -1) {
                err_sys("tcsetattr(TCSANOW) failed");
            }
        }

        if (pwinsize != NULL) {
            if (ioctl(pty_fd, TIOCSWINSZ, pwinsize) == -1) {
                err_sys("ioctl(TIOCSWINSZ) failed");
            }
        }

        close(master_fd);
        dup_fd(pty_fd);

        return (0); /* child returns 0 just like fork() */
    } else { /* parent */
        return (pid); /* parent returns pid of child */
    }

}

pid_t pty_fork1(const char *pty) {
    pid_t pid;
    int pty_fd;

    if ((pid = fork()) < 0) {
        printf("FAILED");
        return (-1);
    }

    if (pid == 0) { /* child */
        /*
         * Create a new process session for this child.
         */
        if (setsid() < 0) {
            err_sys("setsid error");
        }

        /*
         * Open a terminal descriptor...
         */
        if ((pty_fd = open(pty, O_RDWR)) == -1) {
            err_sys("ERROR cannot open pty \"%s\" -- %s\n",
                    pty, strerror(errno));
        }

        /*
         * Associate pty_fd with I/O and close it
         */
        dup_fd(pty_fd);
        return (0);
    } else {
        /* 
         * parent just returns a pid of the child 
         */
        return (pid);
    }
}

static void dup_fd(int pty_fd) {
    // Ensure SIGINT isn't being ignored
    struct sigaction act;
    sigaction(SIGINT, NULL, &act);
    act.sa_handler = SIG_DFL;
    sigaction(SIGINT, &act, NULL);


#if defined(TIOCSCTTY) && !defined(__CYGWIN__) && !defined(__sun) && !defined(__APPLE__)
    // If this terminal is already the controlling 
    // terminal of a different session group then the 
    // ioctl fails with EPERM, unless the caller is root
    if (ioctl(pty_fd, TIOCSCTTY, 0) == -1) {
        printf("ERROR ioctl(TIOCSCTTY) failed on \"pty %d\" -- %s\n",
                pty_fd, strerror(errno));
        exit(-1);
    }
#endif

    /*
     * Slave becomes stdin/stdout/stderr of child.
     */
    if (dup2(pty_fd, STDIN_FILENO) != STDIN_FILENO) {
        err_sys("dup2 error to stdin");
    }

    if (dup2(pty_fd, STDOUT_FILENO) != STDOUT_FILENO) {
        err_sys("dup2 error to stdout");
    }

    if (dup2(pty_fd, STDERR_FILENO) != STDERR_FILENO) {
        err_sys("dup2 error to stderr");
    }

    close(pty_fd);
}
