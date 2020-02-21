    /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

#include <stdio.h>
#include <stdlib.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <fcntl.h>

#include "rfs_protocol.h"
#include "rfs_util.h"
#include "rfs_preload_socks.h"

/** SOCKET_ERROR means that we failed to open a socket */
#define SOCKET_ERROR -1

/** SOCKET_UNINITIALIZED means unitialized */
#define SOCKET_UNINITIALIZED -2

/** Socked descriptor. */
static __thread int _sd __attribute__ ((aligned (16))) = SOCKET_UNINITIALIZED;

//void trace_sd(const char* text) {
//    trace("trace_sd (%s) _sd is %d %X\n", text, _sd, &_sd);
//}

/**
 * as well as open syscall, returns
 * -1 if an error occurred, or
 * non-negative integer in the case of success
 */
static int open_socket() {
    int port = default_controller_port;
    char *env_port = getenv("RFS_CONTROLLER_PORT");
    if (env_port) {
        port = atoi(env_port);
    }
    char* hostname = "localhost";
    char *env_host = getenv("RFS_CONTROLLER_HOST");
    if (env_host) {
        hostname = env_host;
    }
    trace("Connecting %s:%d\n", hostname, port);
    struct hostent *hp;
    if ((hp = gethostbyname(hostname)) == 0) {
        perror("gethostbyname");
        return -1;
    }
    struct sockaddr_in pin;
    memset(&pin, 0, sizeof (pin));
    pin.sin_family = AF_INET;
    pin.sin_addr.s_addr = ((struct in_addr *) (hp->h_addr))->s_addr;
    pin.sin_port = htons(port);
    int sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd == -1) {
        perror("socket");
        return -1;
    }
    if (connect(sd, (struct sockaddr *) & pin, sizeof (pin)) == -1) {
        trace("error connecting remote controller: %s\n", strerror(errno)); // it reports pid, etc
        perror("connect");
        return -1;
    }
    // configure script contains a weird command: exec 7<&0 </dev/null 6>&1
    // so using descriptor 6 or 7 can get us into trouble
    const int min_sock = 64; // an arbitrary number big enougth to not coincide with 6, 7, etc
    if (sd < min_sock) {
        int new_sd = fcntl(sd, F_DUPFD, min_sock);
        trace("configure workaround: duplicating descriptor %d to %d\n", sd, new_sd);
        if (new_sd != -1) {
            close(sd);
            sd = new_sd;
        }
    }
    return sd;
}

/**
 * as well as open syscall, returns
 * -1 if an error occurred, or
 * non-negative integer in the case of success
 */
__attribute__ ((visibility ("hidden")))
int get_socket(int create) {
    // SOCKET_UNINITIALIZED means unitialized
    // SOCKET_ERROR means that we failed to open a socket
    if (!create || (_sd != SOCKET_ERROR && _sd != SOCKET_UNINITIALIZED)) {
        return _sd;
    }
    if (_sd == SOCKET_ERROR) {
        return -1;
    }
    if (_sd == SOCKET_UNINITIALIZED) {
        _sd = SOCKET_ERROR; // in the case of success, it will change
        trace_sd("opening socket (a)");
    }
    _sd = open_socket();
    trace_sd("opening socket (b)");
    if (_sd != -1) {
        char buf[32];
        sprintf(buf, "%d", getpid());
        trace("Sending handshake package (%s) to sd=%d\n", buf, _sd);
        enum sr_result res = pkg_send(_sd, pkg_handshake, buf);
        if (res == sr_reset) {
            report_error("Connection reset by peer when sending a handshake package\n");
        } else if (res == sr_failure) {
            perror("Error sending a handshake package");
        }
    }
    return _sd;
}

__attribute__ ((visibility ("hidden")))
void release_socket() {
    if (_sd != SOCKET_ERROR && _sd != SOCKET_UNINITIALIZED) {
        trace("closing socket _sd=%d &_sd=%X\n", _sd, &_sd);
        close(_sd);
        _sd = SOCKET_UNINITIALIZED;
        trace_sd("releasing socket");
    }
}
