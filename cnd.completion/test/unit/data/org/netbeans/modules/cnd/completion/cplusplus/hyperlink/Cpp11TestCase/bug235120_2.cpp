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

namespace bug235120_2 {
    struct Foo_235120_2 {
        int aaa;
    };
    
    struct FooPtr_235120_2 {
        int bbb;
    };
    
    struct Boo_235120_2 {
        int ccc;
    };
    
    template<typename _Tp>
    struct remove_reference_235120_2
    { typedef _Tp   type; };

    template<typename _Tp>
    struct remove_reference_235120_2<_Tp&>
    { typedef _Tp   type; };    
    
    template <typename T>
    struct delete_default_235120_2 {};
    
    template <>
    struct delete_default_235120_2<Foo_235120_2> {
        typedef FooPtr_235120_2* pointer;
    };
    
    template <typename _Tp, typename _Dp = delete_default_235120_2<_Tp>>
    class UniquePointer_235120_2
    {
      class _Pointer
      {
        template<typename _Up>
        static typename _Up::pointer __test(typename _Up::pointer*);

        template<typename _Up>
        static _Tp* __test(...);

        typedef typename remove_reference_235120_2<_Dp>::type _Del;

      public:
        typedef decltype(__test<_Del>(0)) type;
      };
      
    public:      
      typedef typename _Pointer::type pointer;
    };       
    
    int test_235120_2() {
        UniquePointer_235120_2<Foo_235120_2>::pointer uptr1;
        uptr1->bbb;
        UniquePointer_235120_2<Boo_235120_2>::pointer uptr2;
        uptr2->ccc;
    }
}      