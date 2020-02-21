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

namespace bug242284 {
    int mainbug242284() {
        return []() -> int {
            struct InnerStruct1 {
                int foo() { return 0; };
            }; 
            int aa = InnerStruct1().foo();

            struct InnerStruct2 {
                int boo() { return 1; };
                InnerStruct2() {};
            }; 
            int bb = InnerStruct2().boo();

            return aa + bb;
        }();            
    }
}