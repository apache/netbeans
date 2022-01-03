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

namespace bug250845 {
    int main250845() {
        const int value = 3;
        auto x = value;     // int          
        auto y = &value;    // const int *   
        const int* ptr = &value;
        auto z = ptr;       // const int *   
        auto *zz = ptr;         // const int *
        const auto *zzz = ptr;     // const int *

        const int arr[2] = {1, 2};
        for (auto elem : arr) { // int
            elem = 5;   
        }

        const int * arr2[2] = {&arr[1], &arr[2]};
        for (auto elem : arr2) { // const int *
            *elem;
        }

        int intVal1 = 0;
        int intVal2 = 1;
        int * arr3[2] = {&intVal1, &intVal1};
        for (auto &elem : arr3) { // int *&
            elem = &intVal2;
        }

        int arr4[2] = {intVal1, intVal2};
        for (const auto &elem : arr4) { // const int &
            elem;
        }
    }
}