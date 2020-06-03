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

namespace bug246683 {
    template <class T0, class ...T1> 
    struct A246683 {
        int foo() {
            return 1;
        }
    };

    template <class T> 
    struct A246683<T> {
        int foo() {
            return 0;
        }
        int bar() {
            return 0;
        }
    };

    int main246683() {
        A246683<int,double>().foo();
        A246683<int>().foo()
        A246683<int>().bar();
        return 0;
    }
}