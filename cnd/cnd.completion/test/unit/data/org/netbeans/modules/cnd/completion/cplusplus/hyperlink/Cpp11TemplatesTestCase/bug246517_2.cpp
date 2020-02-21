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

namespace bug246517_2 {   
    struct true_type246517_2 {
        constexpr static bool value = true;
    };

    struct false_type246517_2 {
        constexpr static bool value = false;
    };

    template <bool Cond, typename T1, typename T2>
    struct conditional246517_2 {
        typedef T2 type;
    };

    template <typename T1, typename T2>
    struct conditional246517_2<true, T1, T2> {
        typedef T1 type;
    };

    template<typename...>
      struct __or_246517_2;

    template<>
      struct __or_246517_2<>
      : public false_type246517_2
      { };

    template<typename _B1>
      struct __or_246517_2<_B1>
      : public _B1
      { };

    template<typename _B1, typename _B2>
      struct __or_246517_2<_B1, _B2>
      : public conditional246517_2<_B1::value, _B1, _B2>::type
      { };

    template<typename _B1, typename _B2, typename _B3, typename... _Bn>
      struct __or_246517_2<_B1, _B2, _B3, _Bn...>
      : public conditional246517_2<_B1::value, _B1, __or_246517_2<_B2, _B3, _Bn...>>::type
      { };

    template<typename...>
    struct __and_246517_2;

    template<>
      struct __and_246517_2<>
      : public true_type246517_2
      { };

    template<typename _B1>
      struct __and_246517_2<_B1>
      : public _B1
      { };

    template<typename _B1, typename _B2>
      struct __and_246517_2<_B1, _B2>
      : public conditional246517_2<_B1::value, _B2, _B1>::type
      { };

    template<typename _B1, typename _B2, typename _B3, typename... _Bn>
      struct __and_246517_2<_B1, _B2, _B3, _Bn...>
      : public conditional246517_2<_B1::value, __and_246517_2<_B2, _B3, _Bn...>, _B1>::type
      { };

    template <bool>
    struct AAA246517_2 {
        int false_fun();
    };

    template <>
    struct AAA246517_2<true> {
        int true_fun();
    };

    int test_and246517_2() {
        AAA246517_2<__and_246517_2<>::value> var1;
        var1.true_fun();      
        AAA246517_2<__and_246517_2<true_type246517_2>::value> var2;
        var2.true_fun();
        AAA246517_2<__and_246517_2<false_type246517_2>::value> var3;
        var3.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2>::value> var4;
        var4.true_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, false_type246517_2>::value> var5;
        var5.false_fun();
        AAA246517_2<__and_246517_2<false_type246517_2, true_type246517_2>::value> var6;
        var6.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, false_type246517_2>::value> var7;
        var7.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, true_type246517_2>::value> var8;
        var8.true_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, true_type246517_2, false_type246517_2>::value> var9;
        var9.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, true_type246517_2, true_type246517_2>::value> var10;
        var10.true_fun();      
        return 0;
    }

    int test_or246517_2() {
        AAA246517_2<__or_246517_2<>::value> var1;
        var1.false_fun();      
        AAA246517_2<__or_246517_2<true_type246517_2>::value> var2;
        var2.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2>::value> var3;
        var3.false_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2>::value> var4;
        var4.false_fun();
        AAA246517_2<__or_246517_2<true_type246517_2, false_type246517_2>::value> var5;
        var5.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, true_type246517_2>::value> var6;
        var6.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, true_type246517_2>::value> var7;
        var7.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, false_type246517_2>::value> var8;
        var8.false_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, false_type246517_2, true_type246517_2>::value> var9;
        var9.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, false_type246517_2, false_type246517_2>::value> var10;
        var10.false_fun();      
        return 0;
    }

    int test_and_or246517_2() {
        AAA246517_2<
          __and_246517_2<
              __and_246517_2<true_type246517_2, true_type246517_2>, 
              __or_246517_2<false_type246517_2, false_type246517_2>
            >::value
        > false_var1;
        false_var1.false_fun();

        AAA246517_2<
          __or_246517_2<
                false_type246517_2, 
                false_type246517_2,
                __and_246517_2<
                  true_type246517_2, 
                  true_type246517_2, 
                  true_type246517_2, 
                  __or_246517_2<false_type246517_2, false_type246517_2>
                > 
          >::value
        > false_var2;  
        false_var2.false_fun();

        AAA246517_2<
          __and_246517_2<
                true_type246517_2, 
                __and_246517_2<
                  true_type246517_2, 
                  true_type246517_2, 
                  true_type246517_2, 
                  __or_246517_2<false_type246517_2, true_type246517_2>
                >,
                __and_246517_2< 
                  __and_246517_2<true_type246517_2, true_type246517_2>,
                  __or_246517_2<false_type246517_2, false_type246517_2, true_type246517_2>
                >
          >::value
        > true_var; 
        true_var.true_fun();      

        return 0;
    } 
}