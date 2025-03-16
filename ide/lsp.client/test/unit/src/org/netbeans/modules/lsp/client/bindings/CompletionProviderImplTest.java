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
package org.netbeans.modules.lsp.client.bindings;

import com.google.common.base.Function;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple.Two;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.completion.CompletionImpl;
import org.netbeans.modules.editor.completion.CompletionResultSetImpl;
import org.netbeans.modules.lsp.client.TestUtils.MimeDataProviderImpl;
import org.netbeans.modules.lsp.client.TestUtils.MockLSP;
import org.netbeans.modules.lsp.client.TestUtils.MockMimeResolver;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class CompletionProviderImplTest extends NbTestCase {

    public CompletionProviderImplTest(String name) {
        super(name);
    }

    public void testConvertSnippet2CodeTemplate() {
        doRunConvertSnippetTest("println(${1:args});$0",
                  "println(${T1 default=\"args\"});${cursor}");
        doRunConvertSnippetTest("println(${1:args}, $1);$0",
                  "println(${T1 default=\"args\"}, ${T1});${cursor}");
        //choices are not supported currently:
        doRunConvertSnippetTest("println(${1|one,two,three|});$0",
                  "println(${T1});${cursor}");
        //variables are not supported currently:
        doRunConvertSnippetTest("println(${TM_SELECTED_TEXT:/upcase});$0",
                  "println(${P0});${cursor}");
    }

    private void doRunConvertSnippetTest(String snippet, String template) {
        String converted = CompletionProviderImpl.convertSnippet2CodeTemplate(snippet);
        assertEquals(template, converted);
    }

    public void testCommitWithResolveInsertPlainTextAndAdditionalEdits() throws IOException {
        runCompletionDefaultActionTest(
                """
                foo.ins|
                """,
                new TestTextDocumentService() {
                    @Override
                    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of(
                            new CompletionItem("to-be-resolved")
                        )));
                    }
                    @Override
                    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
                        assertEquals("to-be-resolved", unresolved.getLabel());

                        CompletionItem resolved = new CompletionItem(unresolved.getLabel());

                        resolved.setInsertText("inserted");
                        resolved.setInsertTextFormat(InsertTextFormat.PlainText);
                        resolved.setAdditionalTextEdits(List.of(new TextEdit(new Range(new Position(0, 0), new Position(0, 1)), "new-")));

                        return CompletableFuture.completedFuture(resolved);
                    }
                },
                """
                new-oo.inserted
                """);
    }

    public void testCommitWithResolveInsertSnippetAndAdditionalEdits() throws IOException {
        runCompletionDefaultActionTest(
                """
                foo.ins|
                """,
                new TestTextDocumentService() {
                    @Override
                    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of(
                            new CompletionItem("to-be-resolved")
                        )));
                    }
                    @Override
                    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
                        assertEquals("to-be-resolved", unresolved.getLabel());

                        CompletionItem resolved = new CompletionItem(unresolved.getLabel());

                        resolved.setInsertText("inserted(${1:args});$0");
                        resolved.setInsertTextFormat(InsertTextFormat.Snippet);
                        resolved.setAdditionalTextEdits(List.of(new TextEdit(new Range(new Position(0, 0), new Position(0, 1)), "new-")));

                        return CompletableFuture.completedFuture(resolved);
                    }
                },
                """
                new-oo.inserted(args);
                """);
    }

    public void testCommitWithResolveSnippetMainEditAndAdditionalEdits() throws IOException {
        runCompletionDefaultActionTest(
                """
                foo.ins|
                """,
                new TestTextDocumentService() {
                    @Override
                    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of(
                            new CompletionItem("to-be-resolved")
                        )));
                    }
                    @Override
                    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
                        assertEquals("to-be-resolved", unresolved.getLabel());

                        CompletionItem resolved = new CompletionItem(unresolved.getLabel());

                        resolved.setInsertTextFormat(InsertTextFormat.Snippet);
                        resolved.setTextEdit(Either.forLeft(new TextEdit(new Range(new Position(0, 3), new Position(0, 7)), ".inserted(${1:args});$0")));
                        resolved.setAdditionalTextEdits(List.of(new TextEdit(new Range(new Position(0, 0), new Position(0, 1)), "new-")));

                        return CompletableFuture.completedFuture(resolved);
                    }
                },
                """
                new-oo.inserted(args);
                """);
    }

    public void testCommitWithResolveMainEditAndAdditionalEdits() throws IOException {
        runCompletionDefaultActionTest(
                """
                foo.ins|
                """,
                new TestTextDocumentService() {
                    @Override
                    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of(
                            new CompletionItem("to-be-resolved")
                        )));
                    }
                    @Override
                    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
                        assertEquals("to-be-resolved", unresolved.getLabel());

                        CompletionItem resolved = new CompletionItem(unresolved.getLabel());

                        resolved.setTextEdit(Either.forLeft(new TextEdit(new Range(new Position(0, 4), new Position(0, 7)), "inserted")));
                        resolved.setAdditionalTextEdits(List.of(new TextEdit(new Range(new Position(0, 0), new Position(0, 1)), "new-")));

                        return CompletableFuture.completedFuture(resolved);
                    }
                },
                """
                new-oo.inserted
                """);
    }

    public void testNoCompletionWhenNotEnabled() throws IOException {
        AtomicBoolean notInvoked = new AtomicBoolean();

        runCompletionTest(
                """
                foo.ins|
                """,
                new TestTextDocumentService() {
                    @Override
                    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
                        notInvoked.set(true);
                        return super.completion(position);
                    }
                },
                init -> init,
                (c, items) -> {
                    assertEquals(0, items.size());
                });
        assertFalse(notInvoked.get());
    }

    public void testNoResolveProvider() throws IOException {
        AtomicBoolean notInvoked = new AtomicBoolean();

        runCompletionDefaultActionTest(
                """
                foo.ins|
                """,
                new TestTextDocumentService() {
                    @Override
                    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
                        return CompletableFuture.completedFuture(Either.forLeft(List.of(
                            new CompletionItem("inserted")
                        )));
                    }
                    @Override
                    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
                        notInvoked.set(true);

                        return super.resolveCompletionItem(unresolved);
                    }
                },
                init -> {
                    init.getCapabilities().setCompletionProvider(new CompletionOptions(false, List.of()));
                    return init;
                },
                """
                foo.inserted
                """);
        assertFalse(notInvoked.get());
    }

    public void testSignatureTest1() throws IOException {
        runSignatureTest(
                """
                foo.method(|
                """,
                new TestTextDocumentService() {
                    @Override
                    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
                        SignatureHelp result = new SignatureHelp(List.of(
                            new SignatureInformation(""),
                            new SignatureInformation("String s"),
                            new SignatureInformation("String s1, String s2, String s3", (String) null, List.of(new ParameterInformation("String s1"), createParameterInformationForIntegers(11, 20), new ParameterInformation("String s3")))
                        ), 1, 1);
                        return CompletableFuture.completedFuture(result);
                    }
                },
                "<html>" + Bundle.DN_NoParameter() + "<br>String s<br>String s1, <b>String s2</b>, String s3<br>");
    }

    private static ParameterInformation createParameterInformationForIntegers(int start, int end) {
        ParameterInformation result = new ParameterInformation();

        result.setLabel(new Two<>(start, end));

        return result;
    }

    private void runCompletionDefaultActionTest(String testCodeWithCaret,
                                                TextDocumentService documentService,
                                                String expectedOutput) throws IOException {
        runCompletionDefaultActionTest(testCodeWithCaret, documentService, init -> {
            init.getCapabilities().setCompletionProvider(new CompletionOptions(true, List.of()));
            return init;
        }, expectedOutput);
    }

    private void runCompletionDefaultActionTest(String testCodeWithCaret,
                                                TextDocumentService documentService,
                                                Function<InitializeResult, InitializeResult> adjustInitializeResult,
                                                String expectedOutput) throws IOException {
        runCompletionTest(testCodeWithCaret, documentService, adjustInitializeResult, (c, items) -> {
            assertEquals(1, items.size());
            items.get(0).defaultAction(c);
            assertEquals(expectedOutput, c.getText());
        });
    }

    private void runCompletionTest(String testCodeWithCaret,
                                   TextDocumentService documentService,
                                   Function<InitializeResult, InitializeResult> adjustInitializeResult,
                                   CompletionValidator validator) throws IOException {
        runAnyCompletionTest(testCodeWithCaret, documentService, adjustInitializeResult, c -> {
            CompletionTask task = new CompletionProviderImpl().createTask(CompletionProvider.COMPLETION_QUERY_TYPE, c);
            CompletionResultSetImpl resultImpl = CompletionImpl.get().createTestResultSet(task, CompletionProvider.COMPLETION_QUERY_TYPE);
            task.query(resultImpl.getResultSet());
            while (!resultImpl.isFinished()) {
                try {
                    Thread.sleep(1); //XXX
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            validator.validate(c, resultImpl.getItems());
        });
    }

    interface CompletionValidator {
        public void validate(JTextComponent c, List<? extends org.netbeans.spi.editor.completion.CompletionItem> items);
    }

    private void runSignatureTest(String testCodeWithCaret,
                                  TextDocumentService documentService,
                                  String toolTipText) throws IOException {
        runSignatureTest(testCodeWithCaret, documentService, init -> {
            init.getCapabilities().setSignatureHelpProvider(new SignatureHelpOptions());
            return init;
        }, toolTipText);
    }

    private void runSignatureTest(String testCodeWithCaret,
                                  TextDocumentService documentService,
                                  Function<InitializeResult, InitializeResult> adjustInitializeResult,
                                  String toolTipText) throws IOException {
        runAnyCompletionTest(testCodeWithCaret, documentService, adjustInitializeResult, c -> {
            CompletionTask task = new CompletionProviderImpl().createTask(CompletionProvider.TOOLTIP_QUERY_TYPE, c);
            CompletionResultSetImpl resultImpl = CompletionImpl.get().createTestResultSet(task, CompletionProvider.TOOLTIP_QUERY_TYPE);
            task.query(resultImpl.getResultSet());
            while (!resultImpl.isFinished()) {
                try {
                    Thread.sleep(1); //XXX
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            assertEquals(toolTipText, resultImpl.getToolTip().getTipText());
        });
    }

    private void runAnyCompletionTest(String testCodeWithCaret,
                                      TextDocumentService documentService,
                                      Function<InitializeResult, InitializeResult> adjustInitializeResult,
                                      Consumer<JTextComponent> runCompletion) throws IOException {
        MockLSP.createServer = () -> new TestLanguageServer(documentService, adjustInitializeResult);
        MockServices.setServices(MimeDataProviderImpl.class, MockMimeResolver.class);

        FileObject folder = FileUtil.createMemoryFileSystem().getRoot().createFolder("myfolder");
        FileObject testFile = folder.createData("data1.mock-txt");

        int caret = testCodeWithCaret.indexOf('|');
        String testCode = testCodeWithCaret.substring(0, caret) + testCodeWithCaret.substring(caret + 1);

        try (OutputStream out = testFile.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write(testCode);
        }

        EditorCookie ec = testFile.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = ec.openDocument();
        JEditorPane c = new JEditorPane();
        c.setEditorKit(new NbEditorKit());
        c.setDocument(doc);
        c.setCaretPosition(4);
        runCompletion.accept(c);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    private static final class TestLanguageServer implements LanguageServer {

        private final TextDocumentService textDocumentService;
        private final WorkspaceService workspaceService = new TestWorkspaceService();
        private final Function<InitializeResult, InitializeResult> adjustInitializeResult;

        public TestLanguageServer(TextDocumentService textDocumentService) {
            this(textDocumentService, init -> init);
        }

        public TestLanguageServer(TextDocumentService textDocumentService,
                                  Function<InitializeResult, InitializeResult> adjustInitializeResult) {
            this.textDocumentService = textDocumentService;
            this.adjustInitializeResult = adjustInitializeResult;
        }

        @Override
        public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
            InitializeResult result = new InitializeResult();
            ServerCapabilities capa = new ServerCapabilities();
            result.setCapabilities(capa);
            return CompletableFuture.completedFuture(adjustInitializeResult.apply(result));
        }

        @Override
        public CompletableFuture<Object> shutdown() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void exit() {
        }

        @Override
        public TextDocumentService getTextDocumentService() {
            return textDocumentService;
        }

        @Override
        public WorkspaceService getWorkspaceService() {
            return workspaceService;
        }

    }

    private static class TestTextDocumentService implements TextDocumentService {

        @Override
        public void didOpen(DidOpenTextDocumentParams params) {}

        @Override
        public void didChange(DidChangeTextDocumentParams params) {}

        @Override
        public void didClose(DidCloseTextDocumentParams params) {}

        @Override
        public void didSave(DidSaveTextDocumentParams params) {}

    }

    private static class TestWorkspaceService implements WorkspaceService {

        @Override
        public void didChangeConfiguration(DidChangeConfigurationParams params) {}

        @Override
        public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {}

    }

}
