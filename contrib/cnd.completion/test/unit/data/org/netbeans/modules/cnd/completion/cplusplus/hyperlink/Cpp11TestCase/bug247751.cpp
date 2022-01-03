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

namespace bug247751 {
    struct AAA247751 {
        int foo();
    };

    AAA247751 array[3];

    auto foo247751() -> AAA247751 {
        return AAA247751();
    }

    int main247751() {
        auto var1 = AAA247751();
        auto var2 = decltype(AAA247751())();
        for (auto ttt : array) {
            ttt.foo();
        }
    } 
}