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

namespace IZ145148 {
    namespace Y {
        struct Z {
            int zz;
        };
    }
    void do_z() {
        Y::Z z;
        z.zz = 1; // "zz" is unresolved
    }
}

namespace IZ145148_2 {
    namespace Y {
        struct Z { int z; };
        int foo() {
            Y::Z zz;
            zz.z; // "z" is not resolved
        }
    }
}

// IZ 145142 : unable to resolve declaration imported from child namespace
namespace IZ145142_B {
    int b = 0;
}

namespace IZ145142_A {
    using namespace IZ145142_B;
    int a = 0;
}

namespace IZ145142_B {
    using namespace IZ145142_A;
}

void IZ145142_foo() {
    IZ145142_A::a++;
    IZ145142_B::a++;
    IZ145142_A::b++;
    IZ145142_B::b++;
}

namespace IZ145142_B_2 {
    int i = 0;
}

namespace IZ145142_A_2 {
    namespace IZ145142_B_2 {
        typedef int S;
    }
    using IZ145142_B_2::S;

}
typedef IZ145142_A_2::S IZ145142_T;
