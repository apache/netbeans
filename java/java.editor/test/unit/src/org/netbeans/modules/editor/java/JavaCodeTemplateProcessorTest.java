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
package org.netbeans.modules.editor.java;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
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

    private FileObject testFile;
    
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
        testFile = FileUtil.toFileObject(getWorkDir()).createData("Test.java");
        EditorKit kit = new JavaKit();
        JEditorPane pane = new JEditorPane();
        SwingUtilities.invokeAndWait(() -> {
            pane.setEditorKit(kit);
        });
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
    
    @Test
    public void testShouldAddStaticImportForTemplateParameterWithStaticImportHint() throws Exception {
        doTestTemplateInsert("int max = ${param staticImport=\"java.lang.Math.max\" editable=false}(0, 1);",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        |\n" +
                             "    }\n" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        int max = max(0, 1);|\n" +
                             "    }\n" +
                             "}");
        assertFileObjectTextMatchesRegex("(?s)\\s*?import static java\\.lang\\.Math\\.max;\\s*?public class Test.*?");
    }
    
    @Test
    public void testShouldNotAddDuplicatesOfStaticImportForTemplateParameterWithStaticImportHint() throws Exception {
        doTestTemplateInsert("int max = ${param staticImport=\"java.lang.Math.max\" editable=false}(0, 1);",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        |\n" +
                             "    }\n" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        int max = max(0, 1);|\n" +
                             "    }\n" +
                             "}");
        doTestTemplateInsert("int max = ${param staticImport=\"java.lang.Math.max\" editable=false}(0, 1);",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        int max = max(0, 1);\n" +
                             "        |\n"+
                             "    }\n" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        int max = max(0, 1);\n" +
                             "        int max = max(0, 1);|\n" +
                             "    }\n" +
                             "}");
        assertFileObjectTextMatchesRegex("(?s)\\s*?import static java\\.lang\\.Math\\.max;\\s*?public class Test.*?");
    }
    
    @Test
    public void testWhenOnlyIdentifierWithoutTypeIsSpecifiedThenDoNotAddStaticImport() throws Exception {
        doTestTemplateInsert("int max = ${param staticImport=\"max\" editable=false}(0, 1);",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        |\n" +
                             "    }\n" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        int max = max(0, 1);|\n" +
                             "    }\n" +
                             "}");
        assertFileObjectTextMatchesRegex("(?s)\\s*?public class Test.*?");
    }

    public void testCodeTemplatesShouldWorkInsideParenthesesOfForEachLoop() throws Exception {
         doTestTemplateInsert("${name newVarName}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        for (String |) {\n" +
                             "        }\n" +
                             "    }\n" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        for (String name|) {\n" +
                             "        }\n" +
                             "    }\n" +
                             "}");
         doTestTemplateInsert("${names iterable}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        for (String name: |) {\n" +
                             "        }\n" +
                             "    }\n" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        for (String name: args|) {\n" +
                             "        }\n" +
                             "    }\n" +
                             "}");
    }

    public void testCodeTemplatesShouldWorkInsideParenthesesOfWhileLoop() throws Exception {
         doTestTemplateInsert("${list instanceof=\"java.util.List\"}.isEmpty()",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        while (|) {\n" +
                             "        }\n" +
                             "    }\n" +
                             "}",
                             "public class Test {\n" +
                             "    private void t(String... args) {\n" +
                             "        while (list|.isEmpty()) {\n" +
                             "        }\n" +
                             "    }\n" +
                             "}");
    }

    public void testInfiteLoop() throws Exception {
         doTestTemplateInsert("for (${IT_TYPE rightSideType type=\"java.util.Iterator\" default=\"Iterator\" editable=false} ${IT newVarName default=\"it\"} = ${COL instanceof=\"java.util.Collection\" default=\"col\"}.iterator(); ${IT}.hasNext();) {\n" +
                              "    ${TYPE rightSideType default=\"Object\"} ${ELEM newVarName default=\"elem\"} = ${TYPE_CAST cast default=\"\" editable=false}${IT}.next();\n" +
                              "    ${selection}${cursor}\n" +
                              "}\n",
                             "import java.util.Collection;\n" +
                             "public class Test<E> {\n" +
                             "    private void t(Collection<? extends E> c) {\n" +
                             "        |\n" +
                             "    }\n" +
                             "}",
                             "import java.util.Collection;\n" +
                             "public class Test<E> {\n" +
                             "    private void t(Collection<? extends E> c) {\n" +
                             "        for (Iterator<? extends E> iterator| = c.iterator(); iterator.hasNext();) {\n" +
                             "            E next = iterator.next();\n" +
                             "            \n" +
                             "        }\n" +
                             "    }\n" +
                             "}");
    }

    private void assertFileObjectTextMatchesRegex(String regex) throws IOException {
        String text = testFile.asText();
        assertTrue("The file text must match the regular expression", text.matches(regex));
    }

}
