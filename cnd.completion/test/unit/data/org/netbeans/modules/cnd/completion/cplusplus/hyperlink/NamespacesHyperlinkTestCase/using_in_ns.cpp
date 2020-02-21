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

namespace N159223 {
    namespace N2 {
        struct AA {
            typedef int TTT;
            int i;
        };
    }
    using N2::AA;
}
namespace N159223 {
    struct B : public AA {
        TTT t;
    };
}

namespace N159308 {
    namespace N2 {
        struct AAA {
        };
    }
}
namespace N159308 {
    namespace N3 {
        using N2::AAA;
        void foo() {
            AAA a;
        }
    }
}
