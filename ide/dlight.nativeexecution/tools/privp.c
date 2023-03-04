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

#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#ifdef SOLARIS
#include <stropts.h>
#else
#include <sys/ioctl.h>
#endif
#include <stdio.h>
#include <errno.h>

#define MAX_PASSWORD_LENGTH 64
#define MAX_CMD_LENGTH 256

void usage() {
    fprintf(stderr, "Usage: \n");
    fprintf(stderr, "   ./privp user privs pid\n");
}

/*
 * Kind of wrapper for /sbin/su command.
 * Usage:
 *    privp user privs pid
 *
 * As an argument it takes pid of process that needs to get privileges privs.
 * Then it reads password (to pass to /sbin/su) from stdin
 *
 */

int main(int argc, char** argv) {
    if (argc < 4) {
        usage();
        exit(1);
    }
    
    char readPassword = 1;
    
    /*
     * setsid(2) - creates a new session, if the  calling process
     * is not a process group leader. Upon return the calling process
     * will be the session leader of this new session,
     * will be the process group leader of a new process group,
     * and will have no controlling terminal.
     */

    pid_t gid = setsid();

    if (gid == -1) {
        // The calling process  is  already  a  process group  leader
        // We already have controlling terminal (?)
        readPassword = 0;
    }

    /*
     * /dev/ptmx - a character file used to create a pseudo-terminal master
     * and slave pair
     */

    int master_fd = open("/dev/ptmx", O_RDWR);
    if (master_fd == -1) {
        fprintf(stderr, "Cannot open /dev/ptmx\n");
        perror(NULL);
        exit(1);
    }

    /*
     * Before opening the pseudo-terminal slave, you must pass the master's
     * file descriptor to grantpt(3) and unlockpt(3).
     */

    if (grantpt(master_fd) == -1) {
        fprintf(stderr, "grantpt() failed\n");
        perror(NULL);
        exit(1);
    }

    if (unlockpt(master_fd) == -1) {
        fprintf(stderr, "unlockpt() failed\n");
        perror(NULL);
        exit(1);
    }


    char *slave_name = ptsname(master_fd);

    if (slave_name == NULL) {
        fprintf(stderr, "Cannot get name of slave pseudo-terminal device\n");
        perror(NULL);
        exit(1);
    }

    int slave_fd = open(slave_name, O_RDWR);

#if defined (__SVR4) && defined (__sun)
    /*
     * ptem(7M) - STREAMS Pseudo Terminal Emulation module
     * ptem is a STREAMS module that, when used in conjunction with
     * a line discipline and pseudo terminal driver, emulates a terminal.
     *
     * The ptem module must be pushed onto  the slave side of a pseudo terminal
     * STREAM, before the ldterm(7M) module is pushed.
     *
     */
    
    ioctl(slave_fd, I_PUSH, "ptem");

    /*
     * ldterm(7M) - standard STREAMS terminal line discipline module
     *
     * The ldterm STREAMS module provides most  of  the  termio(7I)
     * terminal  interface.
     */

    ioctl(slave_fd, I_PUSH, "ldterm");

    /*
     * Read password from stdin and write it to master_fd ...
     */
#endif
    if (readPassword == 1) {
        char pwd[MAX_PASSWORD_LENGTH];
        int passwdLength = read(STDIN_FILENO, pwd, MAX_PASSWORD_LENGTH);

        int result = write(master_fd, pwd, passwdLength);

        // Clear password...
        memset(pwd, 0, MAX_PASSWORD_LENGTH);

        if (result == -1) {
            fprintf(stderr, "Cannot redirect pwd\n");
            perror(NULL);
            exit(1);
        }
    }

    char command[MAX_CMD_LENGTH];
    sprintf(command, "/bin/ppriv -s A+%s %s", argv[2], argv[3]);

    /*
     * exec(2) -
     * ...
     * The new process also inherits the following attributes  from
     * the calling process:
     * ...
     * session membership
     * controlling terminal
     * ...
     */

    execlp("/sbin/su", "-", argv[1], "-c", command, NULL);
    
    return (EXIT_SUCCESS);
}

