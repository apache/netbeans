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

#include "file.h"

static int foo() {
    struct A* s;
    void *v;
    s = new A();
    v = s;
    ((struct A *) v)->f2(); // <-- code completion listbox wrong
    static_cast<struct A *> (v)->f2(); // <-- code completion listbox wrong
    return 0;
}

static int foo2() {
    class A* c;
    void *v;
    c = new A();
    v = c;
    ((class A *)v)->f2(); // <-- code completion listbox wrong
    static_cast<class A *> (v)->f2(); // <-- code completion listbox wrong
    return 0;
}
