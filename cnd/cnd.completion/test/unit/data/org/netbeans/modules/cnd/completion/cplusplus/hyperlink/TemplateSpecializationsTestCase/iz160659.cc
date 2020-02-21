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

class iz160659_NullType {};

template <class TList> struct iz160659_Length;

template <> struct iz160659_Length<iz160659_NullType>
{
    enum { value = 0 };
};

int iz160659_foo() {
     iz160659_Length<iz160659_NullType>::value;
}

namespace iz160659_2{
    class iz160659_2_NullType {};

    template <class TList> struct iz160659_2_Length;

    template <> struct iz160659_2_Length<iz160659_2_NullType>
    {
        enum { value = 0 };
    };

    int iz160659_2_foo() {
         iz160659_2_Length<iz160659_2_NullType>::value;
    }
}