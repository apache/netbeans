/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.Arrays;

/**
 *
 */
public class WhereUsedTestCase extends CsmWhereUsedQueryPluginTestCaseBase {

    public WhereUsedTestCase(String testName) {
        super(testName);
    }

    public void testIZ211703_1() throws Exception {
        // IZ#211703 : Find Usages can not find references to functions
        performWhereUsed("iz211703_1.c", 3, 20);
        performWhereUsed("iz211703_1.c", 5, 20);
        performWhereUsed("iz211703_1.c", 11, 20);
        performWhereUsed("iz211703_2.c", 4, 20);
        performWhereUsed("iz211703_2.c", 7, 20);
    }

    public void testIZ211703_2() throws Exception {
        // IZ#211703 : Find Usages can not find references to functions
        performWhereUsed("iz211703_1.c", 2, 30);
        performWhereUsed("iz211703_1.c", 12, 30);
        performWhereUsed("iz211703_1.c", 15, 30);
        performWhereUsed("iz211703_2.c", 3, 30);
        performWhereUsed("iz211703_2.c", 8, 30);
    }
    
    public void test219526_1() throws Exception {
        // IZ#219526 - incorrect Find Usages for the same qualified classifiers
        performWhereUsed("iz219526_1.cpp", 1, 20);
        performWhereUsed("iz219526_1.cpp", 3, 10);
    }
    
    public void test219526_2() throws Exception {
        // IZ#219526 - incorrect Find Usages for the same qualified classifiers
        performWhereUsed("iz219526_2.cpp", 1, 20);
        performWhereUsed("iz219526_2.cpp", 3, 10);
    }    
    
    public void test216130_1() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130.h", 6, 20);
        performWhereUsed("iz216130_2.c", 4, 20);
        
        performWhereUsed("iz216130_1.c", 12, 20);
        performWhereUsed("iz216130_2.c", 15, 20);
    }
    
    public void test216130_2() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130.h", 7, 20);
        performWhereUsed("iz216130_1.c", 5, 20);
        
        performWhereUsed("iz216130_1.c", 12, 40);
        performWhereUsed("iz216130_2.c", 15, 40);
    }

    public void test216130_3() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130_1.c", 7, 20);
        performWhereUsed("iz216130_2.c", 9, 20);
        
        performWhereUsed("iz216130_1.c", 12, 60);
        performWhereUsed("iz216130_2.c", 15, 60);
    }
    
    public void test216130_4() throws Exception {
        // IZ#216130 Find usages: only references to the file where a global definition is defined are found
        performWhereUsed("iz216130_1.c", 10, 20);
        performWhereUsed("iz216130_2.c", 12, 20);
        
        performWhereUsed("iz216130_1.c", 12, 80);
        performWhereUsed("iz216130_2.c", 15, 80);
    }
    
    public void test228094() throws Exception {
        // IZ#228094 - Refactoring: only usages are changed, #define in header from the refactoring was called, remains unchanged
        performWhereUsed("iz228094.cpp", 1, 10, null, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(), CsmWhereUsedFilters.MACROS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void test268930_1() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performWhereUsed("bug268930.cpp", 2, 32);
    }
    
    public void test268930_2() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performWhereUsed("bug268930.cpp", 6, 25);
    }
    
    public void test268930_3() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performWhereUsed("bug268930.cpp", 10, 30);
    }
}
