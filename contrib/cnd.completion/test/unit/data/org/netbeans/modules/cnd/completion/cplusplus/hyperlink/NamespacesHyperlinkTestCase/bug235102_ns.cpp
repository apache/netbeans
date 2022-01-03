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

namespace bug235102_ns {
    namespace A235102_ns {
        struct AAA235102_ns {
            int foo();
        };    
    }

    namespace B235102_ns {
        using namespace A235102_ns;
    }

    namespace B235102_ns {
        namespace C235102_ns {
            AAA235102_ns x;

            int function235102_ns() {
                x.foo(); // foo is unresolved
            }
        } 
    }
    
    namespace UD_A_235102 {
        struct Test235102 {
            int foo();
        };
    }
    
    namespace UD_B_235102 {
        using UD_A_235102::Test235102;
    }
    
    namespace UD_B_235102 {
        namespace UD_C_235102 {
            Test235102 x;

            int ud_func_235102() {
                x.foo(); // foo is unresolved
            }
        }         
    }
}