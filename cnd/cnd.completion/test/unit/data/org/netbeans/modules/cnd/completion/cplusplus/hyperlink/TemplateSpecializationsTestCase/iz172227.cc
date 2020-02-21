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

template <class T> struct iz172227_A {
    int i;
};
template <> struct iz172227_A<int> {
    int j;
};

template <class T, class T2 = int> struct iz172227_B {
    iz172227_A<T2> a;
};

void foo() {
    iz172227_B<char, char> b;
    b.a.i;
    iz172227_B<char> b2;
    b2.a.j;
}
