/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 */

#ifndef FS_SERVER_H
#define	FS_SERVER_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#include "util.h"

#ifdef	__cplusplus
extern "C" {
#endif

enum fs_request_kind {
    FS_REQ_LS = 'l',
    FS_REQ_RECURSIVE_LS = 'r',
    FS_REQ_STAT = 'S',
    FS_REQ_LSTAT = 's',
    FS_REQ_COPY = 'C',
    FS_REQ_MOVE = 'm',
    FS_REQ_QUIT = 'q',
    FS_REQ_SLEEP = 'P',
    FS_REQ_ADD_WATCH = 'W',
    FS_REQ_REMOVE_WATCH = 'w',
    FS_REQ_REFRESH = 'R',
    FS_REQ_DELETE = 'd',
    FS_REQ_DELETE_ON_DISCONNECT = 'D',
    FS_REQ_SERVER_INFO = 'i',
    FS_REQ_HELP = '?',
    FS_REQ_OPTION = 'o'
};

enum fs_response_kind {
    FS_RSP_LS = 'l',
    FS_RSP_RECURSIVE_LS = 'r',
    FS_RSP_ENTRY = 'e',
    FS_RSP_END = 'x',
    FS_RSP_CHANGE = 'c',
    FS_RSP_ERROR = 'E',
    FS_RSP_REFRESH = 'R',
    FS_RSP_SERVER_INFO = 'i'
};

typedef struct fs_request {
    int size;
    /** request kind */
    enum fs_request_kind kind : 8;
    /** request ID */
    int id;
    /** file name length */
    int len;
    /** zero-terminated absoulte file path */
    const char* path;
    /** second path len (0 if absent) */
    int len2;
    /** second path (NULL if absent) */
    const char* path2;
    char data[];

} fs_request;

/**
 * Response is textual.
 *
 * Responses can be single and multiple.
 * Single response describes one file (e.g. result of 'stat' request)
 * Multiple response describes list of files (e.g. result of 'ls' request)
 *
 * First fields (kind, id and length) are space separated
 * All figures are in decimal human readable format (e.g. "234")
 *
 * kind         response kind (1 char); for values, see see fs_response_kind
 *              1-character gap
 * id           the id of the request this response responds to
 *              1-character gap
 * count        number of elements in response
 *              1-character gap
 * length       file absolute path length
 *              1-character gap
 * abspath      absoulte path of the file this response refers to
 *
 * LF           line feed character
 *
 * Each response element has the following format:
 *
 * file_type    1-character file type
 *              1-character gap
 * access       9-character access (e.g. "rwxr-xr-x")
 *              1-character gap
 * user         numeric user id
 *              1-character gap
 * group        numeric group id
 *              1-character gap
 * length       file size
 *              1-character gap
 * modified     modification time in milliseconds since 01/01/1970, 00:00:00 GMT
 *              1-character gap
 * name_len     filename length (short name - without dir)
 *              1-character gap
 * name         file name
 *              1-character gap
 * link         symbolic link
 * LF           line feed character
 *
 * response format example
 * CORRECT: l 123 l my_link
 * INCORRECT: tmp rwxrwxrwx d 3 0 1380101169000 629500
 * ls -ln
 * drwxrwxrwx  79 0        3         632606 Sep 30 06:05 tmp
 *
 *
 *
 */
typedef struct fs_response {
    /** request kind */
    enum fs_response_kind kind : 8;
    char data[];
} fs_response;


#ifdef	__cplusplus
}
#endif

#endif	/* FS_SERVER_H */

