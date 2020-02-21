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

namespace bug235044 {
    
    struct AAA_bug235044 {
        int foo();
    };

    template <class T> using A1_bug235044 = T;

    struct BBB_bug235044 {
        template <class T> using A2_bug235044 = T;
    };

    template<class T>
    struct CCC_bug235044 {
        using A3_bug235044 = T;
    };

    template <class _T> using A4_bug235044 = typename CCC_bug235044<_T>::A3_bug235044;

    template<class T1, class T2>
    struct DDD_bug235044 {
        typedef T1 type1;
        typedef T2 type2;
    };

    template <class T>
    struct EEE_bug235044 {
        template <class _T> using A5_bug235044 = typename DDD_bug235044<T, _T>::type1;
        template <class _T> using A6_bug235044 = typename DDD_bug235044<T, _T>::type2;
    };


    int foo_bug235044() { 
        A1_bug235044<AAA_bug235044> a; 
        a.foo();

        BBB_bug235044::A2_bug235044<AAA_bug235044> b;
        b.foo(); 

        CCC_bug235044<AAA_bug235044>::A3_bug235044 c;
        c.foo(); 

        A4_bug235044<AAA_bug235044> d;
        d.foo();

        EEE_bug235044<AAA_bug235044>::A5_bug235044<int> e;
        e.foo();

//        EEE_bug235044<int>::A6_bug235044<AAA_bug235044> f;
//        f.foo();
    }  
}
