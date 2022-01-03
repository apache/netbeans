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

namespace iz147518_2 {
    template <class T1, class T2, bool b> class C {
    };

    template <class T1, class T2> class C<T1, T2, true > {
    public:
        int i;
    };

    template <class T1, class T2> class C<T1, T2, false > : public C<T1, T2, true > {
    public:
        using C<T1, T2, true > ::i; // unresolved

        int foo() {
            i++; // unresolved
        }
        static int z;
    };

    int main() {
        C<int, int, false> c;
        c.foo(); // unresolved
        C<int, int, false> c2;
        c2.i; // unresolved
        C<int, int, false>::z++; // unresolved
        return 0;
    }
}