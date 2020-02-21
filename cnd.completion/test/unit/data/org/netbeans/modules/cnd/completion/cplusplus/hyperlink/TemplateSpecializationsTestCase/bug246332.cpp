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

namespace bug246332 {
    template <typename T>
    struct tpl246332 {};

    template <typename A, typename B, typename C>
    struct tpl246332<A(B, C)> {
        typedef int two_params;
    };

    template <typename A, typename B, typename C, typename D>
    struct tpl246332<A(B, C, D)> {
        typedef int three_params;
    };

    typedef int zzz246332;
    typedef int yyy246332;
    typedef int xxx246332; 

    typedef int Fun1_246332(int, xxx246332);
    typedef int Fun2_246332(int, xxx246332, float);
    typedef zzz246332 Fun3_246332(yyy246332, xxx246332, float);

    void foo246332() {
        // Unresolved nested types:
        tpl246332<int(int, xxx246332)>::two_params var1;
        tpl246332<int(int, xxx246332, float)>::three_params var2;
        tpl246332<zzz246332(yyy246332, xxx246332, float)>::three_params var3;
        tpl246332<Fun1_246332>::two_params var4;
        tpl246332<Fun2_246332>::three_params var5;
        tpl246332<Fun3_246332>::three_params var6;    
    }  
}