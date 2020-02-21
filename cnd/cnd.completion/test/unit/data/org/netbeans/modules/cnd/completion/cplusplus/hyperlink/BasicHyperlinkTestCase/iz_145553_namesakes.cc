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

class Class {
public:
    struct Inner {
        void foo_inner_1();
    };
    struct Inner1 {
    };
    Class();
    Class(const Class& orig);
    virtual ~Class();
    void foo_1();
};

class Derived : public Class { // Class should hyperlink to ::Class
    Class other; // Class should hyperlink to ::Class
public:
    Derived() {
        other.foo_1(); // should be resolved
        Class::Inner inner; // Inner should hyperlink to ::Class::Inner
        inner.foo_inner_1(); // should be resolved
    }
    Derived(const Derived& orig);
    virtual ~Derived() {
    }
};

namespace AAA {
    class Class {
    public:
        struct Inner {
            void foo_inner_2();
        };
        struct Inner2 {
        };
        Class();
        Class(const Class& orig);
        virtual ~Class();
        void foo_2();
    };

    class Derived : public Class { // Class should hyperlink to AAA::Class
        Class other; // Class should hyperlink to AAA::Class
    public:
        Derived() {
            other.foo_2(); // should be resolved
            Class::Inner inner; // Inner should hyperlink to AAA::Class::Inner
            inner.foo_inner_2(); // should be resolved
        }
        Derived(const Derived& orig);
        virtual ~Derived() {
        }
    };
}
