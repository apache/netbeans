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

package org.netbeans.modules.groovy.editor.api;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Marek Slama
 */
public class GroovyBracesMatcherTest extends GroovyTestBase {
    
    public GroovyBracesMatcherTest(String testName) {
        super(testName);
    }
    
    /**
     * Test for BracesMatcher, first ^ gives current caret position,
     * second ^ gives matching caret position. Test is done in forward and backward direction.
     */
    private void match2(String original) throws BadLocationException {
        super.assertMatches2(original);
    }
    
    public void testFindMatching1() throws Exception {
        match2("if (true) ^{\n^}");
    }
    
    public void testFindMatching2() throws Exception {
        match2("x=^(true^)\ny=5");
    }
    
    public void testFindMatching3() throws Exception {
        match2("x=^(true || (false)^)\ny=5");
    }
    
    public void testFindMatching4() throws Exception {
        match2("function foo() ^{\nif (true) {\n}\n^}\n}");
    }
    
}
