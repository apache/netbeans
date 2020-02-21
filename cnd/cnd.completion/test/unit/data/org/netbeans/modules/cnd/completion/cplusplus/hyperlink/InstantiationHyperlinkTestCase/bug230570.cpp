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

namespace {

    struct MyClass {
        void boo() {}
    };


    template <typename T>  
    struct my_allocator {

        typedef T* pointer;


        template <typename T1>
        struct rebind {
            typedef my_allocator<T1> other;
        };

    };


    template <typename T>
    struct type_traits {

    };

    template<typename _Tp>
    struct type_traits<_Tp*> {

        typedef MyClass& reference;

        typedef MyClass* pointer;

    };

    template <class Iterator>
    struct my_iterator {

        Iterator myIter;

        my_iterator(Iterator iter) : myIter(iter) {}

        typename type_traits<Iterator>::reference operator*() { 
            return *myIter; 
        }

        typename type_traits<Iterator>::reference get() {
            return *myIter;
        }

    };

    template <typename T, typename Allocator>
    struct vector_base {

        typedef typename Allocator::template rebind<T>::other T_Alloc_Types;

    };

    template <class T, class Allocator> 
    struct my_vector {

        typedef typename vector_base<T, Allocator>::T_Alloc_Types T_Alloc_Types;

        typedef typename T_Alloc_Types::pointer pointer;

        typedef my_iterator<pointer> iterator;

    }; 

    template <class T>
    typename my_vector<T, my_allocator<T> >::iterator dereference() {

    }

    int main()
    {
        dereference<MyClass>().get().boo(); // boo is unresolved
        return 0;
    }

}