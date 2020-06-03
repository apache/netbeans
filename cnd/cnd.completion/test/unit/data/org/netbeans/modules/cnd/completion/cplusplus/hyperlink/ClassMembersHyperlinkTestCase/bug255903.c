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

enum path255903 {BASE255903, INCLUDE255903};
typedef struct _ExtDescription255903 {
    enum path255903 ebase255903;
} PathDescription255903;
struct _Lib255903 {
    struct flags_t255903 {
        _Bool no_static255903;
    } flags255903;
    PathDescription255903 *path_desc255903;
};
typedef struct _Lib255903 lib_t255903;
static lib_t255903 LibraryTable255903[] = {{
        .path_desc255903 = (PathDescription255903[]){
            { .ebase255903 = INCLUDE255903 },
            { .ebase255903 = BASE255903 },},
        .flags255903 = { .no_static255903 = 1},
    }};

int main255903(int argc, char** argv) {
    LibraryTable255903[0].path_desc255903[0];
    return 0;  
}