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
package org.netbeans.modules.javascript2.jsdoc;

import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import org.netbeans.modules.javascript2.editor.JsTestBase;

/**
 *
 * @author Petr Pisl
 */
public class TypeDefIssue249068 extends JsCodeCompletionBase {

    public TypeDefIssue249068(String testName) {
        super(testName);
    }
    
    public void testSemantic() throws Exception {
        checkSemantic("testfiles/jsdoc/issue249068.js");
    }
    
    public void testStructure() throws Exception {
        checkStructure("testfiles/jsdoc/issue249068.js");
    }
    
    
}
