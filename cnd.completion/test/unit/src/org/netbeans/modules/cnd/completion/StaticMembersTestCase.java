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
public class StaticMembersTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of StaticMembersTestCase
     */
    public StaticMembersTestCase(String testName) {
        super(testName, true);
    }

    /////////////////////////////////////////////////////////////////
    // int main() {
    
    public void testMainStaticClassA() throws Exception {
        super.performTest("main.cc", 10, 5, "ClassA::");
    }
    
    /////////////////////////////////////////////////////////////////
    // void ClassA::aPubFun() {

    public void testStaticClassAaPubFunVarA() throws Exception {
        performTest("file.cc", 10, 5, "a.");
    }

    
    public void testStaticClassAaPubFunVarC() throws Exception {
        performTest("file.cc", 10, 5, "c.");
    }
    
    public void testStaticClassAaPubFunVarE() throws Exception {
        performTest("file.cc", 10, 5, "e.");
    }    
            
    public void testStaticClassAaPubFunVarB() throws Exception {
        performTest("file.cc", 10, 5, "b.");
    }  
    
    public void testStaticClassAaPubFunVarD() throws Exception {
        performTest("file.cc", 10, 5, "d.");
    }      
    
    public void testStaticClassDaPubFunClassA() throws Exception {
        performTest("file.cc", 37, 5, "::ClassA::");
    }      
    public void testStaticClassDaPubFunClassB() throws Exception {
        performTest("file.cc", 37, 5, "::ClassB::");
    }      
    public void testStaticClassDaPubFunClassC() throws Exception {
        performTest("file.cc", 37, 5, "::ClassC::");
    }      
    public void testStaticClassDaPubFunClassD() throws Exception {
        performTest("file.cc", 37, 5, "::ClassD::");
    }      
    public void testStaticClassDaPubFunClassE() throws Exception {
        performTest("file.cc", 37, 5, "::ClassE::");
    }      
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        @Override
        protected Class<?> getTestCaseDataClass() {
            return StaticMembersTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }

        public void testOK() {
            
        }
        
    }
}
