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

#include "typeinfo.h"
namespace iz162160 {
    using namespace std;

    struct A {

        virtual ~A() {
        }
    };

    struct B : A {
    };

    struct C {
    };

    struct D : C {
    };

    int main() {
        B bobj;
        A* ap = &bobj;
        A& ar = bobj;
        typeid (*ap).name();
        typeid (typeid(ar).name()).name();

        D dobj;
        C* cp = &dobj;
        C& cr = dobj;
        typeid (*cp).name();
        typeid (cr).name();
    }

    int bug219398_main(int argc, char** argv) {
        struct Node
        {
        } node1, node2;
        // "Unable to resolve identifier name" mark appear and source code format
        if (typeid (node1).name() == typeid (struct Node) .name()) {
        }

        return 0;
    }

}