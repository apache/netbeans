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

namespace bug248624 {
    namespace std248624 {
      /// integral_constant
      template<typename _Tp, _Tp __v>
      struct integral_constant248624 {
        static constexpr _Tp value = __v;
      };    

      /// is_class
      template<typename _Tp>
      struct is_class248624 : public integral_constant248624<bool, __is_class(_Tp)> {};

      /// is_union
      template<typename _Tp>
      struct is_union248624 : public integral_constant248624<bool, __is_union(_Tp)> {};

      /// is_enum
      template<typename _Tp>
      struct is_enum248624 : public integral_constant248624<bool, __is_enum(_Tp)> {};
    }

    struct AAA248624 {
        int foo();
    };

    enum class BBB248624 {
        Value
    };

    union CCC248624 {
        int f1;
        float f2;
    };

    template <bool>
    struct Differentiator248624 {
        typedef int _false;
    };

    template <>
    struct Differentiator248624<true> {
        typedef int _true;
    };

    int main(int argc, char** argv) {
        Differentiator248624<std248624::is_class248624<AAA248624>::value>::_true var1;
        Differentiator248624<std248624::is_enum248624<BBB248624>::value>::_true var2;
        Differentiator248624<std248624::is_union248624<CCC248624>::value>::_true var3;
        return 0;
    }
}