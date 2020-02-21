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

#ifndef LIB_HEADER_202433_H
#define LIB_HEADER_202433_H

#include "other_lib_header.h"

struct EMPTY_MACRO_FROM_OTHER_INCLUDE Struct202433* pGlobal202433;

#ifdef MY_PLUS_PLUS
struct MyClass202433_One {
    void foo();
};
#else
struct MyClass202433_Two {
    void boo();
};
#endif

#endif

