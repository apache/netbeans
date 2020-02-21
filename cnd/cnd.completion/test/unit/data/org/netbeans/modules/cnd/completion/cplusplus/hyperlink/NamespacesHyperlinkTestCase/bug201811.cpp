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

struct bug201811_A {
    int n;
    struct B {
        int m;
    };
};

namespace bug201811_std1 {
    namespace tr2 {
        using ::bug201811_A;
        bug201811_A a1;
        namespace tr1 {
            using tr2::bug201811_A;
        }
    }
}

int bug201811_main() {
//    bug201811_std1::tr2::tr1::A x;
//    int n = x->n;
    bug201811_std1::tr2::bug201811_A x2;
    bug201811_std1::tr2::bug201811_A::B x3;
    bug201811_std1::tr2::tr1::bug201811_A::B x4;
    x2.n;
    x3.m;
    x4.m;

    return 0;
}
