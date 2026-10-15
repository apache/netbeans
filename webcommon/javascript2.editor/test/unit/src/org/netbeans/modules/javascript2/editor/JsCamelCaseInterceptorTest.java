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
package org.netbeans.modules.javascript2.editor;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Pisl
 */
public class JsCamelCaseInterceptorTest extends CslTestBase {

    public JsCamelCaseInterceptorTest(String testName) {
        super(testName);
    }
    
    public void testIssue248478_01() throws Exception {
        checkNextWordOffset("var fo^oBarBaz = '';", 7, false);
    }
    
    public void testIssue248478_02() throws Exception {
        checkNextWordOffset("var fooB^arBaz = '';", 10, false);
    }
    
    public void testIssue248478_03() throws Exception {
        checkNextWordOffset("var fooBar^Baz = '';", 13, false);
    }
    
    public void testIssue248478_04() throws Exception {
        checkNextWordOffset("var fooBarB^az = '';", 13, false);
    }
    
    public void testIssue248478_05() throws Exception {
        checkNextWordOffset("var fooBarBaz^ = '';", -1, false);
    }
    
    public void checkNextWordOffset(String text, int expected, boolean reverse) {
        int index = text.indexOf('^');
        if (index == -1) {
            assertFalse("The input text doesn't contain caret position marked via '^'", index == -1);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(text.substring(0, index));
        sb.append(text.substring(index + 1));
        BaseDocument document = getDocument(sb.toString(), JsTokenId.JAVASCRIPT_MIME_TYPE);
        document.readLock();
        try {
            int newOffset = JsCamelCaseInterceptor.getWordOffset(document, index, reverse);
            assertEquals(expected, newOffset);
        } finally {
            document.readUnlock();
        }
    } 
}
