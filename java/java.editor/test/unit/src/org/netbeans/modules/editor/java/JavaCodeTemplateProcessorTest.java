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
package org.netbeans.modules.editor.java;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.junit.Test;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.codetemplates.CodeTemplateInsertHandler;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl.OnExpandAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class JavaCodeTemplateProcessorTest extends NbTestCase {

    public JavaCodeTemplateProcessorTest(String name) {
        super(name);
    }

    @Test
    public void testComplexTemplate() throws Exception {
        doTestTemplateInsert("for (${TYPE iterableElementType default=\"Object\" editable=false} ${ELEM newVarName default=\"elem\" ordering=2} : ${ITER iterable default=\"col\" ordering=1}) {\n" +
                             "${selection}${cursor}\n" +
                             "}\n",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        |" +
                             "    }" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        for (String arg : args|) {\n" +
                             "            \n" +
                             "        }\n" +
                             "    }" +
                             "}");
    }

    private void doTestTemplateInsert(String template, String code, String expected) throws Exception {
        clearWorkDir();
        FileObject testFile = FileUtil.toFileObject(getWorkDir()).createData("Test.java");
        EditorKit kit = new JavaKit();
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(kit);
        Document doc = pane.getDocument();
        doc.putProperty(Document.StreamDescriptionProperty, testFile);
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        int caretOffset = code.indexOf('|');
        assertTrue(caretOffset != (-1));
        String text = code.substring(0, caretOffset) + code.substring(caretOffset + 1);
        pane.setText(text);
        pane.setCaretPosition(caretOffset);
        try (OutputStream out = testFile.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.append(text);
        }
        CodeTemplateManager manager = CodeTemplateManager.get(doc);
        CodeTemplate ct = manager.createTemporary(template);
        CodeTemplateInsertHandler handler = new CodeTemplateInsertHandler(ct, pane, Arrays.asList(new JavaCodeTemplateProcessor.Factory()), OnExpandAction.INDENT);
        handler.processTemplate();
        int resultCaretOffset = expected.indexOf('|');
        assertTrue(resultCaretOffset != (-1));
        String expectedText = expected.substring(0, resultCaretOffset) + expected.substring(resultCaretOffset + 1);
        assertEquals(expectedText, doc.getText(0, doc.getLength()));
        assertEquals(resultCaretOffset, pane.getCaretPosition());
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

}
