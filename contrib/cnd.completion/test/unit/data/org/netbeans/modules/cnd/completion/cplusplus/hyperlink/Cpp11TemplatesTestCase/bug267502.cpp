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

namespace bug267502 {
    struct AAA267502 {
        void foo();
    };

    template <typename T>
    struct BBB267502 {};

    template <typename T>
    struct BBB267502<BBB267502<T>> {
        typedef T template_type;
    };

    template <typename T>
    struct CCC267502 {
        typedef BBB267502<T> orig_type;
        typedef CCC267502<T>::orig_type nested_type;
    };

    typedef CCC267502<AAA267502>::nested_type type267502;

    void boo267502() {
        BBB267502<type267502>::template_type var;
        var.foo();
    } 
}