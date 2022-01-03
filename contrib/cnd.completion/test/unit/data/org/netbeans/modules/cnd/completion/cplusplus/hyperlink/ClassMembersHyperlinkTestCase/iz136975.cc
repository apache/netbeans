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

struct MyType {
    int Release(MyType * pointer);
    void Destroy();
    void OnDereference();

    MyType * operator->() {
        return this;
    }
};

class SmartPtr {
    typedef MyType SP;
    typedef MyType OP;
    typedef MyType KP;
    typedef MyType* PointerType;

    ~SmartPtr() {
        if (OP::Release(*static_cast<SP*> (this))) {
            SP::Destroy();
        }
    }

    PointerType operator->() {
        KP::OnDereference();
        return SP::operator->();
    }
};
