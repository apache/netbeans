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

namespace bug240446 {
    
    struct AAA240446 {
        int foo();
    };    
    
    struct BBB240446 {
        BBB240446 operator()();

        bool boo();
    };
    
    BBB240446 clazz;

    struct Class240446 {
        AAA240446 clazz();

        void foo() {
            BBB240446 clazz;
            clazz().boo(); // non_null is unresolved
        }
        
        void boo() {
            clazz().foo(); // non_null is unresolved
        }
    };
}