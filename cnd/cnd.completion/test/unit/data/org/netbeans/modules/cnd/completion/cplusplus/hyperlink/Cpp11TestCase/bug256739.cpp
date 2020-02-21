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

namespace bug256739 {
    struct AAA256739 {
        struct Iterator256739 {
            AAA256739& operator*();
            Iterator256739& operator++();
            bool operator!=(const Iterator256739 &other);
        };

        Iterator256739 begin() const;

        Iterator256739 end() const;

        void foo256739() const {
            for (const auto &var : *this) {
                var.foo256739(); // foo is unresolved
            }
        }
    };
}