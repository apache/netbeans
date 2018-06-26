/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
