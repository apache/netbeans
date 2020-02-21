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

namespace bug243600 {
    enum class myEnum_243600 : int {
        A243600, B243600, C243600
    };

    struct foo_243600 {
        int field_243600;
    };

    int boo_243600()
    {
        foo_243600 arr[] = { 100, 200, 400 };

        int a = arr[ 0 ].field_243600; // OK
        int b = arr[ (int) 0  ].field_243600; // OK
        int c = arr[ static_cast<int>(myEnum_243600::A243600) ].field_243600; //  field highlighted as unable to resolve 
        int d = arr[ static_cast<long>(0) ].field_243600;        //  field highlighted as unable to resolve

        return 0;
    }   
}