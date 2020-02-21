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

struct VectorElement {
    void foo();
};

template<typename _Tp> class allocator {
public:
    typedef _Tp& reference;
};

template<typename _Tp, typename _Alloc = allocator<_Tp> >
        class vector {
public:
    typedef typename _Alloc::reference reference;
    reference operator[] (int index);
};

void use_vector() {
    vector<VectorElement> v;
    v[2].foo();
}
