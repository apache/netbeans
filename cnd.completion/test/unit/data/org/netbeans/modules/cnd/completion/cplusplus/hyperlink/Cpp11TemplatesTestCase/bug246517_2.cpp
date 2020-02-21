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

namespace bug246517_2 {   
    struct true_type246517_2 {
        constexpr static bool value = true;
    };

    struct false_type246517_2 {
        constexpr static bool value = false;
    };

    template <bool Cond, typename T1, typename T2>
    struct conditional246517_2 {
        typedef T2 type;
    };

    template <typename T1, typename T2>
    struct conditional246517_2<true, T1, T2> {
        typedef T1 type;
    };

    template<typename...>
      struct __or_246517_2;

    template<>
      struct __or_246517_2<>
      : public false_type246517_2
      { };

    template<typename _B1>
      struct __or_246517_2<_B1>
      : public _B1
      { };

    template<typename _B1, typename _B2>
      struct __or_246517_2<_B1, _B2>
      : public conditional246517_2<_B1::value, _B1, _B2>::type
      { };

    template<typename _B1, typename _B2, typename _B3, typename... _Bn>
      struct __or_246517_2<_B1, _B2, _B3, _Bn...>
      : public conditional246517_2<_B1::value, _B1, __or_246517_2<_B2, _B3, _Bn...>>::type
      { };

    template<typename...>
    struct __and_246517_2;

    template<>
      struct __and_246517_2<>
      : public true_type246517_2
      { };

    template<typename _B1>
      struct __and_246517_2<_B1>
      : public _B1
      { };

    template<typename _B1, typename _B2>
      struct __and_246517_2<_B1, _B2>
      : public conditional246517_2<_B1::value, _B2, _B1>::type
      { };

    template<typename _B1, typename _B2, typename _B3, typename... _Bn>
      struct __and_246517_2<_B1, _B2, _B3, _Bn...>
      : public conditional246517_2<_B1::value, __and_246517_2<_B2, _B3, _Bn...>, _B1>::type
      { };

    template <bool>
    struct AAA246517_2 {
        int false_fun();
    };

    template <>
    struct AAA246517_2<true> {
        int true_fun();
    };

    int test_and246517_2() {
        AAA246517_2<__and_246517_2<>::value> var1;
        var1.true_fun();      
        AAA246517_2<__and_246517_2<true_type246517_2>::value> var2;
        var2.true_fun();
        AAA246517_2<__and_246517_2<false_type246517_2>::value> var3;
        var3.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2>::value> var4;
        var4.true_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, false_type246517_2>::value> var5;
        var5.false_fun();
        AAA246517_2<__and_246517_2<false_type246517_2, true_type246517_2>::value> var6;
        var6.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, false_type246517_2>::value> var7;
        var7.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, true_type246517_2>::value> var8;
        var8.true_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, true_type246517_2, false_type246517_2>::value> var9;
        var9.false_fun();
        AAA246517_2<__and_246517_2<true_type246517_2, true_type246517_2, true_type246517_2, true_type246517_2>::value> var10;
        var10.true_fun();      
        return 0;
    }

    int test_or246517_2() {
        AAA246517_2<__or_246517_2<>::value> var1;
        var1.false_fun();      
        AAA246517_2<__or_246517_2<true_type246517_2>::value> var2;
        var2.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2>::value> var3;
        var3.false_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2>::value> var4;
        var4.false_fun();
        AAA246517_2<__or_246517_2<true_type246517_2, false_type246517_2>::value> var5;
        var5.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, true_type246517_2>::value> var6;
        var6.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, true_type246517_2>::value> var7;
        var7.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, false_type246517_2>::value> var8;
        var8.false_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, false_type246517_2, true_type246517_2>::value> var9;
        var9.true_fun();
        AAA246517_2<__or_246517_2<false_type246517_2, false_type246517_2, false_type246517_2, false_type246517_2>::value> var10;
        var10.false_fun();      
        return 0;
    }

    int test_and_or246517_2() {
        AAA246517_2<
          __and_246517_2<
              __and_246517_2<true_type246517_2, true_type246517_2>, 
              __or_246517_2<false_type246517_2, false_type246517_2>
            >::value
        > false_var1;
        false_var1.false_fun();

        AAA246517_2<
          __or_246517_2<
                false_type246517_2, 
                false_type246517_2,
                __and_246517_2<
                  true_type246517_2, 
                  true_type246517_2, 
                  true_type246517_2, 
                  __or_246517_2<false_type246517_2, false_type246517_2>
                > 
          >::value
        > false_var2;  
        false_var2.false_fun();

        AAA246517_2<
          __and_246517_2<
                true_type246517_2, 
                __and_246517_2<
                  true_type246517_2, 
                  true_type246517_2, 
                  true_type246517_2, 
                  __or_246517_2<false_type246517_2, true_type246517_2>
                >,
                __and_246517_2< 
                  __and_246517_2<true_type246517_2, true_type246517_2>,
                  __or_246517_2<false_type246517_2, false_type246517_2, true_type246517_2>
                >
          >::value
        > true_var; 
        true_var.true_fun();      

        return 0;
    } 
}