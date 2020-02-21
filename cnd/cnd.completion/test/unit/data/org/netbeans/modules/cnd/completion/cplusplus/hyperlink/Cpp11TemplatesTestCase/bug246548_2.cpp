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

namespace bug246548_2 {
    template <int Ind, typename...Elems>
    struct AAA246548_2;

    template <int Ind, typename Head, typename...Elems>
    struct AAA246548_2<Ind, Head, Elems...> : AAA246548_2<Ind + 1, Elems...> {
        typedef int inner;
    };

    template <int Ind>
    struct AAA246548_2<Ind> {
        typedef int stop;
    };

    int main246548_2() {
        AAA246548_2<0, int, double>::inner var1;
        AAA246548_2<0, int, double>::stop var2;
        return 0;
    } 
}