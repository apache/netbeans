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

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 *
 */
public class NamespacesTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of NamespacesTestCase
     */
    public NamespacesTestCase(String testName) {
        super(testName, true);
    }
    
    public void testNs1StructVariable() throws Exception {
        // IZ#102894: Hyperlink and Code Completion works badly with namespaces
        super.performTest("file.cc", 39, 5, "q.");
    }
    
    public void testNs2() throws Exception {
        // IZ#102894: Hyperlink and Code Completion works badly with namespaces
        super.performTest("file.cc", 43, 5, "using namespace S1::");
    }    
    public void testInFunction() throws Exception {
        super.performTest("file.cc", 5, 5);
    }        
    
    public void testInFunctionNsS1AsPrefix() throws Exception {
        // IZ84115: "Code Completion" works incorrectly with namespaces
        super.performTest("file.cc", 5, 5, "S1::");
    }      

    public void testInFunctionNsS1S2AsPrefix() throws Exception {
        // IZ84115: "Code Completion" works incorrectly with namespaces
        super.performTest("file.cc", 5, 5, "S1::S2::");
    }      
    
    public void testInFunctionAliasesS1() throws Exception {
        // IZ#117792: Code completion should display namespace aliases
        super.performTest("file.cc", 57, 5, "AliasS1::");
    }
    
    public void testInFunctionAliasesS2() throws Exception {
        // IZ#117792: Code completion should display namespace aliases
        super.performTest("file.cc", 57, 5, "AliasS2::");
    }

    public void testInnerNSElems1() throws Exception {
        // IZ#123420: no completion for deep lucene namespaces
        super.performTest("file.cc", 61, 5, "S3::S4::");        
    }
    
    public void testInnerNSElems2() throws Exception {
        // IZ#123420: no completion for deep lucene namespaces
        super.performTest("file.cc", 61, 5, "S3::S4::S5::");        
    }

    public void testInnerNSFunc1() throws Exception {
        // IZ#123420: no completion for deep lucene namespaces
        super.performTest("file.cc", 61, 5, "S3::S4::S4Class::");
    }
    
    public void testInnerNSFunc2() throws Exception {
        // IZ#123420: no completion for deep lucene namespaces
        super.performTest("file.cc", 61, 5, "S3::S4::S5::S5Class::");
    }
    
    public void testInnerNSFunc3() throws Exception {
        // IZ#123420: no completion for deep lucene namespaces
        super.performTest("file.cc", 61, 5, "S3::S4::S5::S5Class::pPtrS5Class->");
    }
    
    public void testStaticMembers1() throws Exception {
        super.performTest("file2.cc", 13, 9);
    }

    public void testStaticMembers2() throws Exception {
        super.performTest("file2.cc", 15, 5);
    }

    public void testStaticMembers3() throws Exception {
        super.performTest("file2.cc", 17, 1);
    }
    
    public void testStaticMembers4() throws Exception {
        super.performTest("file2.cc", 17, 1, "S1::");
    }

    public void testStaticMembers5() throws Exception {
        super.performTest("file2.cc", 17, 1, "S1::S2::");
    }

    public void testIZ146962() throws Exception {
        super.performTest("iz146962.cc", 3, 5, "Gtk::");
    }
    
    public void test231548() throws Exception {
        super.performTest("231548.cc", 23, 5, "s.");
    }
}
