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

struct bug191026_B {
    int j;
};

namespace bug191026_N1 {
    template <class T> struct bug191026_B {
        int i;
    };
}

namespace bug191026_N2 {
    
    using namespace bug191026_N1;
    
    struct bug191026_A : public bug191026_B<int> {
    };

    void bug191026_foo(){
        bug191026_A a;
        a.i;
    }
}