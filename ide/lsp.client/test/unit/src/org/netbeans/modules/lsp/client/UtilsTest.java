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
package org.netbeans.modules.lsp.client;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.DeleteFile;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

public class UtilsTest extends NbTestCase {

    public UtilsTest(String name) {
        super(name);
    }

    public void testApplyTextEdit() throws Exception {
        clearWorkDir();
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject sourceFile1 = wd.createData("Test1.txt");
        try (OutputStream out = sourceFile1.getOutputStream()) {
            out.write(("0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n").getBytes("UTF-8"));
        }
        FileObject sourceFile2 = wd.createData("Test2.txt");
        try (OutputStream out = sourceFile2.getOutputStream()) {
            out.write(("0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n").getBytes("UTF-8"));
        }
        Map<String, List<TextEdit>> changes = new HashMap<>();
        changes.put(Utils.toURI(sourceFile1), Arrays.asList(new TextEdit(new Range(new Position(2, 3), new Position(2, 6)), "a"),
                                                            new TextEdit(new Range(new Position(1, 2), new Position(1, 6)), "b"),
                                                            new TextEdit(new Range(new Position(3, 1), new Position(4, 4)), "c")));
        changes.put(Utils.toURI(sourceFile2), Arrays.asList(new TextEdit(new Range(new Position(2, 3), new Position(2, 6)), "a"),
                                                            new TextEdit(new Range(new Position(1, 2), new Position(1, 6)), "b"),
                                                            new TextEdit(new Range(new Position(3, 1), new Position(4, 4)), "c")));
        WorkspaceEdit edit = new WorkspaceEdit(changes);
        Utils.applyWorkspaceEdit(edit);
        assertContent("0123456789\n" +
                      "01b6789\n" +
                      "012a6789\n" +
                      "0c456789\n",
                      sourceFile1);
        assertContent("0123456789\n" +
                      "01b6789\n" +
                      "012a6789\n" +
                      "0c456789\n",
                      sourceFile2);
        LifecycleManager.getDefault().saveAll();
    }

    public void testApplyChanges() throws Exception {
        clearWorkDir();
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject sourceFile1 = wd.createData("Test1.txt");
        try (OutputStream out = sourceFile1.getOutputStream()) {
            out.write(("0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n").getBytes("UTF-8"));
        }
        FileObject sourceFile2 = wd.createData("Test2.txt");
        try (OutputStream out = sourceFile2.getOutputStream()) {
            out.write(("0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n" +
                       "0123456789\n").getBytes("UTF-8"));
        }
        FileObject sourceFile3 = wd.createData("Test3.txt");
        WorkspaceEdit edit = new WorkspaceEdit(Arrays.asList(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(Utils.toURI(sourceFile1), -1), Arrays.asList(new TextEdit(new Range(new Position(2, 3), new Position(2, 6)), "a"),
                                                                                                                                                                                  new TextEdit(new Range(new Position(1, 2), new Position(1, 6)), "b"),
                                                                                                                                                                                  new TextEdit(new Range(new Position(3, 1), new Position(4, 4)), "c")))),
                                                             Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(Utils.toURI(sourceFile2), -1), Arrays.asList(new TextEdit(new Range(new Position(2, 3), new Position(2, 6)), "a"),
                                                                                                                                                                                  new TextEdit(new Range(new Position(1, 2), new Position(1, 6)), "b"),
                                                                                                                                                                                  new TextEdit(new Range(new Position(3, 1), new Position(4, 4)), "c")))),
                                                             Either.forRight(new CreateFile(Utils.toURI(sourceFile2).replace("Test2", "Test4"))),
                                                             Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(Utils.toURI(sourceFile2).replace("Test2", "Test4"), -1), Arrays.asList(new TextEdit(new Range(new Position(1, 1), new Position(1, 1)), "new content")))),
                                                             Either.forRight(new DeleteFile(Utils.toURI(sourceFile3))),
                                                             Either.forRight(new RenameFile(Utils.toURI(sourceFile1), Utils.toURI(sourceFile1).replace("Test1", "Test1a")))));
        Utils.applyWorkspaceEdit(edit);
        assertContent("0123456789\n" +
                      "01b6789\n" +
                      "012a6789\n" +
                      "0c456789\n",
                      wd.getFileObject("Test1a.txt"));
        assertContent("0123456789\n" +
                      "01b6789\n" +
                      "012a6789\n" +
                      "0c456789\n",
                      wd.getFileObject("Test2.txt"));
        assertContent("new content", wd.getFileObject("Test4.txt"));
        assertNull(wd.getFileObject("Test3.txt"));
        LifecycleManager.getDefault().saveAll();
    }

    public void testDefaultIndent1() throws Exception {
        doTestDefaultIndent("if (true) {|",
                            "\n",
                            "if (true) {\n" +
                            "    |");
    }

    public void testDefaultIndent2() throws Exception {
        doTestDefaultIndent("    if (true) {\n" +
                            "        |",
                            "}",
                            "    if (true) {\n" +
                            "    }|");
    }

    public void testDefaultIndent3() throws Exception {
        doTestDefaultIndent("   if (true) {\n" +
                            "             |",
                            "}",
                            "   if (true) {\n" +
                            "   }|");
    }

    public void testDefaultIndent4  () throws Exception {
        doTestDefaultIndent("         |",
                            "}",
                            "}|");
    }

    public void testDefaultIndent5() throws Exception {
        doTestDefaultIndent("   if (true) {\n" +
                            "   }|",
                            "}",
                            "   if (true) {\n" +
                            "   }}|");
    }

    private void doTestDefaultIndent(String code, String insertCode, String expectedResult) throws Exception {
        clearWorkDir();
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject sourceFile1 = wd.createData("Test1.txt");
        EditorCookie ec = sourceFile1.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        int insertPos = code.indexOf("|");
        code = code.replace("|", "");
        doc.insertString(0, code, null);
        javax.swing.text.Position caret = doc.createPosition(insertPos);
        doc.insertString(insertPos, insertCode, null);
        List<TextEdit> edits = Utils.computeDefaultOnTypeIndent(doc, insertPos, Utils.createPosition(doc, insertPos), insertCode);
        Utils.applyEditsNoLock(doc, edits);
        int expectedPos = expectedResult.indexOf("|");
        expectedResult = expectedResult.replace("|", "");
        assertEquals(expectedResult, doc.getText(0, doc.getLength()));
        assertEquals(expectedPos, caret.getOffset());
        LifecycleManager.getDefault().saveAll();
    }

    public void testRemovedTestPosition() throws Exception {
        assertEquals(new Position(2, 3), Utils.computeEndPositionForRemovedText(new Position(2, 2), "a"));
        assertEquals(new Position(4, 1), Utils.computeEndPositionForRemovedText(new Position(2, 2), "aaaaa\naaaaaaaa\na"));
    }

    private void assertContent(String expectedContent, FileObject sourceFile) throws Exception {
        EditorCookie ec = sourceFile.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = ec.openDocument();
        assertEquals(expectedContent,
                     doc.getText(0, doc.getLength()));
    }
    @ServiceProvider(service=MimeDataProvider.class)
    public static final class MimeDataProviderImpl implements MimeDataProvider {
        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().equals("text/plain")) {
                return Lookups.singleton(new PlainKit());
            }
            return Lookup.EMPTY;
        }
    }
}
