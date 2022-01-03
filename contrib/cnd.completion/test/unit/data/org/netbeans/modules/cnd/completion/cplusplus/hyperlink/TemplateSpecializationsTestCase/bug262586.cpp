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

namespace bug262586 {

    namespace std262586 { 
        
      struct void_t {};
      
      template <typename _Iterator, typename = void_t>
      struct __iterator_traits262586 { 
      };
       
      template <typename _Iterator>
      struct __iterator_traits262586<_Iterator, void_t> { 
          typedef typename _Iterator::pointer pointer;
      };

      template<typename _Iterator>
      struct iterator_traits262586 : __iterator_traits262586<_Iterator> { 
      };
      
      template<typename _Tp>
      struct iterator_traits262586<_Tp*> { 
          typedef _Tp* pointer;
      };

      template<typename _Iterator>
      class __normal_iterator262586 
      {
        typedef std262586::iterator_traits262586<_Iterator> __traits_type;

      public:
        typedef typename __traits_type::pointer pointer;      
      };    

      template<typename _Iterator>
      class reverse_iterator262586
      {
        protected:         
          typedef std262586::iterator_traits262586<_Iterator> __traits_type;

        public:
          typedef typename __traits_type::pointer pointer;      
          pointer operator->() const;
      };
      
      template <typename BT>
      struct vector_base {
          typedef __normal_iterator262586<BT*> iterator;
      };

      template <typename Tp>
      struct vector262586 {
          typedef vector_base<Tp> _Base;
          typedef typename _Base::iterator iterator;
          typedef reverse_iterator262586<iterator> reverse_iter;
      };
    }

    struct AAA262586 { 
        int foo(); 
    }; 

    int main262586() {
        std262586::vector262586<AAA262586>::reverse_iter iter1;
        iter1->foo();
        return 0;     
    }           
    
    // Part 2 - bug with using declarations

    template <typename T>
    struct CollectionBase262586 {
        T get();
    };

    template <typename T>
    struct Collection262586 : protected CollectionBase262586<T> {
        using CollectionBase262586<T>::get;
    };

    int main262586_2() {
        Collection262586<AAA262586> col;
        col.get().foo();
        return 0;
    } 
}
