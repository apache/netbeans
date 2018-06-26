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
package org.netbeans.modules.javascript2.editor.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Future;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.javascript2.editor.JsTestBase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsCommentGeneratorTest extends JsTestBase {

    public JsCommentGeneratorTest(String testName) {
        super(testName);
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    public void testClass1() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testClass2() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testClass3() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testGlobal1() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testGlobal2() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testGlobal3() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testGlobal4() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testProperty1() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testProperty2() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testProperty3() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testProperty4() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }
    
    public void testObject1() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testIssue218945() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testIssue218411_1() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testIssue218411_2() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }

    public void testIssue230610() throws Exception {
        insertBreak(getOriginalContent(), getExpectedContent());
    }
    
    public void testIssue231420() throws Exception {
        insertBreak(getOriginalContent("js"), getExpectedContent("js"));
    }

    public void testIssue238683() throws Exception {
        insertBreak(getOriginalContent("js"), getExpectedContent("js"));
    }

    @Override
    public void insertNewline(String source, String reformatted, IndentPrefs preferences) throws Exception {
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos + 1);
        Formatter formatter = getFormatter(null);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos + 1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        // wait for generating comment
        Future<?> future = JsDocumentationCompleter.RP.submit(new Runnable() {
            @Override
            public void run() {
            }
        });
        future.get();

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    private String getTestFolderPath() {
        return "testfiles/doc/commentGenerator/"; //NOI18N
    }

    private String getOriginalContent() throws IOException {
        return getOriginalContent("js");
    }

    private String getOriginalContent(String ext) throws IOException {
        File f = new File(getDataDir(), getTestPath(false, ext));
        return readFileAsString(f);
    }

    private String getExpectedContent() throws IOException {
        return getExpectedContent("js");
    }

    private String getExpectedContent(String ext) throws IOException {
        File f = new File(getDataDir(), getTestPath(true, ext));
        return readFileAsString(f);
    }

    private static String readFileAsString(File file) throws java.io.IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    private String getTestPath(boolean expected, String ext) {
        return getTestFolderPath() + getTestName(expected) + "." + ext;//NOI18N
    }

    private String getTestName(boolean expected) {
        String name = getName();
        if (expected) {
            return name + "-expected";
        } else {
            return name;
        }
    }
}
