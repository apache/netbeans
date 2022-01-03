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

enum bug190127_LD_HELPERS {
        LD_HELP_STAB,
        LD_HELP_ANNOTATE,
        LD_HELP_CCEXCEPT
};

int bug190127_main(int argc, char** argv) {
    static struct _helper_desc {
            const char* name;
            int mode;
    } tab[] = {
            [LD_HELP_STAB] = { .name = "lib1.so", .mode = 32 | 64 },
            [LD_HELP_ANNOTATE] = { .name = "lib2.so", .mode = 32 | 64 },
            [LD_HELP_CCEXCEPT] = { .name = "lib3.so.1", .mode = 32 | 64 },
    };
}
