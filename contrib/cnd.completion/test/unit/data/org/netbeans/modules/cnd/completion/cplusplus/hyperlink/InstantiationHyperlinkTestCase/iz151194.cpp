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

class BaseClass {
public:
    void DoThat(void) {}
};

template
<
typename T,
template <class> class StoragePolicy
>
class SmartPtr
: public StoragePolicy<T> {
public:
    typedef StoragePolicy<T> SP;
    typedef typename SP::PointerType PointerType;
    PointerType operator->() {
        return 0;
    }
};

template <class T>
class DefaultSPStorage {
public:
    typedef T* PointerType;
};

typedef SmartPtr< BaseClass, DefaultSPStorage >
NonConstBase_RefLink_NoConvert_Assert_DontPropagate_ptr;

int iz151194_main() {
    NonConstBase_RefLink_NoConvert_Assert_DontPropagate_ptr p1;
    p1->DoThat(); // unresolved
    return 0;
}