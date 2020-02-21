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

namespace bug243083_1 {
    template <typename T>
    struct Recursion243083_1 {
        typedef Recursion243083_1<typename T::next> next;
        typedef T type;
    };
    
    struct Inner2_243083_1 {
        int foo2();
        typedef void next;
    };
    
    struct Inner1_243083_1 {
        int foo1();
        typedef Inner2_243083_1 next;
    };
    
    struct Inner0_243083_1 {
        int foo0();
        typedef Inner1_243083_1 next;
    };    
    
    void recurse243083_1() {
        Recursion243083_1<Inner0_243083_1>::next::next::type var2;
        var2.foo2();
        Recursion243083_1<Inner0_243083_1>::next::type var1;
        var1.foo1();        
        Recursion243083_1<Inner0_243083_1>::type var0;
        var0.foo0();            
    } 
}