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

namespace bug246068 {
    
    struct AAA246068 {
        int foo();
    };

    struct BBB246068 {
        int boo();
    };    
    
    #define BOOST_STATIC_CONSTANT246068(type, assignment) static const type assignment

    template <class T>
    struct remove_rvalue_ref246068 {
       typedef T type;
    };

    template< typename T > 
    struct remove_reference246068 {
        public: typedef typename remove_rvalue_ref246068<T>::type type;
    };

    template< typename T > 
    struct remove_reference246068 < T& > {
        public: typedef T type;
    };

    template <typename T>
    struct add_pointer_impl246068 
    {
        typedef typename remove_reference246068<T>::type no_ref_type;
        typedef no_ref_type* type;
    };

    template< typename T >
    struct add_pointer246068 {
        typedef typename add_pointer_impl246068<T>::type type;
    };

    template<typename Function> 
    struct function_traits_helper246068 {};

    template<typename R>
    struct function_traits_helper246068<R (*)(void)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 0);
      typedef R result_type;
    };

    template<typename R, typename T1>
    struct function_traits_helper246068<R (*)(T1)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 1);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T1 argument_type;
    };

    template<typename R, typename T1, typename T2>
    struct function_traits_helper246068<R (*)(T1, T2)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 2);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T1 first_argument_type;
      typedef T2 second_argument_type;
    };

    template<typename R, typename T1, typename T2, typename T3>
    struct function_traits_helper246068<R (*)(T1, T2, T3)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 3);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
    };

    template<typename R, typename T1, typename T2, typename T3, typename T4>
    struct function_traits_helper246068<R (*)(T1, T2, T3, T4)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 4);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
      typedef T4 arg4_type;
    };

    template<typename R, typename T1, typename T2, typename T3, typename T4,
             typename T5>
    struct function_traits_helper246068<R (*)(T1, T2, T3, T4, T5)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 5);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
      typedef T4 arg4_type;
      typedef T5 arg5_type;
    };

    template<typename R, typename T1, typename T2, typename T3, typename T4,
             typename T5, typename T6>
    struct function_traits_helper246068<R (*)(T1, T2, T3, T4, T5, T6)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 6);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
      typedef T4 arg4_type;
      typedef T5 arg5_type;
      typedef T6 arg6_type;
    };

    template<typename R, typename T1, typename T2, typename T3, typename T4,
             typename T5, typename T6, typename T7>
    struct function_traits_helper246068<R (*)(T1, T2, T3, T4, T5, T6, T7)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 7);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
      typedef T4 arg4_type;
      typedef T5 arg5_type;
      typedef T6 arg6_type;
      typedef T7 arg7_type;
    };

    template<typename R, typename T1, typename T2, typename T3, typename T4,
             typename T5, typename T6, typename T7, typename T8>
    struct function_traits_helper246068<R (*)(T1, T2, T3, T4, T5, T6, T7, T8)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 8);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
      typedef T4 arg4_type;
      typedef T5 arg5_type;
      typedef T6 arg6_type;
      typedef T7 arg7_type;
      typedef T8 arg8_type;
    };

    template<typename R, typename T1, typename T2, typename T3, typename T4,
             typename T5, typename T6, typename T7, typename T8, typename T9>
    struct function_traits_helper246068<R (*)(T1, T2, T3, T4, T5, T6, T7, T8, T9)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 9);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
      typedef T4 arg4_type;
      typedef T5 arg5_type;
      typedef T6 arg6_type;
      typedef T7 arg7_type;
      typedef T8 arg8_type;
      typedef T9 arg9_type;
    };

    template<typename R, typename T1, typename T2, typename T3, typename T4,
             typename T5, typename T6, typename T7, typename T8, typename T9,
             typename T10>
    struct function_traits_helper246068<R (*)(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)>
    {
      BOOST_STATIC_CONSTANT246068(unsigned, arity = 10);
      typedef R result_type;
      typedef T1 arg1_type;
      typedef T2 arg2_type;
      typedef T3 arg3_type;
      typedef T4 arg4_type;
      typedef T5 arg5_type;
      typedef T6 arg6_type;
      typedef T7 arg7_type;
      typedef T8 arg8_type;
      typedef T9 arg9_type;
      typedef T10 arg10_type;
    };

    template<typename Function>
    struct function_traits246068 : public function_traits_helper246068<typename add_pointer246068<Function>::type> {};

    template<int Arity, typename Signature>
    class real_get_signal_impl246068 {};

    template<typename Signature>
    class real_get_signal_impl246068<0, Signature> {
    public:
        typedef typename AAA246068 exact_type;
        typedef typename function_traits246068<Signature>::result_type type;
    };  

    template<typename Signature>
    class real_get_signal_impl246068<1, Signature> {
    public:
        typedef typename BBB246068 exact_type;
        typedef typename function_traits246068<Signature>::result_type type;
    };  

    template<typename Signature>
    struct get_signal_impl246068 : public real_get_signal_impl246068<(function_traits246068<Signature>::arity), Signature> {};

    template<typename Signature>
    class signal246068_exact_type : public get_signal_impl246068<Signature>::exact_type {};
    
    template<typename Signature>
    class signal246068_type : public get_signal_impl246068<Signature>::type {};    

    int main246068() {
        signal246068_exact_type<AAA246068 ()> sig0;
        sig0.foo();
        signal246068_exact_type<BBB246068 (int)> sig1;
        sig1.boo();    
        signal246068_type<AAA246068 ()> sig3;
        sig3.foo();
        signal246068_type<BBB246068 (int)> sig4;
        sig4.boo();         
        return 0;
    }    
    
    #undef BOOST_STATIC_CONSTANT246068
} 