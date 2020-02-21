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

namespace bug246803 {
    template <typename = struct DDD246803>
    struct AAA246803 {
        int foo();
    };

    template <>
    struct AAA246803<DDD246803> {
        int boo();
    };

    void mainAAA246803() {
        AAA246803<> var;
        var.boo();
    }

    template <bool, typename = int>
    struct BBB246803 {
        int foo();
    };

    template <typename T>
    struct BBB246803<false, T> {
        int boo();
    };

    template <>
    struct BBB246803<true, int> {
        int roo();
    };

    template <>
    struct BBB246803<false, int> {
        int doo();
    };

    void mainBBB246803() {
        BBB246803<true> var1;
        var1.roo();
        BBB246803<false, int> var2;
        var2.doo();
        BBB246803<false, double> var3;
        var3.boo();
    }

    template <typename...>
    struct CCC246803 {
        int foo();
    };

    template <typename T>
    struct CCC246803<T> {
        int boo();
    };

    void mainCCC246803() {
        CCC246803<> var1;
        var1.foo();
        CCC246803<int> var2;
        var2.boo();
    }
}