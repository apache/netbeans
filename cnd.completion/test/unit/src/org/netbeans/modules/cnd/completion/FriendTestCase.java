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
 * test cases for resolving completion in friends context
 *
 */
public class FriendTestCase extends CompletionBaseTestCase {
    
    public FriendTestCase(String testName) {
        super(testName, true);
    }
    
    public void testInFriendFuncVarA() throws Exception {
        super.performTest("file.cc", 14, 5, "a.");
    }
    
    public void testInFriendFuncVarB() throws Exception {
        super.performTest("file.cc", 14, 5, "b.");
    }
    
    public void testInFriendFuncVarD() throws Exception {
        super.performTest("file.cc", 14, 5, "d.");
    }     
        
    public void testInFriendFuncVarASt() throws Exception {
        super.performTest("file.cc", 14, 5, "ClassA::");
    }
    
    public void testInFriendFuncVarBSt() throws Exception {
        super.performTest("file.cc", 14, 5, "ClassB::");
    }

    public void testInFriendFuncVarDSt() throws Exception {
        super.performTest("file.cc", 14, 5, "ClassD::");
    }
        
    public void testInFriendCClassVarE() throws Exception {
        super.performTest("file.cc", 19, 5, "e.");
    }
            
    public void testInFriendCClassVarESt() throws Exception {
        super.performTest("file.cc", 19, 5, "ClassE::");
    }

    public void testInFriendCClassVarA() throws Exception {
        super.performTest("file.cc", 7, 5, "a.");
    }
    
    public void testInFriendCClassVarB() throws Exception {
        super.performTest("file.cc", 7, 5, "b.");
    }    
    
    public void testInFriendCClassVarASt() throws Exception {
        super.performTest("file.cc", 7, 5, "ClassA::");
    }
    
    public void testInFriendCClassVarBSt() throws Exception {
        super.performTest("file.cc", 7, 5, "ClassB::");
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        @Override
        protected Class<?> getTestCaseDataClass() {
            return FriendTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }

        public void testOK() {
            
        }
    
    }    
}
