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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

/**
 *
 *
 */
public class Cpp11TemplatesTestCase extends HyperlinkBaseTestCase {

    public Cpp11TemplatesTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp11", "true");         
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("cnd.language.flavor.cpp11", "false");
    }
    
    public void testBug238847_1() throws Exception {
        // Bug 238913 - Unable to deduce type through uniqe_ptr and decltype
        performTest("bug238847_1.cpp", 72, 16, "bug238847_1.cpp", 66, 9);
        performTest("bug238847_1.cpp", 75, 16, "bug238847_1.cpp", 66, 9);
    }
    
    public void testBug238847_3() throws Exception {
        // Bug 238913 - Unable to deduce type through uniqe_ptr and decltype
        performTest("bug238847_3.cpp", 119, 15, "bug238847_3.cpp", 113, 9);
        performTest("bug238847_3.cpp", 122, 22, "bug238847_3.cpp", 113, 9);
    }    
    
    public void testBug238889() throws Exception {
        // Bug 238889 - C++11: Unresolved identifier if templates are closed by RSHIFT token
        performTest("bug238889.cpp", 17, 65, "bug238889.cpp", 13, 9);
        performTest("bug238889.cpp", 18, 87, "bug238889.cpp", 13, 9);
    }        
    
    public void testBug239901() throws Exception {
        // Bug 239901 - Unresolved identifier in type alias template with default type 
        performTest("bug239901.cpp", 19, 13, "bug239901.cpp", 3, 9);
        performTest("bug239901.cpp", 22, 13, "bug239901.cpp", 7, 9);
        performTest("bug239901.cpp", 25, 18, "bug239901.cpp", 3, 9);
    }            
    
    public void testBug246517() throws Exception {
        // Bug 246517 - Cannot resolve elements of std::tuple
        performTest("bug246517.cpp", 88, 22, "bug246517.cpp", 77, 9);
        performTest("bug246517.cpp", 89, 22, "bug246517.cpp", 80, 9);
        performTest("bug246517.cpp", 90, 22, "bug246517.cpp", 83, 9);
        performTest("bug246517.cpp", 92, 23, "bug246517.cpp", 77, 9);
        performTest("bug246517.cpp", 93, 23, "bug246517.cpp", 80, 9);
        performTest("bug246517.cpp", 94, 23, "bug246517.cpp", 83, 9);        
        performTest("bug246517.cpp", 96, 23, "bug246517.cpp", 77, 9);
        performTest("bug246517.cpp", 97, 23, "bug246517.cpp", 80, 9);
        performTest("bug246517.cpp", 98, 23, "bug246517.cpp", 83, 9); 
        performTest("bug246517.cpp", 100, 23, "bug246517.cpp", 77, 9);
        performTest("bug246517.cpp", 101, 23, "bug246517.cpp", 80, 9);
        performTest("bug246517.cpp", 102, 23, "bug246517.cpp", 83, 9);           
    }                
    
    public void testBugbug246548_1() throws Exception {
        // Bug 246548 - Cannot reach base class in case of simple variadic recursion
        performTest("bug246548_1.cpp", 16, 37, "bug246548_1.cpp", 7, 9);
        performTest("bug246548_1.cpp", 17, 37, "bug246548_1.cpp", 12, 9);
    }
    
    public void testBugbug246548_2() throws Exception {
        // Bug 246548 - Cannot reach base class in case of simple variadic recursion
        performTest("bug246548_2.cpp", 16, 40, "bug246548_2.cpp", 7, 9);
        performTest("bug246548_2.cpp", 17, 40, "bug246548_2.cpp", 12, 9);
    }    
    
    public void testBug246683() throws Exception {
        // Bug 246683 - C++11: variadic template and partial specialization
        performTest("bug246683.cpp", 20, 32, "bug246683.cpp", 4, 9);
        performTest("bug246683.cpp", 21, 26, "bug246683.cpp", 11, 9);
        performTest("bug246683.cpp", 22, 25, "bug246683.cpp", 14, 9);
    }    
    
    public void testBug246517_2() throws Exception {
        // Bug 246517 - Cannot resolve elements of std::tuple
        performTest("bug246517_2.cpp", 78, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 80, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 82, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 84, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 86, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 88, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 90, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 92, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 94, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 96, 18, "bug246517_2.cpp", 73, 9);
        
        performTest("bug246517_2.cpp", 102, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 104, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 106, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 108, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 110, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 112, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 114, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 116, 18, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 118, 18, "bug246517_2.cpp", 73, 9);
        performTest("bug246517_2.cpp", 120, 18, "bug246517_2.cpp", 68, 9);      
        
        performTest("bug246517_2.cpp", 131, 24, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 145, 24, "bug246517_2.cpp", 68, 9);
        performTest("bug246517_2.cpp", 162, 22, "bug246517_2.cpp", 73, 9);    
    } 
    
    public void testBug248600() throws Exception {
        performTest("bug248600.cpp", 139, 16, "bug248600.cpp", 130, 9);
    }
    
    public void testBug248747() throws Exception {
        performTest("bug248747.cpp", 83, 17, "bug248747.cpp", 78, 9);
    }    
    
    public void testBug249833() throws Exception {
        performTest("bug249833.cpp", 29, 59, "bug249833.cpp", 6, 9);
        performTest("bug249833.cpp", 30, 58, "bug249833.cpp", 3, 9);
        performTest("bug249833.cpp", 36, 68, "bug249833.cpp", 6, 9);
        performTest("bug249833.cpp", 41, 67, "bug249833.cpp", 3, 9);
    }
    
    public void testBug252597() throws Exception {
        performTest("bug252597.cpp", 25, 12, "bug252597.cpp", 20, 5);
    }
    
    public void testBug257038() throws Exception {
        // Bug 257038 - C++14: make_unique and unresolved identifier
        performTest("bug257038.cpp", 45, 14, "bug257038.cpp", 40, 9);
    }
    
    public void testBug262801() throws Exception {
        // Bug 262801 - Errors when parsing conditional expression inside template
        performTest("bug262801.cpp", 15, 15, "bug262801.cpp", 10, 9);
        performTest("bug262801.cpp", 16, 42, "bug262801.cpp", 4, 9);
    }
    
    public void testBug267502() throws Exception {
        // Bug 267502 - Unresolved identifiers in editor on Ubuntu 16.04 (check C++11 STL API)
        performTest("bug267502.cpp", 24, 14, "bug267502.cpp", 3, 9);
    }
    
    public void testBug267655() throws Exception {
        // Bug 267655 - Bad support of template template parameters
        performTest("bug267655.cpp", 41, 14, "bug267655.cpp", 26, 9);
    }
}