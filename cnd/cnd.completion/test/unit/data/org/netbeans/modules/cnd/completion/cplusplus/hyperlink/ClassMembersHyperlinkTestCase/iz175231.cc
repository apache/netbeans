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

namespace iz175231_std1 {

    template<class charT, class traits>
    class basic_ios1 {
        inline operator void*() const;
        inline bool fail() const;
    };

    template<class charT, class traits>
    basic_ios1<charT, traits>::operator void*() const {
        return fail() ? (void*) 0 : (void*) 1; // <<<== fail is unresolved
    }

    template<class charT, class traits>
    inline bool
    basic_ios1<charT, traits>::fail() const {
        return true;
    }
}
