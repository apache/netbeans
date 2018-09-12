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

package org.netbeans.modules.javascript2.prototypejs.model;

/**
 *
 * @author Petr Pisl
 */
public class TestIssue201885 extends PrototypeJsTestBase {

    private static final String filePath = "basic/issue201885.js";
    
    public TestIssue201885(String testName) {
        super(testName);
    }
    
    public void testStructure() throws Exception {
        checkStructure(filePath);
    }

    public void testSemantic() throws Exception {
        checkSemantic(filePath);
    }
    
    public void testCC_01() throws Exception {
        checkCompletion(filePath, "var person = new Pe^rson();", false);
    }
    
    public void testCC_02() throws Exception {
        checkCompletion(filePath, "person.^", false);
    }
    
    public void testOccurreces_01() throws Exception {
        checkOccurrences(filePath, "var Per^son = Class.create({", true);
    }
    
    public void testOccurreces_02() throws Exception {
        checkOccurrences(filePath, "initialize: function(nam^e){", true);
    }
   
}
