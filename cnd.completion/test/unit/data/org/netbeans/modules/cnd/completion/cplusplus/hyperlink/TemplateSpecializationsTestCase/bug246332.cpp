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

namespace bug246332 {
    template <typename T>
    struct tpl246332 {};

    template <typename A, typename B, typename C>
    struct tpl246332<A(B, C)> {
        typedef int two_params;
    };

    template <typename A, typename B, typename C, typename D>
    struct tpl246332<A(B, C, D)> {
        typedef int three_params;
    };

    typedef int zzz246332;
    typedef int yyy246332;
    typedef int xxx246332; 

    typedef int Fun1_246332(int, xxx246332);
    typedef int Fun2_246332(int, xxx246332, float);
    typedef zzz246332 Fun3_246332(yyy246332, xxx246332, float);

    void foo246332() {
        // Unresolved nested types:
        tpl246332<int(int, xxx246332)>::two_params var1;
        tpl246332<int(int, xxx246332, float)>::three_params var2;
        tpl246332<zzz246332(yyy246332, xxx246332, float)>::three_params var3;
        tpl246332<Fun1_246332>::two_params var4;
        tpl246332<Fun2_246332>::three_params var5;
        tpl246332<Fun3_246332>::three_params var6;    
    }  
}