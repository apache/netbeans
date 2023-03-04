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

package org.netbeans.modules.javascript2.nodejs.editor;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Pisl
 */
public class NodeJsContextTest extends JsTestBase {

    public NodeJsContextTest(String testName) {
        super(testName);
    }
    
    public void testContext01() throws Exception {
        checkCompletionContext("testfiles/context/context01.js");
    }
    
    public void testContext02() throws Exception {
        checkCompletionContext("testfiles/context/context02.js");
    }
    
    public void testSimpleServer() throws Exception {
        checkCompletionContext("testfiles/context/simpleServer.js");
    }
    
    public void testOnEvents() throws Exception {
        checkCompletionContext("testfiles/context/eventer.js");
    }
    
    public void testIssue248135() throws Exception {
        checkCompletionContext("testfiles/context/issue248135.js");
    }
    
    public void testIssue248135_01() throws Exception {
        checkCompletionContext("testfiles/context/issue248135A.js");
    }
    
    private void checkCompletionContext(final String filePath) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));
        Snapshot snapshot = testSource.createSnapshot();
        CharSequence text = snapshot.getText();
        StringBuilder sb = new StringBuilder();
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, 0);
        NodeJsContext contextPrevious = null;
        for (int offset = 0; offset < text.length(); offset++) {
            NodeJsContext context = NodeJsContext.findContext(ts , offset);
            if (!context.equals(contextPrevious)) {
                sb.append('[').append(context).append(']');
                contextPrevious = context;
            }
            sb.append(text.charAt(offset));
        }
        assertDescriptionMatches(filePath, sb.toString(), false, ".context");
    }
}
