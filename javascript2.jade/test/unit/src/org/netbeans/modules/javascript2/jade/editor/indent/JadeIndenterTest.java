/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor.indent;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import static junit.framework.Assert.assertNotNull;
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
