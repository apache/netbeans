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

namespace bug246332_1 {
    ////////////////////////////////////////////////////////////////////////////////
    // Expression parameter passing    
    
    template <bool val>
    struct MyStruct246332_1 {
        int boo(); 
    };  

    template <>
    struct MyStruct246332_1<true> {
        int foo();
    };

    template <bool val1, bool val2>
    struct AAA246332_1 {};

    template <bool val1>
    struct AAA246332_1<val1, true> {
        typedef MyStruct246332_1<val1> type;
    };

    int main246332_1() {
        AAA246332_1<true, true>::type var1;
        AAA246332_1<false, true>::type var2; 
        var1.foo();
        var2.boo(); 
        return 0;
    } 

    ////////////////////////////////////////////////////////////////////////////////
    // Template parameter deducing

    struct ZZZ246332_1 {
        int foo();
    };

    template <typename T>
    struct YYY246332_1 {};

    template <typename T>
    struct YYY246332_1<YYY246332_1<T> > {
        typedef T type;
    };

    typedef typename YYY246332_1<YYY246332_1<ZZZ246332_1> >::type alias246332_1;

    int boo246332_1() {
        YYY246332_1<YYY246332_1<ZZZ246332_1> >::type var1;
        alias246332_1 var2;
        var1.foo();
        var2.foo();
    }  
}