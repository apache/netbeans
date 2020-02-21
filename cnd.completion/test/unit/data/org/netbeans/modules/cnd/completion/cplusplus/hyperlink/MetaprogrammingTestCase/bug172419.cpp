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

namespace bug172419 {

template< bool C_ > struct bool_ {
    static const bool value = C_;
};

// shorcuts
typedef bool_ < true > true_;
typedef bool_ < false > false_;

template<
bool C
, typename T1
, typename T2
>
struct if_c {
    typedef T1 type;
};

template<
typename T1
, typename T2
>
struct if_c < false, T1, T2> {
    typedef T2 type;
};

template<
typename T1
        , typename T2
        , typename T3
        >
        struct if_ {
    typedef if_c<
            static_cast<bool> (T1::value), T2
            , T3
            > z;
    typedef typename z::type type;
};

struct A {
    void foo() {
    }
};

struct B {
};

int main() {
    
    typedef if_<false_, B, A > tt;
    tt::type a;
    a.foo();

    if_c<
            static_cast<bool> (false_::value), int
            , A
            > ::type a2;    
    a2.foo();
    
    return 0;
}

}
