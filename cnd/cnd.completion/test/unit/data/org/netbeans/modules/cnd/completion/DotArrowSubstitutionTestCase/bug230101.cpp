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
        void Test() {}
    };

    template <class T> 
    struct my_vector {
        typedef T* iterator;
    };

    template <class T>
    struct my_foreach_iterator {
        typedef typename T::iterator type;
    };

    template <class Iterator>
    struct my_iterator_reference
    {
        typedef Iterator type;
    };

    template<typename T>
    struct my_foreach_reference : my_iterator_reference<typename my_foreach_iterator<T>::type>
    {
    };

    template <class T>
    typename my_foreach_reference<T>::type myDeref() {

    }

    int main()
    {
        // here dot before Test() should be replaced with arrow
        (*myDeref<my_vector<MyClass*> >());

        return 0;
    }

}
