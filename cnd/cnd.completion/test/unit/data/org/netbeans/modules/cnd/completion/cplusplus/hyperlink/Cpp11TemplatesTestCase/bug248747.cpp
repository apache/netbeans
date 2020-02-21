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

namespace bug248747 {
    #define DEFINE_HAS_CATEGORY \
      template<typename _Tp>  \
      class __has_iterator_category_helper248747 : __sfinae_types248747 { \
          template<typename _Up> struct _Wrap_type {}; \
          template<typename _Up> static __one __test(_Wrap_type<typename _Up:: iterator_category >*); \
          template<typename _Up> static __two __test(...); \
          public: static constexpr bool value = sizeof(__test<_Tp>(0)) == 1; \
      }; \
      template<typename _Tp> struct __has_iterator_category248747 : integral_constant248747<bool, __has_iterator_category_helper248747 <_Tp>::value> {}

    namespace std248747 {

      template <typename Tp, Tp val> 
      struct integral_constant248747 {
          static constexpr Tp value = val;
      };

      struct __sfinae_types248747
      {
        typedef char __one;
        typedef struct { char __arr[2]; } __two;
      };

      DEFINE_HAS_CATEGORY;

      template<typename _Iterator,
               bool = __has_iterator_category248747<_Iterator>::value>
      struct __iterator_traits248747 { };

      template<typename _Iterator>
      struct __iterator_traits248747<_Iterator, true>
      {
        typedef typename _Iterator::iterator_category iterator_category;
        typedef typename _Iterator::pointer           pointer;
      };

      template<typename _Iterator>
      struct iterator_traits248747 : public __iterator_traits248747<_Iterator> { };

      template<typename _Tp>
      struct iterator_traits248747<_Tp*>
      {
        typedef int iterator_category;
        typedef _Tp* pointer;
      };  

      template<typename _Iterator, typename _Container>
      class __normal_iterator248747 
      {
        typedef std248747::iterator_traits248747<_Iterator> __traits_type;

      public:
        typedef typename __traits_type::iterator_category iterator_category;
        typedef typename __traits_type::pointer   	pointer;      
      };    

      template<typename _Iterator>
      class reverse_iterator248747
      {
        protected:        
          typedef std248747::iterator_traits248747<_Iterator> __traits_type;

        public:
          typedef typename __traits_type::pointer pointer;      
          pointer operator->() const;
      };

      template <typename Tp>
      struct vector248747 {
          typedef Tp* pointer;
          typedef __normal_iterator248747<pointer, vector248747> iterator;
          typedef reverse_iterator248747<iterator> reverse_iter;
      };
    }

    struct AAA248747 {
        int foo();
    };

    int main248747() {
        std248747::vector248747<AAA248747>::reverse_iter iter1;
        iter1->foo();
        return 0;    
    }           
}