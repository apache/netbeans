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

namespace bug246643 {
    namespace std246643 {
        template <typename T1, typename T2> 
        struct pair246643 {
            T1 first;
            T2 second;
        };
    }
    template <typename T, unsigned N> 
    class SmallVector246643 {
        T& operator[](int index);
    };

    void foo246643() const {

        class CallBack246643 {
        public:
            SmallVector246643<std246643::pair246643<int, int>, 10> V;
        };
        CallBack246643 cb;
        cb.V[1].second;
    }
}