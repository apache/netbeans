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

