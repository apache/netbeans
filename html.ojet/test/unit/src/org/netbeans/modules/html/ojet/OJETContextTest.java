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

package org.netbeans.modules.html.ojet;

import java.util.Collections;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Petr Pisl
 */
public class OJETContextTest extends JsTestBase{

    public OJETContextTest(String name) {
        super(name);
    }
    
    public void testContext01()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"checkbox\" data-bind=\"ojComponent: {^}\"/> ", OJETContext.COMP_CONF);
    }
    
    public void testContext02()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"^\"/>", OJETContext.DATA_BINDING);
    }
    
    public void testContext03()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\" ^\"/><!-- test 03  -->", OJETContext.DATA_BINDING);
    }
    
    public void testContext04()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"ojComponent: {component: 'ojButton', ^}\"/><!-- test 04  -->", OJETContext.COMP_CONF_PROP_NAME);
    }
    
    public void testContext05()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"ojComponent: {component: ^}\"/><!-- test 05  -->", OJETContext.COMP_CONF_COMP_NAME);
    }
    
    public void testContext06()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"ojComponent: {component: '^'}\"/><!-- test 06  -->", OJETContext.COMP_CONF_COMP_NAME);
    }
    
    public void testContext07()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"ojComponent: {component: 'oj^'}\"/><!-- test 07  -->", OJETContext.COMP_CONF_COMP_NAME);
    }
    
    public void testContext08()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"ojComponent: {component: 'oj^}\"/><!-- test 08  -->", OJETContext.COMP_CONF_COMP_NAME);
    }
    
    public void testContext09()  throws Exception {
       checkContext("testfiles/context/example01.html", "<button id= \"cancel-button\" data-bind=\"click: toogleCommitPanel, ^\"><!-- test 09  -->", OJETContext.DATA_BINDING);
    }
    
    public void testContext10()  throws Exception {
       checkContext("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"ojComponent: {component: 'ojButton', lab^el : ''}\"/><!-- test 10  -->", OJETContext.COMP_CONF_PROP_NAME);
    }
    
    
    public void testComponentName01()  throws Exception {
       checkComponentName("testfiles/context/example01.html", "<input type=\"button\" data-bind=\"ojComponent: {component: 'ojButton', ^}\"/><!-- test 04  -->", "ojButton");
    }
    
    private void checkContext(final String file, final String caretLine, final OJETContext expected) throws Exception {
        
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
                Document document = info.getSnapshot().getSource().getDocument(true);
                ((AbstractDocument)document).readLock();
                OJETContext result = OJETContext.UNKNOWN;
                try {
                     result = OJETContext.findContext(info.getSnapshot().getSource().getDocument(false), caretOffset);
                } finally {
                    ((AbstractDocument)document).readUnlock();
                }
                assertEquals(expected, result);
            }
        });
    }
    
    private void checkComponentName(final String file, final String caretLine, final String expectedName) throws Exception {
        
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
                Document document = info.getSnapshot().getSource().getDocument(true);
                ((AbstractDocument)document).readLock();
                String name = null;
                try {
                     name = OJETContext.findComponentName(info.getSnapshot().getSource().getDocument(false), caretOffset);
                } finally {
                    ((AbstractDocument)document).readUnlock();
                }
                assertEquals(expectedName, name);
            }
        });
    }
    
    
}
