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

namespace bug243171 {
    struct MyStruct_243171 {
        int foo();
    };

    template <typename T>
    struct AAA1_243171 {
        T operator->();
    };

    template <typename T>
    using Alias1_243171 = AAA1_243171<T>;

    namespace NNN {
        template <typename T>
        struct AAA2_243171 {
            T operator->();
        };

        template <typename T>
        using Alias2_243171 = AAA2_243171<T>;    
    }

    int test_243171() {
        auto var1 = AAA1_243171<MyStruct_243171>();
        var1->foo();

        auto var2 = Alias1_243171<MyStruct_243171>();
        var2->foo();

        auto var3 = NNN::AAA2_243171<MyStruct_243171>();
        var3->foo();

        auto var4 = NNN::Alias2_243171<MyStruct_243171>();
        var4->foo();    
        
        auto var5 = new MyStruct_243171();
        var5->foo();
    }
}