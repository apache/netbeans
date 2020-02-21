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

typedef struct bug200115_a {
    struct bug200115_c *i, *j;
} bug200115_aa;
typedef struct bug200115_b {
    union bug200115_d *k,*l;
} bug200115_bb;
int bug200115_main(int argc, char** argv) {   
    long long z;
    bug200115_aa *x;
    bug200115_bb *y;
    &((z + 1) & z ? x : (bug200115_aa *)y->k)->i;
    return 0;
}