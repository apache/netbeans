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
