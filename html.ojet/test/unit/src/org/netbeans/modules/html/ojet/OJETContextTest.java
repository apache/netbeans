/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
