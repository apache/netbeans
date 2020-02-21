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
public class InheritanceTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of InheritanceTestCase
     */
    public InheritanceTestCase(String testName) {
        super(testName, true);
    }
    
    /////////////////////////////////////////////////////////////////
    // void ClassA::aPubFun() {
    
    public void testClassAaPubFunVarA() throws Exception {
        performTest("file.cc", 10, 5, "a.");
    }
    
    public void testClassAaPubFunVarB() throws Exception {
        performTest("file.cc", 10, 5, "b.");
    }  
    
    public void testClassAaPubFunVarC() throws Exception {
        performTest("file.cc", 10, 5, "c.");
    }       
    
    public void testClassAaPubFunVarD() throws Exception {
        performTest("file.cc", 10, 5, "d.");
    }
    
    public void testClassAaPubFunVarE() throws Exception {
        performTest("file.cc", 10, 5, "e.");
    }  
    
    public void testClassAaPubFunClassA() throws Exception {
        performTest("file.cc", 10, 5, "ClassA::");
    }     
    ///////////////////////////////////////////////////////////////////
    // void ClassB::bProtFun() {
    
    public void testClassBbProtFunVarA() throws Exception {
        performTest("file.cc", 19, 5, "a.");
    }
    
    public void testClassBbProtFunVarB() throws Exception {
        performTest("file.cc", 19, 5, "b.");
    }
    
    public void testClassBbProtFunVarC() throws Exception {
        performTest("file.cc", 19, 5, "c.");
    }
    
    public void testClassBbProtFunVarD() throws Exception {
        performTest("file.cc", 19, 5, "d.");
    }    

    public void testClassBbProtFunVarE() throws Exception {
        performTest("file.cc", 19, 5, "e.");
    }
    
    public void testClassBbProtFunClassA() throws Exception {
        performTest("file.cc", 19, 5, "ClassA::");
    }
    
    public void testClassBbProtFunClassB() throws Exception {
        performTest("file.cc", 19, 5, "ClassB::");
    }    
    ////////////////////////////////////////////////////////////////////
    // void ClassC::cPrivFun() {
    
    public void testClassCcPrivFunVarA() throws Exception {
        performTest("file.cc", 28, 5, "a.");
    }
    
    public void testClassCcPrivFunVarB() throws Exception {
        performTest("file.cc", 28, 5, "b.");
    }
    
    public void testClassCcPrivFunVarC() throws Exception {
        performTest("file.cc", 28, 5, "c.");
    }

    public void testClassCcPrivFunVarD() throws Exception {
        performTest("file.cc", 28, 5, "d.");
    }
        
    public void testClassCcPrivFunVarE() throws Exception {
        performTest("file.cc", 28, 5, "e.");
    }  
    
    public void testClassCcPrivFunClassC() throws Exception {
        performTest("file.cc", 28, 5, "ClassC::");
    }     
    ////////////////////////////////////////////////////////////////////
    // void ClassD::dPubFun() {
    
    public void testClassDdPubFunVarA() throws Exception {
        performTest("file.cc", 37, 5, "a.");
    }

    public void testClassDdPubFunVarB() throws Exception {
        performTest("file.cc", 37, 5, "b.");
    }

    public void testClassDdPubFunVarC() throws Exception {
        performTest("file.cc", 37, 5, "c.");
    }
        
    public void testClassDdPubFunVarD() throws Exception {
        performTest("file.cc", 37, 5, "d.");
    }
    
    public void testClassDdPubFunVarE() throws Exception {
        performTest("file.cc", 37, 5, "e.");
    }
    
    public void testClassDdPubFunClassA() throws Exception {
        performTest("file.cc", 37, 5, "::ClassA::");
    }    
    
    public void testClassDdPubFunClassB() throws Exception {
        performTest("file.cc", 37, 5, "ClassB::");
    }    

    public void testClassDdPubFunClassC() throws Exception {
        performTest("file.cc", 37, 5, "ClassC::");
    }    
    
    public void testClassDdPubFunClassD() throws Exception {
        performTest("file.cc", 37, 5, "ClassD::");
    }     
    ////////////////////////////////////////////////////////////////////
    // void ClassE::ePubFun() {
    
    public void testClassEePubFunVarA() throws Exception {
        performTest("file.cc", 46, 5, "a.");
    }
    
    public void testClassEePubFunVarB() throws Exception {
        performTest("file.cc", 46, 5, "b.");
    }
        
    public void testClassEePubFunVarC() throws Exception {
        performTest("file.cc", 46, 5, "c.");
    }
        
    public void testClassEePubFunVarD() throws Exception {
        performTest("file.cc", 46, 5, "d.");
    }
    
    public void testClassEePubFunVarE() throws Exception {
        performTest("file.cc", 46, 5, "e.");
    }
        
    public void testClassEePubFunClassC() throws Exception {
        performTest("file.cc", 46, 5, "ClassC::");
    }    
    
    public void testClassEePubFunClassE() throws Exception {
        performTest("file.cc", 46, 5, "ClassE::");
    }     
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        @Override
        protected Class<?> getTestCaseDataClass() {
            return InheritanceTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }       

        public void testOK() {
            
        }
    }
        
}
