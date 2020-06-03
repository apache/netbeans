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

namespace bug240929 {

    namespace llvm {

        template <class T, T v>
        struct integral_constant {
          typedef T value_type;
          static const value_type value = v;
          typedef integral_constant<T,v> type;
          operator value_type() { return value; }
        };

        typedef integral_constant<bool, true> true_type;
        typedef integral_constant<bool, false> false_type;

        /// \brief Metafunction that determines whether the two given types are 
        /// equivalent.
        template<typename T, typename U> 
        struct is_same       : public false_type {};

        template<typename T>             
        struct is_same<T, T> : public true_type {};

        // enable_if_c - Enable/disable a template based on a metafunction
        template<bool Cond, typename T = void>
        struct enable_if_c {
        };

        template<typename T> 
        struct enable_if_c<true, T> {
            typedef T type;
        };

        // enable_if - Enable/disable a template based on a metafunction
        template<typename Cond, typename T = void>
        struct enable_if : public enable_if_c<Cond::value, T> { };

        /// \brief Metafunction to remove reference from a type.
        template <typename T> struct remove_reference { typedef T type; };
        template <typename T> struct remove_reference<T&> { typedef T type; };

        // remove_pointer - Metafunction to turn Foo* into Foo.  Defined in
        // C++0x [meta.trans.ptr].
        template <typename T> struct remove_pointer { typedef T type; };
        template <typename T> struct remove_pointer<T*> { typedef T type; };
        template <typename T> struct remove_pointer<T*const> { typedef T type; };
        template <typename T> struct remove_pointer<T*volatile> { typedef T type; };
        template <typename T> struct remove_pointer<T*const volatile> { typedef T type; };

        /// \brief Metafunction that determines whether the given type is a pointer
        /// type.
        template <typename T> struct is_pointer : false_type {};
        template <typename T> struct is_pointer<T*> : true_type {};
        template <typename T> struct is_pointer<T* const> : true_type {};
        template <typename T> struct is_pointer<T* volatile> : true_type {};
        template <typename T> struct is_pointer<T* const volatile> : true_type {};

        /// \brief Metafunction that determines wheather the given type is a reference.
        template <typename T> struct is_reference : false_type {};
        template <typename T> struct is_reference<T&> : true_type {};

        // If T is a pointer, just return it. If it is not, return T&.
        template<typename T, typename Enable = void>
        struct add_lvalue_reference_if_not_pointer { typedef T &type; };

        template<typename T>
        struct add_lvalue_reference_if_not_pointer<T,
                                             typename enable_if<is_pointer<T> >::type> {
          typedef T type;
        };

        // If T is a pointer to X, return a pointer to const X. If it is not, return
        // const T.
        template<typename T, typename Enable = void>
        struct add_const_past_pointer { typedef const T type; };

        template<typename T>
        struct add_const_past_pointer<T, typename enable_if<is_pointer<T> >::type> {
          typedef const typename remove_pointer<T>::type *type;
        };

        // Define a template that can be specialized by smart pointers to reflect the
        // fact that they are automatically dereferenced, and are not involved with the
        // template selection process...  the default implementation is a noop.
        //
        template<typename From> struct simplify_type {
          typedef       From SimpleType;        // The real type this represents...
        };

        template<typename From> struct simplify_type<const From> {
          typedef typename simplify_type<From>::SimpleType NonConstSimpleType;
          typedef typename add_const_past_pointer<NonConstSimpleType>::type
            SimpleType;
          typedef typename add_lvalue_reference_if_not_pointer<SimpleType>::type
            RetType;
        };

        ////===----------------------------------------------------------------------===//
        ////                          cast<x> Support Templates
        ////===----------------------------------------------------------------------===//
        //

        template<class To, class From> struct cast_retty;

        // Calculate what type the 'cast' function should return, based on a requested
        // type of To and a source type of From.
        template<class To, class From> struct cast_retty_impl {
          typedef To& ret_type;         // Normal case, return Ty&
        };
        template<class To, class From> struct cast_retty_impl<To, const From> {
          typedef const To &ret_type;   // Normal case, return Ty&
        };

        template<class To, class From> struct cast_retty_impl<To, From*> {
          typedef To* ret_type;         // Pointer arg case, return Ty*
        };

        template<class To, class From> struct cast_retty_impl<To, const From*> {
          typedef const To* ret_type;   // Constant pointer arg case, return const Ty*
        };

        template<class To, class From> struct cast_retty_impl<To, const From*const> {
          typedef const To* ret_type;   // Constant pointer arg case, return const Ty*
        };


        template<class To, class From, class SimpleFrom>
        struct cast_retty_wrap {
          // When the simplified type and the from type are not the same, use the type
          // simplifier to reduce the type, then reuse cast_retty_impl to get the
          // resultant type.
          typedef typename cast_retty<To, SimpleFrom>::ret_type ret_type;
        };

        template<class To, class FromTy>
        struct cast_retty_wrap<To, FromTy, FromTy> {
          // When the simplified type is equal to the from type, use it directly.
          typedef typename cast_retty_impl<To,FromTy>::ret_type ret_type;
        };

        template<class To, class From>
        struct cast_retty {
          typedef typename cast_retty_wrap<To, From,
                           typename simplify_type<From>::SimpleType>::ret_type ret_type;
        };

        template <class X> struct is_simple_type {
          static const bool value =
              is_same<X, typename simplify_type<X>::SimpleType>::value;
        };
        //
        // cast<X> - Return the argument parameter cast to the specified type.  This
        // casting operator asserts that the type is correct, so it does not return null
        // on failure.  It does not allow a null argument (use cast_or_null for that).
        // It is typically used like this:
        //
        //   cast<Instruction>(myVal)->getParent()
        //
        template <class X, class Y>
        typename enable_if_c<!is_simple_type<Y>::value,
                                    typename cast_retty<X, const Y>::ret_type>::type
        cast(const Y &Val);

        template <class X, class Y>
        typename cast_retty<X, Y>::ret_type cast(Y &Val);

        template <class X, class Y>
        typename cast_retty<X, Y *>::ret_type cast(Y *Val);

    } // end of namespace llvm


    struct AAA {
        int foo();
    };

    struct BBB : AAA {
        int boo() const { 
            return 1;
        }

        static bool classof(const AAA *a) {
            return true;
        }
    };

    int main() {
        BBB b;
        const AAA &a1 = b;    
        llvm::cast<BBB>(a1).boo(); 

        AAA &a2 = b;
        llvm::cast<BBB>(a2).boo(); 

        AAA *pa1 = &b;
        llvm::cast<BBB>(pa1)->boo();

        const AAA *pa2 = &b;
        llvm::cast<BBB>(pa2)->boo();    

        return 0; 
    }
    
}
