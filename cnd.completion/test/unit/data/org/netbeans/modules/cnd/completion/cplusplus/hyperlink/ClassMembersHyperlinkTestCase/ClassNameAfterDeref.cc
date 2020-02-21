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
    class Bass {
    public:
        virtual ~Bass() {}
        virtual void method1() {  }
        int field1;
    protected:
        virtual void method11() {  }
        int field11;
    };

    class Derived : public Bass {
    public:
        Derived() {}
        virtual ~Derived() {}
        virtual void method1() {  }
        void method2();
    };

    void Derived::method2()
    {
        this->Bass::method11();
        (*this).Bass::field11;
        Bass::method1();
        Bass::method11();
        Bass* bbb;
        bbb->method11(); // not visible in this context!
    }

    int check() {
        Derived* dd;
        dd->Bass::method1();
        dd->method1();
        dd->Bass::field1;
        dd->Bass::field11; // not visible!
        dd->field1;
        dd->field11; // not visible!
        (*dd).Bass::method1();
        (*dd).Bass::method11(); // not visible!
    }

    int ptrCheck187254(Derived* ptr) {
        ptr->Derived::method1();
        ptr->Derived::~Derived();
    }
