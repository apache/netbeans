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

namespace {
template <typename _A> 
struct AAA {
    
    template <typename _B>
    struct BBB {
        
        template <typename _C>
        struct CCC {
            
            struct DDD {
                DDD(int a);
            };
        };
        
        template <typename _G>
        int deref(const _G &value);  
    };    
};

template <typename _A> template <typename _B> template <typename _X>
AAA<_A>::BBB<_B>::CCC<_X>::DDD::DDD(int a) {
    
}

template <typename _A> template <typename _B> template <typename _G>
int AAA<_A>::BBB<_B>::deref(const _G &value) {
    
}
}