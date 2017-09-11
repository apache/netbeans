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
