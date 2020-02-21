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

namespace bug172419_5 {
    struct AAA {
    };

    struct BBB {
        int bbb();
    };

    struct XXX {
    };

    struct YYY {
        int yyy();
    };

    struct void_ {};

    template <bool C, typename TT1, typename TT2>
    struct if_else {
        typedef TT2 type;
    };

    template <typename TT1, typename TT2>
    struct if_else<true, TT1, TT2> {
        typedef TT1 type;
    };

    template <typename X1, typename X2>
    struct is_same {
        static const bool value = false;
    };

    template <typename X>
    struct is_same<X, X> {
        static const bool value = true;
    };

    struct switch_plain {
        typedef typename 
        if_else< 
            is_same<XXX, AAA>::value, BBB, 
            typename if_else< is_same<XXX, XXX>::value, YYY, void_>::type 
        >::type type;
    };

    template <typename Selector, typename Case1, typename Value1, typename Case2, typename Value2>
    struct switch_complex {
        typedef typename 
        if_else< 
            is_same<Selector, Case1>::value, Value1, 
            typename if_else< is_same<Selector, Case2>::value, Value2, void_>::type 
        >::type type;
    };

    int foo() {
        switch_plain::type var1;
        var1.yyy();

        switch_complex<XXX, AAA, BBB, XXX, YYY>::type var2;
        var2.yyy();
    } 
}
  