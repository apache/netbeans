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

namespace bug249613 {
    namespace A249613 {
        inline namespace __1 {
            namespace B249613 {
                void foo249613() {
                }
            }
            
            struct InlinedStruct249613 {};
        }

        namespace {
            namespace C249613 {
                void boo249613() {
                }
            }
            struct UnnamedStruct249613 {};
        }
    }

    int main249613() {
        A249613::B249613::foo249613();
        A249613::C249613::boo249613();
        A249613::InlinedStruct249613 st1;
        A249613::UnnamedStruct249613 st2;
        return 0;
    }
}