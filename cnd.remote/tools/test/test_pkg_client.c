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

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

#include "../rfs_filedata.h"
#include "../rfs_protocol.h"
#include "../rfs_util.h"
#include "../rfs_controller.h"
#include "../rfs_preload_socks.h"

int main(int argc, char** argv) {
    trace_startup("PKG_TEST", 0);
    int sd = get_socket(true);
    if (sd <= 0) {
        perror("error getting socket");
        return 1;
    }
    const char* path = "mypath";
    trace("Sending \"%s\" to sd=%d\n", path, sd);
    enum sr_result send_res = pkg_send(sd, pkg_request, path);
    if (send_res == sr_failure) {
        perror("send");
    } else if (send_res == sr_reset) {
        perror("Connection reset by peer when sending request");
    } else { // success
        trace("Request for \"%s\" sent to sd=%d\n", path, sd);
        const int maxsize = 256;
        char buffer[maxsize + sizeof(int)];
        struct package *pkg = (struct package *) &buffer;
        enum sr_result recv_res = pkg_recv(sd, pkg, maxsize);
        if (recv_res == sr_failure) {
            perror("Error receiving response");
        } else if (recv_res == sr_reset) {
            perror("Connection reset by peer when receiving response");
        } else { // success
            if (pkg->kind == pkg_reply) {
                trace("Got %s for %s, sd=%d\n", pkg->data, path, sd);
                if (pkg->data[0] == response_ok) {
                    trace("OK\n");
                } else if (pkg->data[0] == response_failure) {
                    trace("FAILURE\n");
                } else {
                    trace("Protocol error, sd=%d\n", sd);
                }
            } else {
                trace("Protocol error: get pkg_kind %d instead of %d\n", pkg->kind, pkg_reply);
            }
        }
    }
    return 0;
}
