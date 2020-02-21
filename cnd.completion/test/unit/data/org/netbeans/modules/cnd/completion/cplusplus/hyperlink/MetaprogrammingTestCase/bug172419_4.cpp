/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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