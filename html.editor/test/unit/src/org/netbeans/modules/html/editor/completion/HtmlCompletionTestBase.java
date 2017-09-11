/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport.Match;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public abstract class HtmlCompletionTestBase extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/completion/";

    public HtmlCompletionTestBase(String name) {
        super(name);
    }

    protected abstract HtmlVersion getExpectedVersion();

    protected String getPublicID() {
        return null;
    }
    
    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type) throws BadLocationException, ParseException {
        assertItems(documentText, expectedItemsNames, type, -1);
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        HtmlCompletionTestSupport.assertItems(getDocument(documentText), expectedItemsNames, type, expectedAnchor);
    }

    protected void assertItems(Document doc, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        HtmlCompletionTestSupport.assertItems(doc, expectedItemsNames, type, expectedAnchor);
    }
    
    protected void assertCompletedText(String documentText, String itemToCompleteName, String expectedText) throws BadLocationException, ParseException {
        HtmlCompletionTestSupport.assertCompletedText(getDocument(""), documentText, itemToCompleteName, expectedText);
    }

    protected String[] arr(String... args) {
        return args;
    }

    protected TestSource getTestSource(String testFilePath, boolean removePipe) throws BadLocationException {
        FileObject source = getTestFile(DATA_DIR_BASE + testFilePath);
        BaseDocument doc = getDocument(source);
        StringBuilder sb = new StringBuilder(doc.getText(0, doc.getLength()));
        int pipeIndex = sb.indexOf("|");
        assertTrue(String.format("Errorneous test file %s, there is no pipe char specifying the completion offset!", testFilePath),
                pipeIndex != -1);

        if(removePipe) {
            sb.deleteCharAt(pipeIndex);
        }

        return new TestSource(sb.toString(), pipeIndex);
    }

    protected static class TestSource {
        private String code;
        private int position;

        private TestSource(String code, int position) {
            this.code = code;
            this.position = position;
        }

        public String getCode() {
            return code;
        }

        public int getPosition() {
            return position;
        }
        
    }

}
