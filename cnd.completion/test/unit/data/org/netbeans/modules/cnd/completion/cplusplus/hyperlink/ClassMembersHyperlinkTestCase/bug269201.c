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

struct AAA269201 {
    struct {
        int s_first;
        float s_second;
    };
    union {
        int u_first;
        float u_second;
    };
    struct {
        int xxx;
    } fld;
    int boo;
}; 

void bla269201() {
    struct AAA269201 var;
    var.s_first = 1;
    var.s_second = 1.0f;
    var.u_first = 1;
    var.u_second = 1.0f;
    var.fld.xxx = 1;
}