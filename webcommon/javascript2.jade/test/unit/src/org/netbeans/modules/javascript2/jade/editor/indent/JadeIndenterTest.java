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
package org.netbeans.modules.javascript2.jade.editor.indent;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript2.jade.editor.JadeLanguage;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Roman Svitanic
 */
public class JadeIndenterTest extends CslTestBase {

    public JadeIndenterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            TestLanguageProvider.register(JadeTokenId.jadeLanguage());
        } catch (IllegalStateException ise) {
            // Ignore -- it's already registered
        }
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JadeLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return JadeTokenId.JADE_MIME_TYPE;
    }

    public void testSimpleIndent01() throws Exception {
        testIndentInFile("testfiles/indent/simple01.jade");
    }

    public void testComment01() throws Exception {
        testIndentInFile("testfiles/indent/comment01.jade");
    }

    public void testComment02() throws Exception {
        testIndentInFile("testfiles/indent/comment02.jade");
    }

    public void testCommentUnbuffered01() throws Exception {
        testIndentInFile("testfiles/indent/commentUnbuffered01.jade");
    }

    public void testIssue250699() throws Exception {
        testIndentInFile("testfiles/indent/issue250699.jade");
    }

    public void testIssue250737() throws Exception {
        testIndentInFile("testfiles/indent/issue250737.jade");
    }

    private void testIndentInFile(String file) throws Exception {
        testIndentInFile(file, null, 0);
    }

    private void testIndentInFile(String file, IndentPrefs indentPrefs, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos + 1);
        Formatter formatter = getFormatter(indentPrefs);

        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, indentPrefs);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".indented");
    }

}
