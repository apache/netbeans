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
public class EnumTestCase extends CompletionBaseTestCase {
    
    public EnumTestCase(String testName) {
        super(testName, true);
    }
    
        
    public void testClassAEnumFun() throws Exception {
        super.performTest("file.cc", 16, 5, "ClassA::");
    }  
    
    
    public void testClassAaPubFun() throws Exception {
        super.performTest("file.cc", 10, 5, "ClassA::");
    }

    public void testVarAaPubFun() throws Exception {
        super.performTest("file.cc", 10, 5, "this->");
    }
    
    public void testEnumInFun() throws Exception {
        super.performTest("file.cc", 16, 5);
    }
    
    public void testClassAEnumeratorsInFun() throws Exception {
        super.performTest("file.cc", 20, 5, "aa.");
    }
    
    public void testBug228519() throws Exception {
        super.performTest("bug228519.cpp", 34, 33);
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends CompletionBaseTestCase {
        @Override
        protected Class<?> getTestCaseDataClass() {
            return EnumTestCase.class;
        }
        
        public Failed(String testName) {
            super(testName, true);
        }

        public void testOK() {
            
        }
    }    
}
