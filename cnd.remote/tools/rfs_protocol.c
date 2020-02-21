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

#include <sys/socket.h>

#include "rfs_util.h"
#include "rfs_protocol.h"

// Actual package layout:
//  char kind;
//  char[2] 2-bytes size representation (high byte first)
//  char[] data 0-32K bytes null-terminated string

__attribute__ ((visibility ("hidden")))
const char* pkg_kind_to_string(enum kind kind) {
    switch (kind) {
        case pkg_null:          return "pkg_null";
        case pkg_handshake:     return "pkg_handshake";
        case pkg_request:       return "pkg_request";
        case pkg_reply:         return "pkg_reply";
        case pkg_written:       return "pkg_written";
        default:                return "pkg_unknown";
    }
}

static int do_send(int sd, const unsigned char* buffer, int size) {
    int sent = 0;
    while (sent < size) {
        int sent_now = send(sd, buffer + sent, size - sent, 0);
        if (sent_now == -1) {
            return false;
        } else {
            sent += sent_now;
        }
    }
    return true;
}

__attribute__ ((visibility ("hidden")))
enum sr_result pkg_send(int sd, enum kind kind, const char* buf) {
    unsigned int size = strlen(buf) + 1;
    unsigned char header[3];
    header[0] = (char) kind;
    header[1] = (unsigned char) (size >> 8); // high byte
    header[2] = (unsigned char) (size); // low byte
    if (do_send(sd, header, sizeof header)) {
        if (do_send(sd, (unsigned char*)buf, size)) {
            return sr_success;
        }
    }
    return sr_failure;
}

__attribute__ ((visibility ("hidden")))
enum sr_result pkg_recv(int sd, struct package* p, short max_data_size) {
    // clear pkg
    p->kind = pkg_null;
    memset(p->data, 0, max_data_size);
    // get kind and size
    unsigned char header[3];
    int received;
    received = recv(sd, header, 3, 0); // 3-rd is for kind
    if (received == 0) { // normal peer shutdown
        return sr_reset; 
    } else if (received == -1) { // abnormal peer shutdown
        perror("Protocol error: error receiving package");
        return sr_reset;
    } else if (received != 3) {
        report_error("Protocol error: received %d bytes instead of 3\n", received);
        return (received == 0) ? sr_reset : sr_failure;
    }
    p->kind = (enum kind) header[0];
    int size =  ((0x0FF & header[1])) * 256 + ((0x0FF & header[2]));
    if (size > max_data_size) {
        //trace("pkg_recv: packet too large: %d\n", size);
        errno = EMSGSIZE;
        report_error("Protocol error: size too large: %d \n", size);
        return sr_failure;
    }
    received = recv(sd, p->data, size, 0);
    if (received == 0) {
        return sr_reset;
    }
    if (received != size) {
        //trace("pkg_recv: received %d instead of %d\n", received, size);
        report_error("Protocol error: received %d bytes instead of %d\n", received, size);
        return sr_failure;
    }
    return sr_success;
}
