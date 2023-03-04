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

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Pisl
 */
public class JadeLexerTest extends CslTestBase {
    
    public JadeLexerTest(String testName) {
        super(testName);
    }
    
    public void setUp() {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
    public void testPlainText01() throws Exception {
        checkLexer("testfiles/lexer/plainText01.jade");
    }

    public void testDoctype01() throws Exception {
       checkLexer("testfiles/lexer/doctype01.jade");
    }
        
    public void testExtends01() throws Exception {
        checkLexer("testfiles/lexer/extends01.jade");
    }
     
    public void testExtends02() throws Exception {
        checkLexer("testfiles/lexer/extends02.jade");
    }
    
    public void testFilters01() throws Exception {
        checkLexer("testfiles/lexer/filters01.jade");
    }
    
    public void testInclude01() throws Exception {
        checkLexer("testfiles/lexer/include01.jade");
    }
    
    public void testInclude02() throws Exception {
        checkLexer("testfiles/lexer/include02.jade");
    }
    
    public void testInclude03() throws Exception {
        checkLexer("testfiles/lexer/include03.jade");
    }
    
    public void testTag01() throws Exception {
        checkLexer("testfiles/lexer/tag01.jade");
    }
    
    public void testTag02() throws Exception {
        checkLexer("testfiles/lexer/tag02.jade");
        
    }
    
    public void testTag03() throws Exception {
        checkLexer("testfiles/lexer/tag03.jade");
    }
    
    public void testTag04() throws Exception {
        checkLexer("testfiles/lexer/tag04.jade");
    }
    
    public void testAttribute01() throws Exception {
        checkLexer("testfiles/lexer/attribute01.jade");
    }
     
    public void testAttribute02() throws Exception {
        checkLexer("testfiles/lexer/attribute02.jade");
    }
    
    public void testAttribute03() throws Exception {
        checkLexer("testfiles/lexer/attribute03.jade");
    }
    
    public void testAttribute04() throws Exception {
        checkLexer("testfiles/lexer/attribute04.jade");
    }
    
    public void testAttribute05() throws Exception {
        checkLexer("testfiles/lexer/attribute05.jade");
    }
    
    public void testAttribute06() throws Exception {
        checkLexer("testfiles/lexer/attribute06.jade");
    }
    
    public void testAttribute07() throws Exception {
        checkLexer("testfiles/lexer/attribute07.jade");
    }
    
    public void testAttribute08() throws Exception {
        checkLexer("testfiles/lexer/attribute08.jade");
    }
    
    public void testAttribute09() throws Exception {
        checkLexer("testfiles/lexer/attribute09.jade");
    }
    
    public void testAttribute10() throws Exception {
        checkLexer("testfiles/lexer/attribute10.jade");
    }
    
    public void testAttribute11() throws Exception {
        checkLexer("testfiles/lexer/attribute11.jade");
    }
    
    public void testAttribute12() throws Exception {
        checkLexer("testfiles/lexer/attribute12.jade");
    }
    
    public void testAttribute13() throws Exception {
        checkLexer("testfiles/lexer/attribute13.jade");
    }
    
    public void testAttribute14() throws Exception {
        checkLexer("testfiles/lexer/attribute14.jade");
    }
    
    public void testCase01() throws Exception {
        checkLexer("testfiles/lexer/case01.jade");
    }
    
    public void testCase02() throws Exception {
        checkLexer("testfiles/lexer/case02.jade");
    }
    
    public void testCase03() throws Exception {
        checkLexer("testfiles/lexer/case03.jade");
    }
    
    public void testCode01() throws Exception {
        checkLexer("testfiles/lexer/code01.jade");
    }
    
    public void testComment01() throws Exception {
        checkLexer("testfiles/lexer/comment01.jade");
    }
    
    public void testComment02() throws Exception {
        checkLexer("testfiles/lexer/comment02.jade");
    }
    
    public void testComment03() throws Exception {
        checkLexer("testfiles/lexer/comment03.jade");
    }
    
    public void testComment04() throws Exception {
        checkLexer("testfiles/lexer/comment04.jade");
    }
    
    public void testExpression01() throws Exception {
        checkLexer("testfiles/lexer/expression01.jade");
    }
    
    public void testConditional01() throws Exception {
        checkLexer("testfiles/lexer/conditional01.jade");
    }
    
    public void testConditional02() throws Exception {
        checkLexer("testfiles/lexer/conditional02.jade");
    }
    
    public void testConditional03() throws Exception {
        checkLexer("testfiles/lexer/conditional03.jade");
    }
    
    public void testInterpolation01() throws Exception {
        checkLexer("testfiles/lexer/interpolation01.jade");
    }
    
    public void testInterpolation02() throws Exception {
        checkLexer("testfiles/lexer/interpolation02.jade");
    }
    
    public void testInterpolation03() throws Exception {
        checkLexer("testfiles/lexer/interpolation03.jade");
    }
    
    public void testIteration01() throws Exception {
        checkLexer("testfiles/lexer/iteration01.jade");
    }
    
    public void testIteration02() throws Exception {
        checkLexer("testfiles/lexer/iteration02.jade");
    }
    
    public void testIteration03() throws Exception {
        checkLexer("testfiles/lexer/iteration03.jade");
    }
    
    public void testIteration04() throws Exception {
        checkLexer("testfiles/lexer/iteration04.jade");
    }
    
    public void testIteration05() throws Exception {
        checkLexer("testfiles/lexer/iteration05.jade");
    }
    
    public void testMixin01() throws Exception {
        checkLexer("testfiles/lexer/mixin01.jade");
    }
    
    public void testMixin02() throws Exception {
        checkLexer("testfiles/lexer/mixin02.jade");
    }
    
    public void testMixin03() throws Exception {
        checkLexer("testfiles/lexer/mixin03.jade");
    }
    
    public void testMixin04() throws Exception {
        checkLexer("testfiles/lexer/mixin04.jade");
    }
    
    public void testIssue250531() throws Exception {
        checkLexer("testfiles/lexer/issue250531.jade");
    }
    
    public void testIssue250547() throws Exception {
        checkLexer("testfiles/lexer/issue250547.jade");
    }
    
    public void testIssue250566() throws Exception {
        checkLexer("testfiles/lexer/issue250566.jade");
    }
    
    public void testIssue250564() throws Exception {
        checkLexer("testfiles/lexer/issue250564.jade");
    }
    
    public void testIssue250567_01() throws Exception {
        checkLexer("testfiles/lexer/issue250567_01.jade");
    }
    
    public void testIssue250567_02() throws Exception {
        checkLexer("testfiles/lexer/issue250567_02.jade");
    }
    
    public void testIssue250563() throws Exception {
        checkLexer("testfiles/lexer/issue250563.jade");
    }
    
    public void testIssue250543() throws Exception {
        checkLexer("testfiles/lexer/issue250543.jade");
    }
    
    public void testIssue250539() throws Exception {
        checkLexer("testfiles/lexer/issue250539.jade");
    }
    
    public void testIssue250519() throws Exception {
        checkLexer("testfiles/lexer/issue250519.jade");
    }
    
    public void testIssue250538() throws Exception {
        checkLexer("testfiles/lexer/issue250538.jade");
    }
    
    public void testIssue250523() throws Exception {
        checkLexer("testfiles/lexer/issue250523.jade");
    }
    
    public void testIssue250517() throws Exception {
        checkLexer("testfiles/lexer/issue250517.jade");
    }
    
    public void testIssue250838() throws Exception {
        checkLexer("testfiles/lexer/issue250838.jade");
    }
    
    public void testIssue250499() throws Exception {
        checkLexer("testfiles/lexer/issue250499.jade");
    }
    
    public void testIssue250860() throws Exception {
        checkLexer("testfiles/lexer/issue250860.jade");
    }
    
    public void testIssue250860_01() throws Exception {
        checkLexer("testfiles/lexer/issue250860_01.jade");
    }
    
    public void testIssue250495_01() throws Exception {
        checkLexer("testfiles/lexer/issue250495_01.jade");
    }
    
    public void testIssue250495_02() throws Exception {
        checkLexer("testfiles/lexer/issue250495_02.jade");
    }
    
    public void testIssue251146() throws Exception {
        checkLexer("testfiles/lexer/issue251146.jade");
    }
    
    public void testIssue251141() throws Exception {
        checkLexer("testfiles/lexer/issue251141.jade");
    }
    
    public void testIssue251150() throws Exception {
        checkLexer("testfiles/lexer/issue251150.jade");
    }
    
    public void testIssue251144() throws Exception {
        checkLexer("testfiles/lexer/issue251144.jade");
    }
    
    public void testIssue251140() throws Exception {
        checkLexer("testfiles/lexer/issue251140.jade");
    }
    
    public void testIssue250516() throws Exception {
        checkLexer("testfiles/lexer/issue250516.jade");
    }
    
    public void testIssue250537() throws Exception {
        checkLexer("testfiles/lexer/issue250537.jade");
    }
    
    public void testIssue250542() throws Exception {
        checkLexer("testfiles/lexer/issue250542.jade");
    }
    
    public void testIssue251209() throws Exception {
        checkLexer("testfiles/lexer/issue251209.jade");
    }
    
    public void testIssue251837() throws Exception {
        checkLexer("testfiles/lexer/issue251837.jade");
    }
    
    public void testIssue251847() throws Exception {
        checkLexer("testfiles/lexer/issue251847.jade");
    }
    
    public void testIssue258274_01() throws Exception {
        checkLexer("testfiles/lexer/issue258274_01.jade");
    }
    
    public void testIssue258274_02() throws Exception {
        checkLexer("testfiles/lexer/issue258274_02.jade");
    }
    
    public void testIssue258274_03() throws Exception {
        checkLexer("testfiles/lexer/issue258274_03.jade");
    }
    
    private void checkLexer(final String filePath) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));
        Snapshot snapshot = testSource.createSnapshot();
        CharSequence text = snapshot.getText();
        StringBuilder sb = new StringBuilder();
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<? extends JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());
        assertNotNull("Can not obtain token sequence for file: " + filePath, ts);
        while (ts.moveNext()) {
            sb.append("[").append(ts.token().id()).append("(").append(ts.token().text().length()).append("):");
            if (ts.token().id() != JadeTokenId.EOL){
                sb.append(ts.token().text().toString()).append("]");
            } else {
                sb.append("\\n]").append("\n");
            }
            
        }

        assertDescriptionMatches(filePath, sb.toString(), false, ".lexer");
    }
}
