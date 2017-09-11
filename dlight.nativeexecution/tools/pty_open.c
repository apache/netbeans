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

#if !defined (__SVR4) || !defined (__sun)
#define _XOPEN_SOURCE 600
#define _BSD_SOURCE
#include <termios.h>
#endif

#ifdef SOLARIS
#include <stropts.h>
#else
#include <sys/select.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <poll.h>
#include <errno.h>
#include <string.h>

static int ptm_open(void) {
    int masterfd;

    if ((masterfd = posix_openpt(O_RDWR)) == -1) {
        return -1;
    }

    if (grantpt(masterfd) == -1 || unlockpt(masterfd) == -1) {
        close(masterfd);
        return -1;
    }

    return masterfd;
}

static int pts_open(int masterfd) {
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
#else
    /*
        struct termios termios_p;
        tcgetattr(slavefd, &termios_p);
        cfmakeraw(&termios_p);
        tcsetattr(slavefd, TCSANOW, &termios_p);
     */
#endif

    return slavefd;
}

#ifdef __APPLE__

static void loop(int master_fd) {
    ssize_t n;
    char buf[BUFSIZ];
    int select_result;
    fd_set read_set;

    for (;;) {
        FD_ZERO(&read_set);
        FD_SET(STDIN_FILENO, &read_set);
        FD_SET(master_fd, &read_set);
        select_result = select(master_fd + 1, &read_set, NULL, NULL, NULL);

        if (select_result == -1) {
            printf("ERROR: poll failed\n");
            exit(1);
        }

        if (FD_ISSET(STDIN_FILENO, &read_set)) {
            if ((n = read(STDIN_FILENO, buf, BUFSIZ)) == -1) {
                printf("ERROR: read from stdin failed\n");
                exit(1);
            }

            if (n == 0) {
                break;
            }

            if (write(master_fd, buf, n) == -1) {
                printf("ERROR: write to master failed\n");
                exit(1);
            }
        }

        if (FD_ISSET(master_fd, &read_set)) {
            if ((n = read(master_fd, buf, BUFSIZ)) == -1) {
                printf("ERROR: read from master failed\n");
                exit(1);
            }

            if (n == 0) {
                break;
            }

            if (write(STDOUT_FILENO, buf, n) == -1) {
                printf("ERROR: write to stdout failed\n");
                exit(1);
            }
        }
    }
}

#else

static void loop(int master_fd) {
    ssize_t n;
    char buf[BUFSIZ];
    struct pollfd fds[2];

    fds[0].fd = STDIN_FILENO;
    fds[0].events = POLLIN;
    fds[0].revents = 0;
    fds[1].fd = master_fd;
    fds[1].events = POLLIN;
    fds[1].revents = 0;

    int poll_result;

    for (;;) {
        poll_result = poll((struct pollfd*) & fds, 2, /*INFTIM*/ -1);

        // interrupted poll is ignored - see CR 7086177
        if (poll_result == -1 && errno == EINTR) {
            continue;
        }

        if (poll_result == -1) {
            printf("pty_open: poll() failed in main_loop: %s\n", strerror(errno));
            exit(1);
        }

        if (fds[0].revents & POLLHUP || fds[1].revents & POLLHUP) {
            break;
        }

        if (fds[0].revents & POLLIN) {
            if ((n = read(STDIN_FILENO, buf, BUFSIZ)) == -1) {
                printf("pty_open: read from stdin failed: %s\n", strerror(errno));
                exit(1);
            }

            if (n == 0) {
                break;
            }

            if (write(master_fd, buf, n) == -1) {
                printf("pty_open: write to master failed: %s\n", strerror(errno));
                exit(1);
            }
        }

        if (fds[1].revents & POLLIN) {
            if ((n = read(master_fd, buf, BUFSIZ)) == -1) {
                printf("pty_open: read from master failed: %s\n", strerror(errno));
                exit(1);
            }

            if (n == 0) {
                break;
            }

            if (write(STDOUT_FILENO, buf, n) == -1) {
                printf("pty_open: write to stdout failed: %s\n", strerror(errno));
                exit(1);
            }
        }
    }
}
#endif

int main(int argc, char** argv) {
    int master_fd;
    int slave_fd;
    char* name;

    if ((master_fd = ptm_open()) == -1) {
        printf("pty_open: ptm_open() failed: %s\n", strerror(errno));
        exit(1);
    }

    if ((name = ptsname(master_fd)) == NULL) {
        close(master_fd);
        return -1;
    }

    // open slave to setup line discipline...
    if ((slave_fd = pts_open(master_fd)) == -1) {
        printf("pty_open: cannot open PTY slave: %s\n", strerror(errno));
        exit(1);
    }

    printf("PID: %d\n", getpid());
    printf("TTY: %s\n\n", name);
    fflush(stdout);

    loop(master_fd);

    return (EXIT_SUCCESS);
}

#ifdef __CYGWIN__
//added for compatibility with cygwin 1.5

int posix_openpt(int flags) {
    return open("/dev/ptmx", flags);
}
#endif
