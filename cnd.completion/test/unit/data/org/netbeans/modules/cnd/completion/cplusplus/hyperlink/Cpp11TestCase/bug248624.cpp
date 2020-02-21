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

namespace bug248624 {
    namespace std248624 {
      /// integral_constant
      template<typename _Tp, _Tp __v>
      struct integral_constant248624 {
        static constexpr _Tp value = __v;
      };    

      /// is_class
      template<typename _Tp>
      struct is_class248624 : public integral_constant248624<bool, __is_class(_Tp)> {};

      /// is_union
      template<typename _Tp>
      struct is_union248624 : public integral_constant248624<bool, __is_union(_Tp)> {};

      /// is_enum
      template<typename _Tp>
      struct is_enum248624 : public integral_constant248624<bool, __is_enum(_Tp)> {};
    }

    struct AAA248624 {
        int foo();
    };

    enum class BBB248624 {
        Value
    };

    union CCC248624 {
        int f1;
        float f2;
    };

    template <bool>
    struct Differentiator248624 {
        typedef int _false;
    };

    template <>
    struct Differentiator248624<true> {
        typedef int _true;
    };

    int main(int argc, char** argv) {
        Differentiator248624<std248624::is_class248624<AAA248624>::value>::_true var1;
        Differentiator248624<std248624::is_enum248624<BBB248624>::value>::_true var2;
        Differentiator248624<std248624::is_union248624<CCC248624>::value>::_true var3;
        return 0;
    }
}