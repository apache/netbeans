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
package org.netbeans.modules.cnd.completion.includes;

/**
 *
 *
 */
public class CCIncludesCompletionTestCase extends IncludesCompletionBaseTestCase {

    /**
     * Creates a new instance of CCIncludesCompletionTestCase
     */
    public CCIncludesCompletionTestCase(String testName) {
        super(testName);
    }    
    
    public void testNothing() throws Exception {
        performTest("file.cc", 1, 1, " ");
    }
    
    public void testEmtpy() throws Exception {
        performTest("file.cc", 1, 1, "#include ");
    }
    
    public void testEmtpyUsr() throws Exception {
        performTest("file.cc", 1, 1, "#include \"\"", -1);
    }
    
    public void testEmtpySys() throws Exception {
        performTest("file.cc", 1, 1, "#include <>", -1);
    } 
    
    public void testSmthUsr() throws Exception {
        performTest("file.cc", 1, 1, "#include \"us\"", -1);
    }
    
    public void testSmthSys() throws Exception {
        performTest("file.cc", 1, 1, "#include <inc>", -1);
    }    
        
    // IZ 119931 : Class name is suggested in include directive
    public void testPrefix1() throws Exception {
        performTest("file.cc", 1, 1, "#include incl");
    }    

    // IZ 119931 : Class name is suggested in include directive
    public void testPrefix2() throws Exception {
        performTest("file.cc", 1, 1, "#include us");
    }

    public void testInclWoExt() throws Exception {
        // IZ#158074: Qt headers doesn't appear in code completion list
        performTest("file.cc", 1, 1, "#include \"usr_incl/no\"", -1);
    }
}
