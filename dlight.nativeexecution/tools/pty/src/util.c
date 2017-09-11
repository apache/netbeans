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

#include "util.h"
#include "poll.h"
#include "error.h"

ssize_t writen(int fd, const void *ptr, size_t n) {
    const char *pos = ptr;
    size_t nleft = n;
    ssize_t nwritten;

    while (nleft > 0) {
        if ((nwritten = write(fd, pos, nleft)) < 0) {
            if (nleft == n)
                return (-1); /* error, return -1 */
            else
                break; /* error, return amount written so far */
        } else if (nwritten == 0) {
            break;
        }
        nleft -= nwritten;
        pos += nwritten;
    }
    return (n - nleft); /* return >= 0 */
}

ssize_t writen_no_block(int fd, struct buffer *ptr) {
    size_t n = ptr->length - ptr->offset;
    size_t nleft = n;
    ssize_t nwritten;

    int ret;
    struct pollfd block[1];
    block[0].fd = fd;
    block[0].events = POLLOUT;
    block[0].revents = 0;

    while (nleft > 0) {
        ret = poll((struct pollfd*) & block, 1, 1);
        if (ret == -1) {
            err_sys("polling in writen_no_block failed");
        }
        if (!(block[0].revents & POLLOUT)) {
            break;
        }
        if ((nwritten = write(fd, ptr->buf + ptr->offset, nleft)) < 0) {
            if (nleft == n)
                return (-1); /* error, return -1 */
            else
                break; /* error, return amount written so far */
        } else if (nwritten == 0) {
            break;
        }
        nleft -= nwritten;
        ptr->offset += nwritten;
    }
    return (n - nleft); /* return >= 0 */
}
