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

#include "file.h"

namespace S1 {
    int var1;

    void foo() {
        // S1 content must be visible with and without S1:: prefix
        S1::foo();
        S1::var1 = 10;
        foo();
        var1 = 11;
        // S2 content must be visible with prefixes
        S1::S2::boo();
        S1::S2::var2 = 100;
        S2::boo();
        S2::var2 = 101;
    }

    namespace S2 {
        int var2;
        
        void boo() {
            // S1 content must be visible with and without S1:: prefix
            S1::foo();
            S1::var1 = 12;
            foo();
            var1 = 13;
            // S2 content must be visible with and without prefixes
            S1::S2::boo();
            S1::S2::var2 = 102;
            S2::boo();
            S2::var2 = 103;
            boo();
            var2 = 104;
        }
        
        void funS2() {
            clsS1 s1;
            s1.clsS1pubFun();            
            
            clsS2 s2;
            s2.clsS2pubFun();
        }
        
        void clsS2::clsS2pubFun() {
            
        }
    }
    
    void funS1() {
        clsS1 s1;
        s1.clsS1pubFun();
        
        S2::clsS2 s2;
        s2.clsS2pubFun();
    }
    
    void clsS1::clsS1pubFun() {
        
    }
    
    extern int myCout;
}

