package org.netbeans.modules.groovy.editor.api.completion;


import org.netbeans.modules.groovy.editor.api.completion.GroovyCCTestBase;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 *
 * @author Petr Pisl
 */
public class SpockCCTest extends GroovyCCTestBase {

    public SpockCCTest(String testName) {
        super(testName);
    }
    
    @Override
    protected String getTestType() {
        return "spock";
    }
    
    public void testSpockBlock_1() throws Exception {
        checkCompletion(BASE + "SpockBlocks01.groovy", "whe^re:", true);
    }
    
    public void testSpockBlock_2() throws Exception {
        checkCompletion(BASE + "SpockBlocks01.groovy", "the^n:", true);
    }
    
    public void testSpockBlock_3() throws Exception {
        checkCompletion(BASE + "SpockBlocks01.groovy", "and^:", true);
    }
    
    public void testSpockBlock_4() throws Exception {
        checkCompletion(BASE + "SpockBlocks01.groovy", "clea^nup:", true);
    }
    
    public void testSpockBlock_5() throws Exception {
        checkCompletion(BASE + "SpockBlocks01.groovy", "expe^ct:", true);
    }

    public void testSpockBlock_6() throws Exception {
        checkCompletion(BASE + "SpockBlocks01.groovy", "giv^en:", true);
    }
    
    public void testSpockBlock_7() throws Exception {
        checkCompletion(BASE + "SpockBlocks01.groovy", "setu^p:", true);
    }
    
    public void testSpockParam_1() throws Exception {
        checkCompletion(BASE + "SpockParam01.groovy", "def result = mathService.compute(ni^m)", true);
    }
    
    public void testSpockParam_2() throws Exception {
        checkCompletion(BASE + "SpockParam01.groovy", "1 * mathService.compute(_) >> { Math.pow(ni^m, 2) }", true);
    }
    
    public void testSpockParam_3() throws Exception {
        checkCompletion(BASE + "SpockParam01.groovy", "result == squ^are", true);
    }
    
    public void testSpockParam_4() throws Exception {
        checkCompletion(BASE + "SpockParam01.groovy", "ni^m || square", true);
    }
    
    public void testSpockParam_5() throws Exception {
        checkCompletion(BASE + "SpockParam01.groovy", "nim || squa^re", true);
    }
}
