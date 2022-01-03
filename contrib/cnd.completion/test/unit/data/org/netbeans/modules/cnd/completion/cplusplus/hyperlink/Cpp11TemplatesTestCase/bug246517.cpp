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

namespace bug246517 {    
    template <typename T> 
    struct add_reference246517 {
        typedef T& type;
    };

    template <typename T> 
    struct add_reference246517<T&> {
        typedef T& type;
    };
    
    template <typename T>
    struct remove_reference246517 {
        typedef T type;
    };

    template <typename T>
    struct remove_reference246517<T&> {
        typedef T type;
    };
    
    template <typename T>
    struct remove_const246517 {
        typedef T type;
    };

    template <typename T>
    struct remove_const246517<const T> {
        typedef T type;
    };    
    
    template <typename T>
    struct decay246517 {
        typedef typename remove_reference246517<T>::type no_ref;
        typedef typename remove_const246517<no_ref>::type type;
    };
    
    template <typename _Tp>
    using decay246517_t = typename decay246517<_Tp>::type;
    
    template <typename...Elements>
    struct tuple246517 {};

    template< int I, class T >
    struct tuple_element246517;

    // recursive case
    template< int __i, class Head, class... Tail >
    struct tuple_element246517<__i, tuple246517<Head, Tail...>>
        : tuple_element246517<__i-1, tuple246517<Tail...>> { };

    // base case
    template< class Head, class... Tail >
    struct tuple_element246517<0, tuple246517<Head, Tail...>> {
       typedef Head type;
    };

    template <int Ind, class... Elements>
    typename add_reference246517<
        typename tuple_element246517<Ind, tuple246517<Elements...>>::type
    >::type 
    get(tuple246517<Elements...> &tpl);
    
    template<typename... _Elements>
    constexpr tuple246517<_Elements...>
      simple_make_tuple246517(_Elements&&... __args);
    
    template<typename... _Elements>
    constexpr tuple246517<typename decay246517<_Elements>::type...>
      complex_make_tuple246517(_Elements&&... __args);    
    
    template<typename... _Elements>
    constexpr tuple246517<decay246517_t<_Elements>...>
      very_complex_make_tuple246517(_Elements&&... __args);    

    struct AAA246517 {
        int aaa();
    }; 
    struct BBB246517 {
        int bbb();
    };
    struct CCC246517 {
        int ccc();
    };

    int foo246517() {
        tuple246517<AAA246517, BBB246517, CCC246517> tpl;
        get<0>(tpl).aaa();
        get<1>(tpl).bbb();
        get<2>(tpl).ccc();
        auto tpl2 = simple_make_tuple246517(AAA246517(), BBB246517(), CCC246517());
        get<0>(tpl2).aaa();
        get<1>(tpl2).bbb();
        get<2>(tpl2).ccc();
        auto tpl3 = complex_make_tuple246517(AAA246517(), BBB246517(), CCC246517());
        get<0>(tpl3).aaa();
        get<1>(tpl3).bbb();
        get<2>(tpl3).ccc();        
        auto tpl4 = very_complex_make_tuple246517(AAA246517(), BBB246517(), CCC246517());
        get<0>(tpl4).aaa();
        get<1>(tpl4).bbb();
        get<2>(tpl4).ccc();        
    } 
}