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

namespace bug238847_3 {
    
    //==========================================================================
    // Allocators

    template <typename T>  
    struct my_allocator_238847_3 {

        typedef T value_type;

        typedef T* pointer;

        int foo();

        template <typename T1>
        struct my_rebind {
            typedef my_allocator_238847_3<T1> other;
        };

    };
    
    template<typename _Alloc, typename _Tp>
    struct my_alloc_rebind_238847_3
    {
      typedef typename _Alloc::template my_rebind<_Tp>::other __type;
    };    

    template <typename _Alloc>
    struct my_allocator_traits_238847_3 {

        typedef typename _Alloc::value_type value_type;

    private: 
            
        template<typename _Tp> 
        static typename _Tp:: pointer _S_pointer_helper (_Tp*); 
        
        static value_type* _S_pointer_helper (...); 
        
        typedef decltype( _S_pointer_helper ((_Alloc*)0)) __pointer ;
        
    public:

        typedef __pointer pointer;


        template<typename _Tp>
        using rebind_alloc = typename my_alloc_rebind_238847_3<_Alloc, _Tp>::__type;
    };

    template<typename _Alloc>
     struct my_alloc_traits_238847_3 {
        typedef _Alloc allocator_type;
        typedef my_allocator_traits_238847_3<_Alloc>           _Base_type;

        typedef typename _Base_type::value_type         value_type;

        typedef typename _Base_type::pointer            pointer;

        template<typename _Tp>
        struct rebind_helper
        { typedef typename _Base_type::template rebind_alloc<_Tp> other; };    
    };    
    
    //==========================================================================
    // Iterators

    template <typename T>
    struct type_traits_238847_3 {

    };

    template<typename _Tp>
    struct type_traits_238847_3<_Tp*> {

        typedef _Tp& reference;

        typedef _Tp* pointer;

    };

    template <class Iterator>
    struct my_iterator_238847_3 {

        typename type_traits_238847_3<Iterator>::pointer get();

    };

    //==========================================================================
    // Vector
    
    template <typename T, typename Allocator>
    struct vector_base_238847_3 {

        typedef typename my_alloc_traits_238847_3<Allocator>::template rebind_helper<T>::other _Tp_alloc_type;

        typedef typename my_alloc_traits_238847_3<_Tp_alloc_type>::pointer pointer;    

    };    
    
    template <typename T, typename Alloc = my_allocator_238847_3<T> >
    struct vector_238847_3 {
        
        typedef vector_base_238847_3<T, Alloc> _Base;
        
        typedef typename _Base::pointer pointer;
        
        typedef my_iterator_238847_3<pointer> iterator;
        
    };

    struct MyClass_238847_3 {
        void boo() {}
    };

    int main_238847_3()
    {
        vector_238847_3<MyClass_238847_3>::pointer ptr;
        ptr->boo();

        vector_238847_3<MyClass_238847_3>::iterator iter;
        iter.get()->boo();
        
        return 0; 
    }  
} 