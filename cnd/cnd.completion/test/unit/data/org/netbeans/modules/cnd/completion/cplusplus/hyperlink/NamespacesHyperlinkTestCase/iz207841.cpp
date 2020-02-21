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

namespace ns207841_1 {
    struct BBB207841;
    namespace ns207841_2 {
        struct CCCBBB207841 {
            const BBB207841* zoo();
        };
    }
}

namespace ns207841_1 {
    struct BBB207841 {
        void boo() const;
        void boo2() const {}
    };
    namespace ns207841_2 {
        struct SS207841 {
            void foo();
            void foo2() {}
        };
    } 
}

using namespace ns207841_1;
using namespace ns207841_2;

void BBB207841::boo() const {
    boo2();
}

void SS207841::foo() {
    foo2();
}

const BBB207841* CCCBBB207841::zoo() {
    const BBB207841* ptr;
    ptr->boo();
    return ptr;
}

int checkFunction207841() {
    SS207841 aa;
    aa.foo();
    BBB207841 bb;
    bb.boo();
}
