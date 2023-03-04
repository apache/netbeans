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
package org.netbeans.modules.javascript2.editor;

import java.util.Collections;
import static org.junit.Assert.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Petr Pisl
 */
public class CompletionContextFinderTest extends JsTestBase {

    public CompletionContextFinderTest(String testName) {
        super(testName);
    }

    public void testSimpleObject01()  throws Exception {
       checkCompletionContext("testfiles/model/simpleObject.js", "formatter.prin^tln(Carrot.getColor());", CompletionContext.OBJECT_PROPERTY);
    }
    
    public void testSimpleObject02()  throws Exception {
       checkCompletionContext("testfiles/model/simpleObject.js", "this.called = this.^called + 1;", CompletionContext.OBJECT_MEMBERS);
    }
    
    
    public void testGlobal01()  throws Exception {
       checkCompletionContext("testfiles/model/simpleObject.js", "if (this.color === \"red\") {^", CompletionContext.GLOBAL);
    }
    
    public void testGlobal02()  throws Exception {
       checkCompletionContext("testfiles/model/simpleObject.js", " ^   },", CompletionContext.GLOBAL);
    }
    
    public void testGlobal03()  throws Exception {
       checkCompletionContext("testfiles/completion/issue217100_3.js", "v^", CompletionContext.GLOBAL);
    }
    
    public void testObjectPropertyName01() throws Exception {
        checkCompletionContext("testfiles/completion/objectPropertyNameContext.js", "    autoCr^ea: \"sranda\"", CompletionContext.OBJECT_PROPERTY_NAME);
    }

    public void testObjectPropertyName02() throws Exception {
        checkCompletionContext("testfiles/completion/objectPropertyNameContext.js", "   nam^e: 'Panda',", CompletionContext.OBJECT_PROPERTY_NAME);
    }
    
    public void testObjectPropertyName03() throws Exception {
        checkCompletionContext("testfiles/completion/objectPropertyNameContext.js", "    ^context: document.body,", CompletionContext.OBJECT_PROPERTY_NAME);
    }
    
    public void testObjectPropertyName04() throws Exception {
        checkCompletionContext("testfiles/completion/objectPropertyNameContext.js", "    ^crossDomain: false,", CompletionContext.OBJECT_PROPERTY_NAME);
    }
    
    public void testObjectPropertyName05() throws Exception {
        checkCompletionContext("testfiles/completion/objectPropertyNameContext.js", "    ^complete: function(jqXHR, textStatus) {", CompletionContext.OBJECT_PROPERTY_NAME);
    }
    
    public void testObjectPropertyName06() throws Exception {
        checkCompletionContext("testfiles/completion/objectPropertyNameContext.js", "    complete: functi^on(jqXHR, textStatus) {", CompletionContext.EXPRESSION);
    }
    
    public void testObjectPropertyName07() throws Exception {
        checkCompletionContext("testfiles/completion/objectPropertyNameContext.js", "       ^ // try here", CompletionContext.GLOBAL);
    }
    
    public void testString01() throws Exception {
        checkCompletionContext("testfiles/completion/extDefine.js", "    extend: '^',", CompletionContext.IN_STRING);
    }
    
    public void testString02() throws Exception {
        checkCompletionContext("testfiles/completion/extDefine.js", "    extend2: 'Ext.panel.^',", CompletionContext.IN_STRING);
    }

    public void testIssue244803() throws Exception {
        checkCompletionContext("testfiles/completion/issue244803.js", "foo: encode^,", CompletionContext.EXPRESSION);
    }
    
    public void testIssue246020_01() throws Exception {
        checkCompletionContext("testfiles/completion/issue246020.js", "for(var i=0, max = ^i )", CompletionContext.EXPRESSION);
    }
    
    public void testIssue246020_02() throws Exception {
        checkCompletionContext("testfiles/completion/issue246020.js", "},^ ", CompletionContext.OBJECT_PROPERTY_NAME);
    }
    
    public void testIssue246020_03() throws Exception {
        checkCompletionContext("testfiles/completion/issue246020.js", "for(var i=0, max = i )^", CompletionContext.EXPRESSION);
    }
    
    public void testIssue249264_01() throws Exception {
        checkCompletionContext("testfiles/completion/issue249264.js", "document.getElementById('mainCon^tainer');", CompletionContext.STRING_ELEMENTS_BY_ID);
    }
    
    public void testIssue249264_02() throws Exception {
        checkCompletionContext("testfiles/completion/issue249264.js", "document.getElementById('^');", CompletionContext.STRING_ELEMENTS_BY_ID);
    }
    
    public void testIssue249264_03() throws Exception {
        checkCompletionContext("testfiles/completion/issue249264.js", "document.getElementsByClassName('demo-apphe^ader');", CompletionContext.STRING_ELEMENTS_BY_CLASS_NAME);
    }
    
    public void testIssue249264_04() throws Exception {
        checkCompletionContext("testfiles/completion/issue249264.js", "document.getElementsByClassName('^');", CompletionContext.STRING_ELEMENTS_BY_CLASS_NAME);
    }
    
    public void testCallArgumentContext_01() throws Exception {
        checkCompletionContext("testfiles/completion/simpleCallArgumentContext.js", "r.send(^);", CompletionContext.CALL_ARGUMENT);
    }
    
    public void testCallArgumentContext_02() throws Exception {
        checkCompletionContext("testfiles/completion/simpleCallArgumentContext.js", "r.send({}, ^);", CompletionContext.CALL_ARGUMENT);
    }
    
    public void testIssue250369() throws Exception {
        checkCompletionContext("testfiles/completion/issue250369.js", "* @^", CompletionContext.DOCUMENTATION);
    }
    
    private void checkCompletionContext(final String file, final String caretLine, final CompletionContext expected) throws Exception {
        
        Source testSource = getTestSource(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult info = (ParserResult) r;

                CompletionContext result = CompletionContextFinder.findCompletionContext(info, caretOffset);
                assertEquals(expected, result);
            }
        });
    }
}
