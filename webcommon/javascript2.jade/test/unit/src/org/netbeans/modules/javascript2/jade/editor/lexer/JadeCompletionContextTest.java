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
package org.netbeans.modules.javascript2.jade.editor.lexer;

import java.util.Collections;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.jade.editor.JadeCompletionContext;
import org.netbeans.modules.javascript2.jade.editor.JadeTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Petr Pisl
 */
public class JadeCompletionContextTest extends JadeTestBase {

    public JadeCompletionContextTest(String testName) {
        super(testName);
    }
    
    public void testContext01() throws Exception {
        checkCompletionContext("testfiles/lexer/tag01.jade");
    }
    
    public void testAttribute03() throws Exception {
        checkCompletionContext("testfiles/lexer/attribute03.jade");
    }
    
    public void testTag01() throws Exception {
        checkCompletionContext("testfiles/ccContext/tag01.jade");
    }
    
    public void testTag02() throws Exception {
        checkCompletionContext("testfiles/ccContext/tag02.jade");
    }
    
    public void testTag03() throws Exception {
        checkCompletionContext("testfiles/ccContext/tag03.jade");
    }
    
    public void testIssue250743() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250743.jade");
    }
    
    public void testIssue250742() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250742.jade");
    }
    
    public void testIssue250741() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250741.jade");
    }
    
    public void testIssue250739() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250739.jade");
    }
    
    public void testIssue250738() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250738.jade");
    }
    
    public void testIssue250734() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250734.jade");
    }
    
    public void testIssue250732() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250732.jade");
    }
    
    public void testIssue250731() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250731.jade");
    }
    
    public void testIssue250736() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250736.jade");
    }
    
    public void testIssue250735() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue250735.jade");
    }
    
    public void testIssue251132() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251132.jade");
    }
    
    public void testIssue251152() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251152.jade");
    }
    
    public void testIssue251160() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251160.jade");
    }
    
    public void testIssue251278() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251278.jade");
    }
    
    public void testIssue251281() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251281.jade");
    }
    
    public void testIssue251157() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251157.jade");
    }
    
    public void testIssue251153() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue251153.jade");
    }
    
    public void testIssue254618() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue254618.jade");
    }
    
    public void testIssue254617() throws Exception {
        checkCompletionContext("testfiles/ccContext/issue254617.jade");
    }
    
    private void checkCompletionContext(final String filePath) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));
        final Snapshot snapshot = testSource.createSnapshot();
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult info = (ParserResult) r;

                CharSequence text = snapshot.getText();
                StringBuilder sb = new StringBuilder();
        
                JadeCompletionContext contextPrevious = null;
                for (int offset = 0; offset < text.length(); offset++) {
                    JadeCompletionContext context = JadeCompletionContext.findCompletionContext(info , offset);
                    if (!context.equals(contextPrevious)) {
                        sb.append('[').append(context).append(']');
                        contextPrevious = context;
                    }
                    sb.append(text.charAt(offset));
                }
                assertDescriptionMatches(filePath, sb.toString(), false, ".context");
            }
        });
        
    }
}
