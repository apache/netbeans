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

namespace bug247751 {
    struct AAA247751 {
        int aaa();
    };

    struct BBB247751 {
        int bbb();
    };

    struct CCC247751 {
        int ccc();
    };

    template <typename T1>
    struct Wrapper1_247751 {
        typedef T1& type;
    };

    template <typename T2>
    struct Wrapper2_247751 {
        typedef typename Wrapper1_247751<T2>::type type;
    };

    namespace std247751 {
        ///////////////////////////////////////
        // String    
        template <typename Char>
        struct basic_string247751 {
            typedef Char* iterator;
            iterator begin();
        };

        typedef basic_string247751<char> string247751;

        ///////////////////////////////////////
        // Tuple
        template <typename...Elements> 
        struct tuple247751 {};

        template< int I, class T >
        struct tuple_element247751;

        // recursive case
        template< int __i, class Head, class... Tail >
        struct tuple_element247751<__i, tuple247751<Head, Tail...>>
            : tuple_element247751<__i-1, tuple247751<Tail...>> { };

        // base case
        template< class Head, class... Tail >
        struct tuple_element247751<0, tuple247751<Head, Tail...>> {
           typedef Head type;
        };    

        template <int Ind, class... Elements>
        typename Wrapper1_247751<
            typename tuple_element247751<Ind, tuple247751<Elements...>>::type
        >::type
        get247751(tuple247751<Elements...> &tpl);

        template <typename...Elements>
        tuple247751<typename Wrapper2_247751<Elements>::type...> 
        make_tuple247751(Elements&&...args);

        ///////////////////////////////////////
        // Map  
        template <typename Key, typename Value> 
        struct map247751 {
            typedef Key key_type;
            typedef Value mapped_type;
            mapped_type& operator[](const key_type& key);
        };
    }

    int main247751() {
        auto var = std247751::make_tuple247751(AAA247751(), BBB247751(), CCC247751());
        auto elem0 = std247751::get247751<0>(var);
        auto elem1 = std247751::get247751<1>(var);
        auto elem2 = std247751::get247751<2>(var);
        std247751::map247751<int, int*> mapping;
        auto mapElem = mapping[3];
        std247751::string247751 str;
        auto stringVar = str;
        //auto stringIter = str.begin();
        return 0;
    }
}
