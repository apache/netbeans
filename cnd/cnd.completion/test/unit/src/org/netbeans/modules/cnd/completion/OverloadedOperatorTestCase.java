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
