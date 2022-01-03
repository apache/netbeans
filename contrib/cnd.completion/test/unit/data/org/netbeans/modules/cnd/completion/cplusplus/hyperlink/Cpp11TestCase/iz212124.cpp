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

namespace classMembersEnums212124
{
    struct S212124
    {
        enum { A212124 = 1, B212124 = 2 };
        struct T212124
        {
            enum { B212124 = 102 };

            enum class E1_212124;
            enum E2_212124 : int;
        };
    };

    enum class S212124::T212124::E1_212124 { A1_212124 = A212124, B1_212124 = B212124, C1_212124 };
    enum S212124::T212124::E2_212124 : int { A1_212124 = A212124, B1_212124 = B212124, C1_212124 };

    static_assert(int(S212124::T212124::E1_212124::A1_212124) == 1, "error");
    static_assert(int(S212124::T212124::E1_212124::B1_212124) == 102, "error");
    static_assert(int(S212124::T212124::E1_212124::C1_212124) == 103, "error");

    static_assert(int(S212124::T212124::E2_212124::A1_212124) == 1, "error");
    static_assert(int(S212124::T212124::E2_212124::B1_212124) == 102, "error");
    static_assert(int(S212124::T212124::E2_212124::C1_212124) == 103, "error");
    static_assert(int(S212124::T212124::A1_212124) == 1, "error");
    static_assert(int(S212124::T212124::B1_212124) == 102, "error");
    static_assert(int(S212124::T212124::C1_212124) == 103, "error");
}
