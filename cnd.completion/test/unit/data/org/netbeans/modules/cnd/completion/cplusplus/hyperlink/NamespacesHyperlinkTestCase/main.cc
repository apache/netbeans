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

#include "file.h"

int main(int argc, char** argv) {
     // here test text is inserted
    S1::foo();
    S1::var1 = 14;
    S1::S2::boo();
    S1::S2::var2 = 105;
    return 0;
}

void usingNS1() {    
    using namespace S1;
    var1 = 10;
    foo();
    clsS1 c1;
    c1.clsS1pubFun();
}

void usingNS1S2() {
    using namespace S1::S2;
    var2 = 10;
    boo();
    clsS2 c2;
    c2.clsS2pubFun();
}

void usingDirectivesS1() {
    using S1::clsS1;
    clsS1 c1;
    using S1::var1;
    var1 = 10;
    using S1::foo;
    foo();
}

void usingDirectivesS1S2() {
    using S1::S2::clsS2;
    clsS2 c2;
    using S1::S2::var2;
    var2 = 10;
    using S1::S2::boo;
    boo();
}

void usingNS2() {
    using namespace S1;
    using namespace S2;
    
    var2 = 10;
    boo();
    clsS2 c2;
    c2.clsS2pubFun();    
}

void usingDirectivesS2() {
    using namespace S1;
    myCout = 10;
    using S2::clsS2;
    clsS2 c2;
    using S2::var2;
    var2 = 10;
    using S2::boo;
    boo();
}

void usingCout() {
    S1::myCout;
    using S1::myCout;
    myCout;
}

void usingS1AccessNestedTypesOfTemplatedClass {
    S1::myType::reference ref1;
    using namespace S1;
    myType::reference ref2;
}

// IZ#144982: std class members are not resolved in litesql
namespace XXX {
    struct string {
        int size();
    };
}

namespace YYY {
    using namespace XXX;
}

namespace YYY {
    int foo() {
        string s;
        s.size();
    }
}

namespace ZZZ {
    using namespace YYY;
    int bar() {
        string s;
        s.size();
    }
}
