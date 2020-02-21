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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makefile.editor;

import org.netbeans.modules.cnd.makefile.editor.MakefileIndentTaskFactory;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.indent.api.Indent;

/**
 */
public class MakefileIndentTest extends NbTestCase {

    private static final String MIME_TYPE = MIMENames.MAKEFILE_MIME_TYPE;
    private MimePath mimePath;

    public MakefileIndentTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MakefileIndentTaskFactory factory = new MakefileIndentTaskFactory();
        MockServices.setServices(MockMimeLookup.class);
        mimePath = MimePath.parse(MIME_TYPE);
        MockMimeLookup.setInstances(mimePath, factory);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testRuleIndent1() throws BadLocationException {
        createDocument("build: myapp|");
        indentNewLine();
        assertText("build: myapp\n\t");
    }

    public void testRuleIndent2() throws BadLocationException {
        createDocument("build: myapp|\nclean:\n");
        indentNewLine();
        assertText("build: myapp\n\t\nclean:\n");
    }

    public void testRuleIndent3() throws BadLocationException {
        createDocument("build: myapp| #comment");
        indentNewLine();
        assertText("build: myapp\n\t#comment");
    }

    public void testRuleIndent4() throws BadLocationException {
        createDocument("build:\n\tcc -o a.o a.c|cc -o b.o b.c\n");
        indentNewLine();
        createDocument("build:\n\tcc -o a.o a.c\n\tcc -o b.o b.c\n");
    }

    public void testCommentIndent() throws BadLocationException {
        createDocument("#build: myapp|");
        indentNewLine();
        assertText("#build: myapp\n");
    }


    private BaseDocument doc;
    private int caretOffset = -1;

    private void createDocument(String text) throws BadLocationException {
        doc = new NbEditorDocument(MIME_TYPE);
        setText(text);
    }

    private void setText(String text) throws BadLocationException {
        doc.remove(0, doc.getLength());
        caretOffset = text.indexOf('|');
        if (0 <= caretOffset) {
            text = text.substring(0, caretOffset) + text.substring(caretOffset + 1);
        }
        doc.insertString(0, text, null);
    }

    private void indentNewLine() throws BadLocationException {
        doc.insertString(caretOffset, "\n", null);
        Indent indent = Indent.get(doc);
        indent.lock();
        try {
            indent.reindent(caretOffset + 1);
        } finally {
            indent.unlock();
        }
    }

    private void assertText(String text) throws BadLocationException {
        assertEquals(text, doc.getText(0, doc.getLength()));
    }

}
