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

typedef unsigned int size_t; /* (historical version) */
typedef int ptrdiff_t; /* (historical version) */

namespace bug218848_std {
    template<typename _Tp>
    class allocator;

    template<>
    class allocator<void> {
    public:
        typedef size_t size_type;
        typedef ptrdiff_t difference_type;
        typedef void* pointer;
        typedef const void* const_pointer;
        typedef void value_type;

        template<typename _Tp1>
        struct rebind {
            typedef allocator<_Tp1> other;
        };
    };

    template<typename _Tp>
    class allocator {
    public:
        typedef size_t size_type;
        typedef ptrdiff_t difference_type;
        typedef _Tp* pointer;
        typedef const _Tp* const_pointer;
        typedef _Tp& reference;
        typedef const _Tp& const_reference;
        typedef _Tp value_type;

        template<typename _Tp1>
        struct rebind {
            typedef allocator<_Tp1> other;
        };
    };
} // namespace std

namespace bug218848_std {
    /// pair holds two objects of arbitrary type.

    template<class _T1, class _T2>
    struct pair {
        typedef _T1 first_type; ///<  @c first_type is the first bound type
        typedef _T2 second_type; ///<  @c second_type is the second bound type

        _T1 first; ///< @c first is a copy of the first object
        _T2 second; ///< @c second is a copy of the second object

        pair(const _T1& __a, const _T2 & __b)
        : first(__a), second(__b) {
        }
    };
} // namespace std

namespace bug218848_std {

    template<typename _Tp, typename _Distance = ptrdiff_t,
            typename _Pointer = _Tp*, typename _Reference = _Tp&>
            struct iterator {
        /// The type "pointed to" by the iterator.
        typedef _Tp value_type;
        /// Distance between iterators is represented as this type.
        typedef _Distance difference_type;
        /// This type represents a pointer-to-value_type.
        typedef _Pointer pointer;
        /// This type represents a reference-to-value_type.
        typedef _Reference reference;
    };

    template<typename _Tp>
    struct iterator_traits {
        typedef typename _Tp::value_type value_type;
        typedef typename _Tp::difference_type difference_type;
        typedef typename _Tp::pointer pointer;
        typedef typename _Tp::reference reference;
    };

    template<typename _Tp>
    struct iterator_traits<_Tp*> {
        typedef _Tp value_type;
        typedef ptrdiff_t difference_type;
        typedef _Tp* pointer;
        typedef _Tp& reference;
    };

    template<typename _Tp>
    struct iterator_traits<const _Tp*> {
        typedef _Tp value_type;
        typedef ptrdiff_t difference_type;
        typedef const _Tp* pointer;
        typedef const _Tp& reference;
    };
} // namespace std



namespace bug218848_std__gnu_cxx {
    // This iterator adapter is 'normal' in the sense that it does not
    // change the semantics of any of the operators of its iterator
    // parameter.  Its primary purpose is to convert an iterator that is
    // not a class, e.g. a pointer, into an iterator that is a class.
    // The _Container parameter exists solely so that different containers
    // using this template can instantiate different types, even if the
    // _Iterator parameter is the same.
    using bug218848_std::iterator_traits;
    using bug218848_std::iterator;

    template<typename _Iterator, typename _Container>
    class __normal_iterator {
    protected:
        _Iterator _M_current;

    public:
        typedef typename iterator_traits<_Iterator>::value_type value_type;
        typedef typename iterator_traits<_Iterator>::difference_type
        difference_type;
        typedef typename iterator_traits<_Iterator>::reference reference;
        typedef typename iterator_traits<_Iterator>::pointer pointer;


        // Forward iterator requirements

        reference
        operator*() const {
            return *_M_current;
        }

        pointer
        operator->() const {
            return _M_current;
        }
    };
} // namespace __gnu_cxx


namespace bug218848_std {

    template<typename _Tp, typename _Alloc>
    struct _Vector_base {

        struct _Vector_impl
                : public _Alloc {
            _Tp* _M_start;
            _Tp* _M_finish;
            _Tp* _M_end_of_storage;

            _Vector_impl(_Alloc const& __a)
            : _Alloc(__a), _M_start(0), _M_finish(0), _M_end_of_storage(0) {
            }
        };

    public:
        typedef _Alloc allocator_type;
    };

    template<typename _Tp, typename _Alloc = allocator<_Tp> >
            class vector : protected _Vector_base<_Tp, _Alloc> {
        // Concept requirements.

        typedef _Vector_base<_Tp, _Alloc> _Base;
        typedef vector<_Tp, _Alloc> vector_type;

    public:
        typedef _Tp value_type;
        typedef typename _Alloc::pointer pointer;
        typedef typename _Alloc::const_pointer const_pointer;
        typedef typename _Alloc::reference reference;
        typedef typename _Alloc::const_reference const_reference;
        typedef bug218848_std__gnu_cxx::__normal_iterator<pointer, vector_type> iterator;
        typedef bug218848_std__gnu_cxx::__normal_iterator<const_pointer, vector_type>
        const_iterator;
        typedef size_t size_type;
        typedef ptrdiff_t difference_type;
        typedef typename _Base::allocator_type allocator_type;

    public:

        iterator
        begin() {
            iterator i;
            return i;
        }

        reference
        operator[](size_type __n) {
            return *(begin() + __n);
        }

    public:

        void
        push_back(const value_type& __x) {
        }
    };

} // namespace std

namespace bug218848 {

    using namespace bug218848_std;

    template
    <
    class K,
    class V,
    class A = allocator< pair<K, V> >
    >
    struct Z
    : private vector< pair<K, V>, A > {
        typedef vector<pair<K, V>, A> Base;
        typedef K key_type;
        typedef V mapped_type;
        typedef typename Base::iterator iterator;
    };

    template
    <
    class T,
    class A = allocator< T >
    >
    struct W
    : private vector< T, A > {
        typedef allocator<T> All;
        typedef vector<T, All> Base;
        typedef typename Base::iterator iterator;
    };

    template
    <
    class K,
    class V,
    class A = allocator< pair<K, V> >
    >
    struct Y
    : private vector< pair<K, V>, A > {
        typedef allocator< pair<K, V> > All;
        typedef vector<pair<K, V>, All> Base;
        typedef K key_type;
        typedef V mapped_type;
        typedef typename Base::iterator iterator;
    };

    class Foo {
    public:
        void bar() {
        }
    };
    int main() {
        vector<Foo*> v;
        Foo *a[10];

        for (auto p : a) {
          p->bar();
        }

        for (auto p : v) {
          p->bar();
        }

        return 0;
    }
}
