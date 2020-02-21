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

namespace bug249833 {
    struct True249833 {
        constexpr static bool value = true;
    };
    struct False249833 {
        constexpr static bool value = false;
    };

    template <bool val, typename T1, typename T2> 
    struct If249833 {
        typedef T2 type;
    };

    template <typename T1, typename T2> 
    struct If249833<true, T1, T2> {
        typedef T1 type;
    };

    template <typename...Elems>
    struct And249833 {};

    template <typename Element>
    struct And249833<Element> : If249833<Element::value, True249833, False249833>::type {};

    template <typename Element, typename...Elems> 
    struct And249833<Element, Elems...> : If249833<Element::value, And249833<Elems...>, False249833>::type {};

    int main249833() {
        And249833<True249833, True249833, False249833>::value; 
        And249833<True249833, True249833, True249833>::value; 
        And249833<
                True249833, 
                True249833, 
                True249833, 
                True249833, 
                And249833<True249833, True249833, False249833>>::value; 
        And249833<
                True249833, 
                True249833, 
                And249833<True249833, True249833>, 
                And249833<True249833, True249833, True249833>>::value; 
        return 0;
    } 
}