/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
