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

// IZ#154775: Unresolved inner type of instantiation

template <class T> struct B {
    typedef T bType;
};

struct A {
    typedef int aType;
    void foo();
};

int main() {
    B<A>::bType::aType i;
}

template <class T1, class T2, class T3> struct C {
    typedef T1 t1;
    typedef T3 t3;
};

struct S1 {
    typedef int i;
};

struct S3 {
    typedef int k;
};

int main2() {
    C<S1, S2, S3>::t1::i a;
    C<S1, S2, S3>::t3::k b;
}
