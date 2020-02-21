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

#ifndef _RFS_CONTROLLER_H
#define _RFS_CONTROLLER_H

static const int default_controller_port = 5555;

enum response_kind {
    response_ok = '1',
    response_failure = '0'
};

enum kind {
    pkg_null = '0',
    pkg_handshake = 'h',
    pkg_request = 'q',
    pkg_written = 'w',
    pkg_reply = 'r'
};

const char* pkg_kind_to_string(enum kind kind);

/**
 * The below is the representation of a package in program.
 * This does not mean its fields has exactly such size when passing via sockets.
 * That's pkg_send and pkg_recv that is responsibe for how the data is represented when passing via sockets.
 */
struct package {
    enum kind kind;
    char data[];
};

enum sr_result {
    sr_success = 1,
    sr_failure = 0,
    sr_reset = -1 // reset by peer
};

enum sr_result pkg_send(int sd, enum kind kind, const char* buf);

enum sr_result pkg_recv(int sd, struct package* p, short max_data_size);

#endif // _RFS_CONTROLLER_H
