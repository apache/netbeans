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

namespace bug262801 {
    template <bool val>
    struct AAA262801 {
        void roo();
    };

    template <typename T>
    struct container262801 {
        container262801(T param1, T param2);
        int size();
    };

    void fun262801() {
        auto var1 = container262801<container262801<const char*>>{{"one", "two"}, {"three", "four"}};
        var1.size();
        AAA262801< (1 > 5 && 1 > 6) >().roo(); // roo is unresolved
    }
}
