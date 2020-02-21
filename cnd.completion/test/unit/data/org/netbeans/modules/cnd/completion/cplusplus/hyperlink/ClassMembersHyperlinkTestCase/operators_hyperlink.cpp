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

namespace 148223 {

    class AND_A_B {
    public:

        int cc() {
            return 0;
        }
    };

    class AND_B_A {
    public:

        int c1() {
            return 0;
        }
    };

    class A {
    public:
        AND_A_B c1;

        int a() {
            return 0;
        }
    };

    class B {
    public:

        int b() {
            return 0;
        }
    };

    class D : public A {
    public:

        int d() {
            return 0;
        }
    };

    class E : public B {
    public:

        int e() {
            return 0;
        }
    };

    AND_A_B operator&&(A a1, B b1) {
        return a1.c1;
    }

    AND_B_A * operator&&(B a1, A b1) {
        return new AND_B_A();
    }

    int main() {
        A a1;
        B b1;
        D d1;
        E e1;
        (a1 && b1).cc(); // Unable to resolve identifier c
        (a1 && e1).cc(); // Unable to resolve identifier c
        (e1 && d1)->c1(); // Unable to resolve identifier c1

        return (0);
    }
}