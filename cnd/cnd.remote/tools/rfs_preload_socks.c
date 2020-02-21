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
