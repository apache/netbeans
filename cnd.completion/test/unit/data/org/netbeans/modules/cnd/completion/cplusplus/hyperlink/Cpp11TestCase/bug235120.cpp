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

namespace bug235120 {
    struct AAA_235120 {
        int foo();
    };

    struct BBB_235120 {
        int boo();
    };

    AAA_235120 operator+(AAA_235120 a, BBB_235120 b);

    BBB_235120 operator+(BBB_235120 a, AAA_235120 b);

    template <typename T1, typename T2>
    struct DDD_235120 {
        T1 a;
        T2 b;

        decltype(a + b) doo();
        decltype(b + a) goo();

        auto hoo() -> decltype(a + b);
        auto joo() -> decltype(b + a);
    };

    template <typename T1, typename T2>
    auto roo_235120(T1 a, T2 b) -> decltype(a + b);

    int zoo_235120() {
        DDD_235120<AAA_235120, BBB_235120> var;
        var.doo().foo();
        var.goo().boo();
        var.hoo().foo();
        var.joo().boo();

        AAA_235120 a;
        BBB_235120 b;
        roo_235120<AAA_235120, BBB_235120>(a, b).foo();
    }

    // ================= Unique ptr test case =================
    struct Foo_Content235120 {
        int abc;
    };
    
    struct Foo_235120
    {
        typedef Foo_Content235120* Pointer;
    };

    struct Bar_235120
    {
        int abc;
    };

    template <typename T>
    class SFINAE_235120 //Substitution Failure Is Not An Error.
    {
        template <typename U>
        static typename U::Pointer test(typename U::Pointer);

        template <typename U>
        static T* test(...);

    public:
        typedef decltype(test<T>(nullptr)) Pointer;
    };

    int main_235120(int argc, char ** argv) 
    {
        SFINAE_235120<Foo_235120>::Pointer foo = new Foo_Content235120();
        foo->abc = 11; //Unable to resolve identifier abc.
        SFINAE_235120<Bar_235120>::Pointer bar = new Bar_235120();
        bar->abc = 11; //Unable to resolve identifier abc.
        return 0;
    } 
    
    // === Typedef in decltype ===

    struct SimpleType_235120 {
        int foo();
    };

    template <typename T>
    struct TypesHolder_235120 {
        typedef T type;
        typedef decltype(*((type*)0)) declared_type;
    }; 

    int testTypesHolder_235120() {
        SimpleType_235120 var;
        TypesHolder_235120<SimpleType_235120>::declared_type &ref = var;
        ref.foo();
    }

    // === Iterators like in range based for statement ===

    struct Item_235120 {
        int foo();
    };

    template <typename T>
    struct Iterator_235120 {
        T& operator * ();
        T* operator -> ();

        T& get();
        void next(); //++
    };

    template <typename T>
    struct Collection_235120 {
        typedef Iterator_235120<T> iterator;
        iterator begin();
    };

    template <typename T>
    auto begin_235120(T param) -> decltype(param.begin());

    void testIterators_235120() {
        Collection_235120<Item_235120> a;
        begin_235120<Collection_235120<Item_235120>>(a)->foo();
        begin_235120(a)->foo();
    }
}