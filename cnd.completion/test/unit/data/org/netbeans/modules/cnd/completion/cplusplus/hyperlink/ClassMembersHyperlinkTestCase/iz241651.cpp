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

namespace bug241651 {
    template <class T> using TemplateAlias241651 = T;

    struct Class1_241651 {
        virtual int foo();
    };

    struct Class2_241651 : Class1_241651 {
        virtual int foo();
    };

    typedef Class1_241651 XXX241651;

    int main241651() {
        int XXX241651 = 1;   
        int a = Class2_241651().foo();
        int b = Class2_241651().TemplateAlias241651<Class1_241651>::foo();
        int c = Class2_241651().XXX241651::foo();
        int d = Class2_241651().Class1_241651::foo();
        return a + b + c + d - XXX241651;
    }
}