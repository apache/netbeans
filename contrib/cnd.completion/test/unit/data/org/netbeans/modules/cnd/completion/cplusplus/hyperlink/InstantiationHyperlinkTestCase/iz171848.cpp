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

struct iz171848_A {
    int i;
};

template <class T, class TT = iz171848_A> struct iz171848_B;

template <class T, class TT> struct iz171848_B {
    TT t;
};

int iz171848_main(int argc, char** argv) {
    iz171848_B<int> b;
    b.t.i++; // unresolved i
    return (0);
}

namespace iz171848_N {
    struct iz171848_A2 {
        int i;
    };

    template <class T, class TT = iz171848_A2> struct iz171848_B2;

    template <class T, class TT> struct iz171848_B2 {
        TT t;
    };

    int iz171848_main(int argc, char** argv) {
        iz171848_B2<int> b;
        b.t.i++; // unresolved i
        return (0);
    }
}