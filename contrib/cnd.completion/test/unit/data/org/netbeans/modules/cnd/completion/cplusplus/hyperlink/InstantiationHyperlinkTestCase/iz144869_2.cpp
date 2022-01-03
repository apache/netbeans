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

template <class T1, class T2> class C {
};

template <class T> class C<T, int>{
public:
    int i;
};

template <class T> class C<int, T>{
public:
    int j;
};

template <class T1, class T2> class C<T1, T2* >{
public:
    int k;
};

template <class X> class ZZ {
public:
    X foo();
};

template <class X> class ZZ<X*> {
public:
    void boo();
};


int main() {
    C<char, int> c;
    c.i; // unresolved

    C<int, char> c2;
    c2.j; // unresolved

    C<bool, bool*> c3;
    c3.k; // unresolved

    ZZ<int *> t;
    t.boo();

    return 0;
}