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

namespace bug255475 {
    typedef const int* check_type255475;

    template <typename T>
    struct type_traits255475 {
        typedef T was_not_pointer;
    };

    template <typename T>
    struct type_traits255475<T*> {
        typedef T was_pointer;
    };

    void foo255475() {
        type_traits255475<
                type_traits255475<check_type255475>::was_pointer
        >::was_not_pointer var = 0;
    }
}