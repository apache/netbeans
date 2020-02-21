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

namespace bug246803 {
    template <typename = struct DDD246803>
    struct AAA246803 {
        int foo();
    };

    template <>
    struct AAA246803<DDD246803> {
        int boo();
    };

    void mainAAA246803() {
        AAA246803<> var;
        var.boo();
    }

    template <bool, typename = int>
    struct BBB246803 {
        int foo();
    };

    template <typename T>
    struct BBB246803<false, T> {
        int boo();
    };

    template <>
    struct BBB246803<true, int> {
        int roo();
    };

    template <>
    struct BBB246803<false, int> {
        int doo();
    };

    void mainBBB246803() {
        BBB246803<true> var1;
        var1.roo();
        BBB246803<false, int> var2;
        var2.doo();
        BBB246803<false, double> var3;
        var3.boo();
    }

    template <typename...>
    struct CCC246803 {
        int foo();
    };

    template <typename T>
    struct CCC246803<T> {
        int boo();
    };

    void mainCCC246803() {
        CCC246803<> var1;
        var1.foo();
        CCC246803<int> var2;
        var2.boo();
    }
}