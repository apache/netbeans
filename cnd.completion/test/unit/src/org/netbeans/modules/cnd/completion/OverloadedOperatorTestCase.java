/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 *
 */
public class OverloadedOperatorTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of StaticMembersTestCase
     */
    public OverloadedOperatorTestCase(String testName) {
        super(testName, true);
    }

    public void testNextLevelArrow() throws Exception {
        performTest("file.cc", 7, 5, "(*pC).fieldCB->");
    }
    
    public void testOverloadedArrowOnB() throws Exception {
        performTest("file.cc", 7, 5, "b->");
    }
    
    public void testOverloadedArrowOnPtrB() throws Exception {
        performTest("file.cc", 7, 5, "pB->");
    }

    public void testOverloadedArrowOnC() throws Exception {
        performTest("file.cc", 7, 5, "c->");
    }
    
    public void testOverloadedArrowOnPtrC() throws Exception {
        performTest("file.cc", 7, 5, "pC->");
    }

    public void testOverloadedArrayOnC() throws Exception {
        performTest("file.cc", 7, 5, "c[1].");
    }
    
    public void testOverloadedArrowArrayOnC() throws Exception {
        performTest("file.cc", 7, 5, "c[1]->");
    }
    
    public void testInstantiationOverloadedArrowOnB() throws Exception {
        performTest("file.cc", 15, 5, "b->");
    }
    
    public void testInstantiationOverloadedArrowOnPtrB() throws Exception {
        performTest("file.cc", 15, 5, "pB->");
    }
    
    public void testInstantiationOverloadedArrowOnC() throws Exception {
        performTest("file.cc", 15, 5, "c->");
    }

    public void testInstantiationOverloadedArrowOnPtrC() throws Exception {
        performTest("file.cc", 15, 5, "pC->");
    }

    public void testInstantiationOverloadedArrayOnC() throws Exception {
        performTest("file.cc", 15, 5, "c[1].");
    }   
    
    public void testInstantiationOverloadedArrowArrayOnC() throws Exception {
        performTest("file.cc", 15, 5, "c[1]->");
    }       
    
    public void testBug254273() throws Exception {
        performTest("bug254273.cpp", 19, 9, "ccc->");
    }
    
    public void testBug268930_1() throws Exception {
        performTest("bug268930_cc.cpp", 46, 13);
    }
    
    public void testBug268930_2() throws Exception {
        performTest("bug268930_cc.cpp", 47, 14);
    }
    
    public void testBug268930_3() throws Exception {
        performTest("bug268930_cc.cpp", 49, 21);
    }
    
    public void testBug268930_4() throws Exception {
        performTest("bug268930_cc.cpp", 53, 27);
    }
    
    public void testBug268930_5() throws Exception {
        performTest("bug268930_cc.cpp", 56, 27);
    }
}
