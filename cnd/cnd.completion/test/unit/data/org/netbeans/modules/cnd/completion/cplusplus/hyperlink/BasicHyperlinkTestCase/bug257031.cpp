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

namespace bug257031 {
    enum A257031 { A1 = 0, A2 };

    namespace ns257031 {
        int func();
        int cmp(int a, int b);
    }

    void foo257031() {
        ns257031::func() < A1 ? A1 : A2;
    }
    
    void boo257031() {
        int s1 = 0, s2 = 1;
        int s3 = ns257031::cmp(s1, s2) < 0 ? s1 : s2;
    }
}