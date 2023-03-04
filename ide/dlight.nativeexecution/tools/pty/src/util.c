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
