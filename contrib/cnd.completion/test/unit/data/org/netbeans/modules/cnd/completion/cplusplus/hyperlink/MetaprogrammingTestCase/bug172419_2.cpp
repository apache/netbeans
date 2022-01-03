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

namespace bug172419_2 {

struct any_tag {
};

struct output : virtual any_tag {
};

struct na {
    typedef na type;

    enum {
        value = 0
    };
};

struct integral_c_tag {
    static const int value = 0;
};

template< bool C_ > struct bool_ {
    static const bool value = C_;
    typedef integral_c_tag tag;
    typedef bool_ type;
    typedef bool value_type;

    operator bool() const {
        return this->value;
    }
};

template< bool C_ >
bool const bool_<C_>::value;

// shorcuts
typedef bool_ < true > true_;
typedef bool_ < false > false_;

struct void_ {
    typedef void_ type;
};

template<
bool C
, typename T1
, typename T2
>
struct if_c {
    typedef T1 type;
};

template<
typename T1
, typename T2
>
struct if_c < false, T1, T2> {
    typedef T2 type;
};

// agurt, 05/sep/04: nondescriptive parameter names for the sake of DigitalMars
// (and possibly MWCW < 8.0); see http://article.gmane.org/gmane.comp.lib.boost.devel/108959

template<
typename T1 = na
        , typename T2 = na
        , typename T3 = na
        >
        struct if_ {
private:
    // agurt, 02/jan/03: two-step 'type' definition for the sake of aCC 
    typedef if_c<
            static_cast<bool> (T1::value), T2
            , T3
            > almost_type_;

public:
    typedef typename almost_type_::type type;


};

template<
typename T1 = na
        , typename T2 = na
        , typename T3 = na
        >
        class if_2 {
public:
    typedef if_c<
            static_cast<bool> (T1::value), T2
            , T3
            > almost_type_;

    typedef typename almost_type_::type type;


};

template<
typename C = na
        , typename F1 = na
        , typename F2 = na
        >
        struct eval_if {
    typedef typename if_<C, F1, F2>::type f_;
    typedef typename f_::type type;

};

// (almost) copy & paste in order to save one more 
// recursively nested template instantiation to user

template<
bool C
, typename F1
, typename F2
>
struct eval_if_c {
    typedef typename if_c<C, F1, F2>::type f_;
    typedef typename f_::type type;
};

template<> struct eval_if < na, na, na > {

    template<typename T1, typename T2, typename T3, typename T4 = na, typename T5 = na > struct apply : eval_if <T1, T2, T3 > {
    };
};

template<
typename T = na
        >
        struct identity {
    typedef T type;

};

template<
typename T = na
        >
        struct make_identity {
    typedef identity<T> type;

};

template<> struct identity < na > {

    template<typename T1, typename T2 = na, typename T3 = na, typename T4 = na, typename T5 = na > struct apply : identity <T1 > {
    };
};

template<> struct make_identity < na > {

    template<typename T1, typename T2 = na, typename T3 = na, typename T4 = na, typename T5 = na > struct apply : make_identity <T1 > {
    };
};


typedef true_ else_;

template< typename Case1 = true_,
        typename Type1 = void_,
        typename Case2 = true_,
        typename Type2 = void_,
        typename Case3 = true_,
        typename Type3 = void_>
        struct select {
    typedef typename
    eval_if<
            Case1, identity<Type1>, eval_if<
            Case2, identity<Type2>, eval_if<
             if_<Case3, Type3, void_ > > > >::type type;
};

template< typename T, T N > struct integral_c;

template <class T, T val>
struct integral_constant : public integral_c<T, val> {
    typedef integral_constant<T, val> type;
};

template<> struct integral_constant<bool, true > : public true_ {
    typedef integral_constant<bool, true > type;
};

template<> struct integral_constant<bool, false > : public false_ {
    typedef integral_constant<bool, false > type;
};

typedef integral_constant<bool, true > true_type;
typedef integral_constant<bool, false > false_type;

template< typename T, typename U > struct is_same : integral_constant<bool, false > {
};

template< typename T > struct is_same < T, T > : integral_constant<bool, true > {
};

struct protected_ {
};

struct public_ {
};

template<typename U>
struct prot_ : protected U {

    prot_() {
    }

    template<typename V > prot_(V v) : U(v) {
    }
};

template<typename U>
struct pub_ : public U {

    pub_() {
    }

    template<typename V > pub_(V v) : U(v) {
    }
};

template<typename T, typename Access>
struct access_control_base {
    typedef int bad_access_specifier;
    typedef typename
    select< is_same<
            Access, protected_
            >, prot_<T>,
            is_same<
            Access, public_
            >, pub_<T>,
            else_, bad_access_specifier
            >::type type;
};

template< typename T, typename Access,
        typename Base = typename access_control_base<T, Access>::type >
        struct access_control : public Base {

    access_control() {
    }

    template<typename U> explicit access_control(U u) : Base(u) {
    }
};

namespace boost {
    namespace iostreams {
        namespace detail {


        }
    }
}

class chain_client {
public:
    void push() {
    }
};

class chain_client2 {
public:
    void pop() {
    }
};


template<typename Access>
class filtering_stream_base
: public access_control<
chain_client,
Access
> {
public:

    filtering_stream_base() {
    }
};

class filtering_stream :
public filtering_stream_base<public_ > {
public:

    filtering_stream() {
    }
};

int main() {
    filtering_stream out;
    out.push();
    
    filtering_stream_base<public_ > out2;
    out2.push();
    
    access_control<chain_client, public_> out3;
    out3.push();
    
    access_control_base<chain_client, public_>::type out4;
    out4.push();
    
    select< is_same<
            public_, protected_
            >, prot_<chain_client>,
            is_same<
            public_, public_
            >, pub_<chain_client>,
            else_, int
            >::type out5;
    out5.push();
        
    eval_if<
            is_same<
            public_, protected_
            >, identity<prot_<chain_client> >, eval_if<
            is_same<
            public_, public_
            >, identity<pub_<chain_client> >, eval_if<
            if_<else_, int, void_ > > > >::type out6;
    out6.push();

    if_<is_same<public_, protected_>, void_, chain_client >::type out7;
    out7.push();

    if_<false_, void_, chain_client >::type out8;
    out8.push();

    if_2<false_, void_, chain_client >::type out9;
    out9.push();
    
    
    if_c< static_cast<bool> (false_::value), void_
            , chain_client
            >::type outN10;
    outN10.push();        
    
    typedef if_c<
            static_cast<bool> (false_::value), void_
            , chain_client
            >::type type;
    type outN9;
    outN9.push();    
    
    typedef if_c<
            static_cast<bool> (false_::value), void_
            , chain_client
            > almost_type_;
    typedef typename almost_type_::type type;
    type outN8;
    outN8.push();
    
    if_c< false, void_, chain_client>::type outN7;
    outN7.push();
    
    if_c< true_::value, chain_client, void_>::type outN6;
    outN6.push();
    
    if_c< true_::value, chain_client, void_>::type outN5;
    outN5.push();

    if_<true_, chain_client, void_ >::type outN4;
    outN4.push();    
    
    if_<else_, chain_client, void_ >::type outN1;
    outN1.push();
    
    chain_client outN;
    outN.push();
            
    return 0;
}

}