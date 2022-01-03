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
public class TemplateInstantiationTestCase extends CompletionBaseTestCase {

    public TemplateInstantiationTestCase(String testName) {
        super(testName, false);
    }

    public void test1() throws Exception {
        super.performTest("instantiation.cc", 8, 12);
    }

    public void test2() throws Exception {
        super.performTest("instantiation.cc", 9, 11);
    }

    public void test3() throws Exception {
        super.performTest("instantiation.cc", 10, 15);
    }

    public void test4() throws Exception {
        super.performTest("instantiation.cc", 11, 16);
    }

    public void testFoo1_1() throws Exception {
        super.performTest("instantiation.cc", 8, 21);
    }
    
    public void testFoo1_2() throws Exception {
        super.performTest("instantiation.cc", 10, 24);
    }
    
    public void testFoo1_3() throws Exception {
        super.performTest("instantiation.cc", 11, 34);
    }    

    public void testBoo1_1() throws Exception {
        super.performTest("instantiation.cc", 9, 20);
    }
    
    public void testBoo1_2() throws Exception {
        super.performTest("instantiation.cc", 12, 36);
    }    
            
    public void testPointerDepthInSimpleInstantiation() throws Exception {
        super.performTest("pointerDepthInSimpleInstantiation.cpp", 14, 25);
    }
}
