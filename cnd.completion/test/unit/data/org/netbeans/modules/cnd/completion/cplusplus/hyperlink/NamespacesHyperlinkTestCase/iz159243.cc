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

namespace iz159243_N1 {
    namespace iz159243_N2 {
        void iz159243_foo() {
        }
    }
    namespace iz159243_N2A = iz159243_N2;
}
using namespace iz159243_N1::iz159243_N2A;
int iz159243_main() {
    iz159243_foo();
    return 0;
}

namespace iz159243_N0 {
    namespace iz159243_N1 {
        namespace iz159243_N2 {
            void iz159243_foo2() {
            }
        }
        namespace iz159243_N2A = iz159243_N2;
    }
}
using namespace iz159243_N0::iz159243_N1::iz159243_N2A;
int iz159243_main2() {
    iz159243_foo2();
    return 0;
}