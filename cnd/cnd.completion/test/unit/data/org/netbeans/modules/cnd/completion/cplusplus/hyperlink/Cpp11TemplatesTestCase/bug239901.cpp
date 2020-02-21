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

namespace bug239901 {
    struct A239901 {
        int foo();
    };

    struct B239901 {
        int boo();
    };

    template <class T = A239901>
    struct XXX239901 {
        T foo();
    };

    template<class T = A239901> using Type239901 = T;

    void function239901() {
        Type239901<> t;
        t.foo();

        Type239901<B239901> t1;
        t1.boo();    

        XXX239901<> a;
        a.foo().foo(); 
    }  
}
