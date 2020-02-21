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

template <class T, class TT> class explicit_specializations_C {
public:
    T i;
    void foo();
};

template<class T, class TT> void explicit_specializations_C<T,TT>::foo() {
    i++;
}

template<class TT> class explicit_specializations_C<long,TT> {
    int i;
public:
    void foo();
};

template<class TT> void explicit_specializations_C<long,TT>::foo() {
    i++;
}

template<> void explicit_specializations_C<int,int>::foo();

template<> void explicit_specializations_C<int,int>::foo() {
    i++;
}

int explicit_specializations_main(int argc, char** argv) {

    explicit_specializations_C<int, int> c;
    c.foo();

    explicit_specializations_C<long, char> c2;
    c2.foo();

    explicit_specializations_C<char,bool> c3;
    c3.foo();

    return 0;
}