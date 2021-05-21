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
package org.netbeans.modules.javascript2.editor.hint;

import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.javascript2.editor.hints.GlobalIsNotDefined;

/**
 *
 * @author Petr Pisl
 */
public class JsGlobalIsNotDeclaredTest extends HintTestBase {

    public JsGlobalIsNotDeclaredTest(String testName) {
        super(testName);
    }
    
    private static class GlobalIsNotDefinedHint extends GlobalIsNotDefined {
        @Override
        public HintSeverity getDefaultSeverity() {
            return HintSeverity.WARNING;
        }
        
    }
    
    private Rule createRule() {
        GlobalIsNotDefined gind = new GlobalIsNotDefinedHint();
        return gind;
    }
    
    public void testSimple01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/globalIsNotDeclared.js", null);
    }
    
    public void testIssue224040() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224040.js", null);
    }
    
    public void testIssue224041() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224041.js", null);
    }
    
    public void testIssue224035() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224035.js", null);
    }
    
    public void testIssue225048() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue225048.js", null);
    }
    
    public void testIssue225048_01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue225048_01.js", null);
    }
    
    public void testIssue250372() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue250372.js", null);
    }
    
    public void testIssue248696_01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue248696_01.js", null);
    }
    
    public void testIssue248696_02() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue248696_02.js", null);
    }
    
    public void testIssue252022() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue252022.js", null);
    }

    public void testIssue249487() throws Exception {
        checkHints(this, createRule(), "testfiles/markoccurences/issue249487.js", null);
    }
    
    public void testIssue255494() throws Exception {
        checkHints(this, createRule(), "testfiles/coloring/issue255494.js", null);
    }
    
    public void testIssue268384() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue268384.js", null);
    }
    
}
