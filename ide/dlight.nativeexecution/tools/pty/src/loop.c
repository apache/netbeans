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

#include "util.h"
#include "loop.h"
#include "error.h"
#include <poll.h>
#include <sys/termios.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "stdio.h"

#ifndef INFTIM
#define INFTIM  -1
#endif

#ifdef __APPLE__

int loop(int master_fd) {
    ssize_t n;
    char buf[BUFSIZ];
    int select_result;
    fd_set read_set;

    for (;;) {
        FD_ZERO(&read_set);
        FD_SET(STDIN_FILENO, &read_set);
        FD_SET(master_fd, &read_set);
        select_result = select(master_fd + 1, &read_set, NULL, NULL, NULL);

        // interrupted select is ignored - see CR 7086177
        if (select_result == -1 && errno == EINTR) {
            continue;
        }

        if (select_result == -1) {
            err_sys("poll failed\n");
        }

        if (FD_ISSET(STDIN_FILENO, &read_set)) {
            if ((n = read(STDIN_FILENO, buf, BUFSIZ)) == -1) {
                err_sys("read from stdin failed\n");
            }

            if (n == 0) {
                break;
            }

            if (write(master_fd, buf, n) == -1) {
                err_sys("write to master failed\n");
            }
        }

        if (FD_ISSET(master_fd, &read_set)) {
            if ((n = read(master_fd, buf, BUFSIZ)) == -1) {
                err_sys("read from master failed\n");
            }

            if (n == 0) {
                break;
            }

            if (write(STDOUT_FILENO, buf, n) == -1) {
                err_sys("write to stdout failed\n");
                exit(1);
            }
        }
    }

    return 0;
}

#else

#define FRAGSIZ 1000

int loop(int master_fd) {
    ssize_t n;
    char buf[FRAGSIZ];
    struct pollfd fds[2];
    
    struct buffer to_master;
    to_master.offset = 0;
    to_master.length = 0;

    fds[0].fd = STDIN_FILENO;
    fds[0].events = POLLIN;
    fds[0].revents = 0;
    fds[1].fd = master_fd;
    fds[1].events = POLLIN;
    fds[1].revents = 0;

    int poll_result;

    for (;;) {
        poll_result = poll((struct pollfd*) & fds, 2, INFTIM);

        // interrupted poll is ignored - see CR 7086177
        if (poll_result == -1 && errno == EINTR) {
            continue;
        }

        if (poll_result == -1) {
            err_sys("poll() failed in main_loop");
        }

        if (fds[1].revents & POLLIN) {
            if ((n = read(master_fd, buf, FRAGSIZ)) == -1) {
#ifdef __CYGWIN__
                // On Windows master_fd is invalid here
                // (bug #252202)
                close(master_fd);
                return 0;
#else
                err_sys("read from master failed\n");
#endif
            }

            if (n == 0) {
                break;
            }

            if (writen(STDOUT_FILENO, buf, n) == -1) {
                err_sys("write to stdout failed\n");
            }
        }

        if (to_master.offset != to_master.length) {
            int nwritten = writen_no_block(master_fd, &to_master);
            if (nwritten == -1) {
                err_sys("write to master failed\n");
            }
        } else if (fds[0].revents & POLLIN) {
            if ((n = read(STDIN_FILENO, to_master.buf, FRAGSIZ)) == -1) {
                err_sys("read from stdin failed");
            }

            if (n == 0) {
#ifdef __CYGWIN__
                // On Windows when calling process is killed,
                // POLLIN flag is set, not POLLHUP.
                // So behave as if we have received POLLHUP in this case...
                close(master_fd);
                return 1;
#endif
                break;
            }

            to_master.offset = 0;
            to_master.length = n;

            int nwritten = writen_no_block(master_fd, &to_master);
            if (nwritten == -1) {
                err_sys("write to master failed\n");
            }
        }

        if (fds[1].revents & POLLHUP) {
            break;
        }

        if (fds[0].revents & POLLHUP) {
            // STDIN END is broken... 
            // Cannot just break at this point as 'child' process still alive.
            // [will hung on waitpid later.. ]
            // So will close the MASTER END => this causes a hangup to occur on 
            // the other end of the pipe.
            // no data is drained in this case...
            close(master_fd);
            return 1;
        }
    }

    return 0;
}
#endif


