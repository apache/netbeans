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

namespace bug238688 {
    struct AAA238688 {
        int foo();
    };

    struct BBB238688 {
        int boo();
    };

    AAA238688 operator+(const AAA238688 &a, const BBB238688 &b) {
        return a;
    }

    BBB238688 operator+(const BBB238688 &b, const AAA238688 &a) {
        return b;
    }

    int function238688() {
        AAA238688 a;
        BBB238688 b;
        auto var1 = a + b;
        var1.foo();
        auto var2 = b + a;
        var2.boo();
    } 
}