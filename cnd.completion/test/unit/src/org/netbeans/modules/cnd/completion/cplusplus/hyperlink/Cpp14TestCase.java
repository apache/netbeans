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

import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;

/**
 *
 *
 */
public class Cpp14TestCase extends HyperlinkBaseTestCase {

    public Cpp14TestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp14", "true");         
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("cnd.language.flavor.cpp14", "false");
    }

    public void testBug268671() throws Exception {
        // Bug 268671 - C++14: IDE parser fails on "Variable templates"
        performTest("bug268671.cpp", 15, 32, "bug268671.cpp", 6, 9);
        performTest("bug268671.cpp", 16, 39, "bug268671.cpp", 6, 9);
    }
    
    public void testBug269290() throws Exception {
        // Bug 269290 - C++14: unresolved return type of function with auto type
        performTest("bug269290.cpp", 25, 23, "bug269290.cpp", 3, 9);
        performTest("bug269290.cpp", 27, 21, "bug269290.cpp", 3, 9);
    }
    
    public void testBug269292() throws Exception {
        // Bug 269292 - С++14: decltype(auto) is not supported
        performTest("bug269292.cpp", 17, 23, "bug269292.cpp", 3, 9);
        performTest("bug269292.cpp", 18, 23, "bug269292.cpp", 3, 9);
        performTest("bug269292.cpp", 19, 23, "bug269292.cpp", 7, 9);
        performTest("bug269292.cpp", 21, 15, "bug269292.cpp", 7, 9);
    }
}
