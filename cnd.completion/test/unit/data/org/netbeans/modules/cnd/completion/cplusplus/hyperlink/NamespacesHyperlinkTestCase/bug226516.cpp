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

namespace bug226516_common {
namespace bug226516_api {
    enum {
        bug226516_first = 1,
        bug226516_second = 2,
    };
}
}

namespace bug226516_aaa {
namespace bug226516_bbb {
    namespace bug226516_api = ::bug226516_common::bug226516_api;

    void bug226516_f()
    {
        // !!! shall follow link to common::api::first
        std::cout << "val=" << bug226516_api::bug226516_first << std::endl;
    }
}
}

namespace bug226516_ccc {
namespace bug226516_ddd {
    using namespace bug226516_common::bug226516_api;
}
}
/*
 * 
 */

namespace bug226516_cd {
    struct bug226516_AA {
    };
}


int bug226516_main(int argc, char** argv) 
{
    // this shall get precendence over above 
    namespace bug226516_cd = ::bug226516_ccc::bug226516_ddd;

    bug226516_aaa::bug226516_bbb::bug226516_f();

    // !!! shall follow link to common::api::second
    std::cout << "val=" << bug226516_cd::bug226516_second << std::endl;


    // !!! shall not follow the link to cd::AA as it shall be ::ccc::ddd::AA -
    //compile time error
    bug226516_cd::bug226516_AA aa;
    
    ::bug226516_cd::bug226516_AA aa;

    return 0;
}