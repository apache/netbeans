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


struct na {
};

template< bool C_ > struct bool_ {
    static const bool value = C_;
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
    typedef T1 if_c_type;
};

template<
typename T1
, typename T2
>
struct if_c < false, T1, T2> {
    typedef T2 if_c_type;
};


template<
typename T1d = na
        , typename T2d = na
        , typename T3d = na
        >
        struct if_ {
private:
    // agurt, 02/jan/03: two-step 'type' definition for the sake of aCC 
    typedef if_c<
            static_cast<bool> (T1d::value), T2d
            , T3d
            > almost_type_;

public:
    typedef typename almost_type_::if_c_type if_type;


};

template<
typename Cb = na
        , typename F1b = na
        , typename F2b = na
        >
        struct eval_if {
    typedef typename if_<Cb, F1b, F2b>::if_type f_;
    typedef typename f_::if_type type;

};

// (almost) copy & paste in order to save one more 
// recursively nested template instantiation to user

template<
bool C
, typename F1
, typename F2
>
struct eval_if_c {
    typedef typename if_c<C, F1, F2>::if_c_type f_;
};

template<
typename T = na
        >
        struct identity {
    typedef T type;

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

template< typename T, typename U > struct is_same : public false_ {
    int f;
};

template< typename T > struct is_same < T, T > : public true_ {
    int t;
};

struct protected_ {
};

struct public_ {
};


template<typename Ta, typename Access1>
struct access_control_base {
    typedef int bad_access_specifier;
    typedef is_same<
            Access1, protected_
            > x;
    typedef is_same<
            Access1, public_
            > y;
  
    typedef
    if_<y, Ta, bad_access_specifier> z;
    
    typedef typename z::if_type type;
};

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

int main() {
    access_control_base<chain_client, public_>::type out4;
    out4.push();
        
    return 0;
}
    
}