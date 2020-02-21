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

namespace bug239739 {

    typedef struct AAA_239739 SimpleTypedef_239739, (*FunPtrTypedef_239739)();

    struct AAA_239739 {
        int foo();
    };

    int foo_239739() {
        SimpleTypedef_239739 a;
        FunPtrTypedef_239739 b;
        a.foo();
        b().foo();
    }                
    
}