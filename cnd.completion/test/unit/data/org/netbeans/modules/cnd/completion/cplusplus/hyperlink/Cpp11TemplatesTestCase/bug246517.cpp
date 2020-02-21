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

namespace bug246517 {    
    template <typename T> 
    struct add_reference246517 {
        typedef T& type;
    };

    template <typename T> 
    struct add_reference246517<T&> {
        typedef T& type;
    };
    
    template <typename T>
    struct remove_reference246517 {
        typedef T type;
    };

    template <typename T>
    struct remove_reference246517<T&> {
        typedef T type;
    };
    
    template <typename T>
    struct remove_const246517 {
        typedef T type;
    };

    template <typename T>
    struct remove_const246517<const T> {
        typedef T type;
    };    
    
    template <typename T>
    struct decay246517 {
        typedef typename remove_reference246517<T>::type no_ref;
        typedef typename remove_const246517<no_ref>::type type;
    };
    
    template <typename _Tp>
    using decay246517_t = typename decay246517<_Tp>::type;
    
    template <typename...Elements>
    struct tuple246517 {};

    template< int I, class T >
    struct tuple_element246517;

    // recursive case
    template< int __i, class Head, class... Tail >
    struct tuple_element246517<__i, tuple246517<Head, Tail...>>
        : tuple_element246517<__i-1, tuple246517<Tail...>> { };

    // base case
    template< class Head, class... Tail >
    struct tuple_element246517<0, tuple246517<Head, Tail...>> {
       typedef Head type;
    };

    template <int Ind, class... Elements>
    typename add_reference246517<
        typename tuple_element246517<Ind, tuple246517<Elements...>>::type
    >::type 
    get(tuple246517<Elements...> &tpl);
    
    template<typename... _Elements>
    constexpr tuple246517<_Elements...>
      simple_make_tuple246517(_Elements&&... __args);
    
    template<typename... _Elements>
    constexpr tuple246517<typename decay246517<_Elements>::type...>
      complex_make_tuple246517(_Elements&&... __args);    
    
    template<typename... _Elements>
    constexpr tuple246517<decay246517_t<_Elements>...>
      very_complex_make_tuple246517(_Elements&&... __args);    

    struct AAA246517 {
        int aaa();
    }; 
    struct BBB246517 {
        int bbb();
    };
    struct CCC246517 {
        int ccc();
    };

    int foo246517() {
        tuple246517<AAA246517, BBB246517, CCC246517> tpl;
        get<0>(tpl).aaa();
        get<1>(tpl).bbb();
        get<2>(tpl).ccc();
        auto tpl2 = simple_make_tuple246517(AAA246517(), BBB246517(), CCC246517());
        get<0>(tpl2).aaa();
        get<1>(tpl2).bbb();
        get<2>(tpl2).ccc();
        auto tpl3 = complex_make_tuple246517(AAA246517(), BBB246517(), CCC246517());
        get<0>(tpl3).aaa();
        get<1>(tpl3).bbb();
        get<2>(tpl3).ccc();        
        auto tpl4 = very_complex_make_tuple246517(AAA246517(), BBB246517(), CCC246517());
        get<0>(tpl4).aaa();
        get<1>(tpl4).bbb();
        get<2>(tpl4).ccc();        
    } 
}