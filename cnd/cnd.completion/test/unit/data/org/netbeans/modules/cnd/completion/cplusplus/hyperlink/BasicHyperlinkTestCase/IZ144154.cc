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

namespace detail {

    template<typename T>
    struct member_char_type {
        typedef typename T::char_type type;
    };

    template<typename T>
    class unwrap_reference
    {
     public:
        typedef T type;
    };

    template<typename T>
    struct unwrapped_type
    : unwrap_reference<T>
    { };
}

template<typename T>
struct char_type_of
    : detail::member_char_type<
          typename detail::unwrapped_type<T>::type // type unresolved
      >
    { };

template<typename T>
struct next
{
    typedef typename T::next type;
};

template<
      typename Size
    , typename T
    , typename Next
    >
struct l_item
{
    typedef l_item type;
    typedef Size size;
    typedef T item;
    typedef Next next;
};

namespace boost { namespace mpl {

template<class T> struct push_front_impl {};

template<>
struct push_front_impl<int>
{
    template< typename List, typename T > struct apply
    {
        typedef l_item<
              typename next<typename List::size>::type // type unresolved
            , T
            , typename List::type
            > type;
    };
};

}}
