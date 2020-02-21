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

namespace bug234973 {
    typedef int T1_234973;

    typedef T1_234973 T2_234973;

    typedef T2_234973 T3_234973;

    template <typename T>
    struct AAA_234973 {
    };

    template <>
    struct AAA_234973<int> {
        int y;
    };
    
    template <typename T>
    struct BBB_234973 {
    };

    template <>
    struct BBB_234973<T2_234973> {
        int y;
    };    

    int foo_234973() {    
        AAA_234973<T3_234973> a;
        a.y = 0; // y is unresolved
        
        BBB_234973<T3_234973> b;
        b.y = 0; // y is unresolved        
    }
}