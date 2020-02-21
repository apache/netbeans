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
