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

#include <stdio.h>

typedef long size_t;
typedef long* ptrdiff_t;

int main() {
    size_t st = sizeof(int);
    ptrdiff_t pt = 1;
    size_t* pst = &st;
    ptrdiff_t* ppt = &pt;
    printf("%zu %td", st, pt);
    printf("%zn %tn\n", pst, ppt);
    return 0;
}