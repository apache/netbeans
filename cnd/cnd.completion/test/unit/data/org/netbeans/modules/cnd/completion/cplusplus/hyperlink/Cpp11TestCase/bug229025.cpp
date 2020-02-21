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

namespace bug229025 {
    struct A {};
    template<class T> struct foo { const static bool bar = false; typedef T type;};
    template<class T> struct foo<T&> { const static bool bar1 = true; };
    template<class T> struct foo<T&&> { const static bool bar2 = true; };

    void main() {
        foo<A>::bar;
        foo<A&>::bar1;
        foo<A&&>::bar2;
    }
}
