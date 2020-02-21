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

package org.netbeans.modules.cnd.highlight.error;

/**
 * Error highlighting test case for missing semicolons
 */
public class MissedSemicolonsTestCase extends ErrorHighlightingBaseTestCase {

    static {
        System.setProperty("cnd.parser.error.transparent", "false");
        //System.setProperty("cnd.modelimpl.trace.error.provider", "true");
        //System.setProperty("parser.report.errors", "true");
    }
    
    public MissedSemicolonsTestCase(String testName) {
        super(testName);
    }
    
    public void testMissedSemicolonAfterClass() throws Exception {
        performStaticTest("missed_semicolon_after_class.cc"); //NOI18N
    }
    
    public void testDynamicSimple() throws Exception {
        MissedSemicolonsErrorMaker errorMaker = new MissedSemicolonsErrorMaker();
        performDynamicTest("missed_semicolon_simple_1.cc", errorMaker); //NOI18N
        performDynamicTest("missed_semicolon_simple_2.cc", errorMaker); //NOI18N
        errorMaker.printStatistics();
    }
}
