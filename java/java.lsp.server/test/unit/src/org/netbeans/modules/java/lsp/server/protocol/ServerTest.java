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
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.ImplementationParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.TypeDefinitionParams;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkDoneProgressCancelParams;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressKind;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.infrastructure.JavaErrorProvider;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.lsp.ErrorProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;

/**
 *
 * @author lahvac
 */
public class ServerTest extends NbTestCase {

    private final Gson gson = new Gson();
    private Socket client;
    private Thread serverThread;

    public ServerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("java.awt.headless", Boolean.TRUE.toString());
        super.setUp();
        clearWorkDir();
        ServerSocket srv = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        serverThread = new Thread(() -> {
            try {
                Socket server = srv.accept();

                Path tempDir = Files.createTempDirectory("lsp-server");
                File userdir = tempDir.resolve("scratch-user").toFile();
                File cachedir = tempDir.resolve("scratch-cache").toFile();
                System.setProperty("netbeans.user", userdir.getAbsolutePath());
                File varLog = new File(new File(userdir, "var"), "log");
                varLog.mkdirs();
                System.setProperty("jdk.home", System.getProperty("java.home")); //for j2seplatform
                Class<?> main = Class.forName("org.netbeans.core.startup.Main");
                main.getDeclaredMethod("initializeURLFactory").invoke(null);
                new File(cachedir, "index").mkdirs();
                Class jsClass = JavaSource.class;
                File javaCluster = Utilities.toFile(jsClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile();
                System.setProperty("netbeans.dirs", javaCluster.getAbsolutePath());
                CacheFolderProvider.getCacheFolderForRoot(Utilities.toURI(Places.getUserDirectory()).toURL(), EnumSet.noneOf(CacheFolderProvider.Kind.class), CacheFolderProvider.Mode.EXISTENT);

                Lookup.getDefault().lookup(ModuleInfo.class); //start the module system

                CommandLine.getDefault().process(new String[] {"--start-java-language-server"}, server.getInputStream(), server.getOutputStream(), System.err, getWorkDir());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
        serverThread.start();
        client = new Socket(srv.getInetAddress(), srv.getLocalPort());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TextDocumentServiceImpl.HOOK_NOTIFICATION = null;
        serverThread.stop();
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
    }
    
    final List<Diagnostic>[] diags = new List[1];
    Set<String> diagnosticURIs = Collections.synchronizedSet(new HashSet<>());
    
    void clearDiagnostics() {
        synchronized (diags) {
            diagnosticURIs.clear();
            diags[0] = null;
        }
    }
    
    void cancelDiagnostics(AtomicBoolean cancel) {
        synchronized (diags) {
            cancel.set(true);
            diags.notifyAll();
        }
    }
    
    class LspClient implements LanguageClient {
        List<MessageParams> loggedMessages = new ArrayList<>();

        @Override
        public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void notifyProgress(ProgressParams params) {
        }
        
        
        @Override
        public void telemetryEvent(Object arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams params) {
            synchronized (diags) {
                diagnosticURIs.add(params.getUri());
                diags[0] = params.getDiagnostics();
                diags.notifyAll();
            }
        }

        @Override
        public void showMessage(MessageParams arg0) {
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void logMessage(MessageParams arg0) {
            loggedMessages.add(arg0);
        }
    }

    public void testMain() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test { int i = \"\".hashCode(); public void run() { this.test(); } /**Test.*/public void test() {} }";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient(), client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        assertDiags(diags);//errors
        assertDiags(diags);//hints
        int hashCodeStart = code.indexOf("hashCode");
        Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, hashCodeStart + 2))).get();
        assertTrue(completion.isRight());
        List<String> actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Method:hashCode() : int"), actualItems);
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(toURI(src));
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, hashCodeStart), new Position(0, hashCodeStart + "hashCode".length())), "hashCode".length(), "equ"))));
        assertDiags(diags, "Error:0:31-0:34");//errors
        assertDiags(diags, "Error:0:31-0:34");//hints
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, hashCodeStart + 2))).get();
        actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        if (jdk9Plus()) {
            assertEquals(Arrays.asList("Method:equals(Object anObject) : boolean", "Method:equalsIgnoreCase(String anotherString) : boolean"), actualItems);
        }
        int testStart = code.indexOf("test") + "equ".length() - "hashCode".length();
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, testStart + 3))).get();
        List<CompletionItem> actualCompletionItem = completion.getRight().getItems();
        actualItems = actualCompletionItem.stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Method:test() : void"), actualItems);
        assertEquals(null, actualCompletionItem.get(0).getDocumentation());
        CompletionItem resolvedItem = server.getTextDocumentService().resolveCompletionItem(actualCompletionItem.get(0)).get();
        assertEquals("**[Test](*0)**\n" +
                     "\n" +
                     "```\n" +
                     "public void test()\n" +
                     "```\n" +
                     "\n" +
                     "Test.\n" +
                     "\n",
                     resolvedItem.getDocumentation().getRight().getValue());
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, 0))).get();
        actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertTrue(actualItems.contains("Keyword:interface"));
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, hashCodeStart), new Position(0, hashCodeStart + "equ".length())), "equ".length(), "hashCode"))));
        int closingBrace = code.lastIndexOf("}");
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, closingBrace), new Position(0, closingBrace)), 0, "public String c(Object o) {\nreturn o;\n}"))));
        List<Diagnostic> diagnostics = assertDiags(diags, "Error:1:0-1:9"); //errors
        assertDiags(diags, "Error:1:0-1:9");//hints
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(1, 0), new Position(1, 9)), new CodeActionContext(Arrays.asList(diagnostics.get(0))))).get();
        String log = codeActions.toString();
        assertTrue(log, codeActions.size() >= 2);
        assertTrue(log, codeActions.get(0).isRight());
        CodeAction action = codeActions.get(0).getRight();
        assertEquals("Cast ...o to String", action.getTitle());
        assertEquals(1, action.getEdit().getDocumentChanges().size());
        assertEquals(1, action.getEdit().getDocumentChanges().get(0).getLeft().getEdits().size());
        TextEdit edit = action.getEdit().getDocumentChanges().get(0).getLeft().getEdits().get(0);
        assertEquals(1, edit.getRange().getStart().getLine());
        assertEquals(7, edit.getRange().getStart().getCharacter());
        assertEquals(1, edit.getRange().getEnd().getLine());
        assertEquals(7, edit.getRange().getEnd().getCharacter());
        assertEquals("(String) ", edit.getNewText());
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, closingBrace), new Position(0, closingBrace)), 0, "public  void assignToSelf(Object o) { o = o; }"))));
        assertDiags(diags, "Error:1:0-1:9");//errors
        assertDiags(diags, "Error:1:0-1:9", "Warning:0:148-0:153", "Warning:0:152-0:153");//hints
    }
    
    /**
     * Checks that diagnostics are cleared if the file vanishes. Uses didClose to trigger reparse,
     * similar to vscode that notices a file has been removed or renames the file.
     */
    public void testDiagnosticsRemovedForDeletedFile() throws Exception {
        AtomicBoolean cancel = new AtomicBoolean(false);
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test { int i = \"\".hash(); public void run() { this.test(); } /**Test.*/public void test() {} }";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient(), client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        assertDiags(diags, "Error:0:31-0:35");//errors
        
        clearDiagnostics();
        Files.move(src.toPath(), src.toPath().resolveSibling("Test2.java"));
        
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(toURI(src));

        server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(id));
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            cancelDiagnostics(cancel);
        }, 5, TimeUnit.SECONDS);
        
        assertDiags(diags, cancel);
        assertEquals(1, diagnosticURIs.size());
        assertEquals(toURI(src), diagnosticURIs.iterator().next());
    }
    
    private class OpenCloseHook {
        private Semaphore didOpenCompleted = new Semaphore(0);
        private Semaphore didCloseCompleted = new Semaphore(0);
        private Semaphore didChangeCompleted = new Semaphore(0);
        
        public void accept(String n, Object params){
            switch (n) {
                case "didOpen":
                    didOpenCompleted.release();
                    break;
                case "didClose":
                    didCloseCompleted.release();
                    break;
                case "didChange":
                    didChangeCompleted.release();
                    break;
            }
        }
    }
    
    private static final String SAMPLE_CODE = 
                "public class Test \n"
                + "{ \n"
                + "  int i = \"\".hashCode();\n"
                + "  public void run() {\n"
                + "    this.test(); \n"
                + "  }\n\n"
                + "  /**Test.*/public void test() {\n"
                + "  }\n"
                + "}";
    
    /**
     * Checks that opening the document preserves lines. This is necessary for breakpoints
     * or computed markers. The test will:
     * <ul>
     * <li>Open a document, create a Line object (which uses PositionRefs). Close the doucment. Load with didOpen(). This is the initial scenario.
     * <li>Leave line's document opened; load with didOpen(). Simulates the case that the backend has been working with the text.
     * <li>Initially opens a document with didOpen(). Then simulate close with didClose() with a recorded position; open again with didOpen().
     * </ul>
     * 
     * @throws Exception 
     */
    public void testDidOpenPreservesLines() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        File src2 = new File(getWorkDir(), "Test2.java");
        File src3 = new File(getWorkDir(), "Test3.java");
        src.getParentFile().mkdirs();
        String code = SAMPLE_CODE;
        String code2 = code.replace("Test", "Test2");
        String code3 = code.replace("Test", "Test3");
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        try (Writer w = new FileWriter(src2)) {
            w.write(code2);
        }
        try (Writer w = new FileWriter(src3)) {
            w.write(code3);
        }
        
        FileObject f1 = FileUtil.toFileObject(src);
        EditorCookie cake = f1.getLookup().lookup(EditorCookie.class);
        LineCookie lines = f1.getLookup().lookup(LineCookie.class);
        
        StyledDocument d = cake.openDocument();
        javax.swing.text.Position p = NbDocument.createPosition(d, 23, javax.swing.text.Position.Bias.Forward);
        int offset1 = p.getOffset();
        int line1 = NbDocument.findLineNumber(d, p.getOffset());
        Line lineObject1 = lines.getLineSet().getCurrent(line1);
        cake.close();
        
        
        FileObject f2 = FileUtil.toFileObject(src2);
        cake = f2.getLookup().lookup(EditorCookie.class);
        StyledDocument d2 = cake.openDocument();
        javax.swing.text.Position p2 = NbDocument.createPosition(d2, 40, javax.swing.text.Position.Bias.Forward);
        int offset2 = p2.getOffset();
        int line2 = NbDocument.findLineNumber(d2, offset2);
        
        LineCookie lines2 = f2.getLookup().lookup(LineCookie.class);
        Line lineObject2 = lines2.getLineSet().getCurrent(line2);
        
        OpenCloseHook hook = new OpenCloseHook();
        TextDocumentServiceImpl.HOOK_NOTIFICATION = hook::accept;

        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient(), client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();


        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src2.toURI().toString(), "java", 0, code2)));
        assertTrue(hook.didOpenCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        int nl2 = NbDocument.findLineNumber(d2, p2.getOffset());
        assertEquals(line2, lineObject2.getLineNumber());
        assertEquals(line2, nl2);
        
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));
        assertTrue(hook.didOpenCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        d = cake.openDocument();
        int nl1 = NbDocument.findLineNumber(d, p.getOffset());
        assertEquals(line1, lineObject1.getLineNumber());
        assertEquals(line1, nl1);

        FileObject f3 = FileUtil.toFileObject(src3);
        TextDocumentItem tdi = new TextDocumentItem(src3.toURI().toString(), "java", 0, code3);
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(tdi));
        assertTrue(hook.didOpenCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));

        cake = f3.getLookup().lookup(EditorCookie.class);
        StyledDocument d3 = cake.openDocument();
        javax.swing.text.Position p3 = NbDocument.createPosition(d3, 40, javax.swing.text.Position.Bias.Forward);
        int offset3 = p3.getOffset();
        int line3 = NbDocument.findLineNumber(d3, offset3);
        LineCookie lines3 = f3.getLookup().lookup(LineCookie.class);
        Line lineObject3 = lines3.getLineSet().getCurrent(line3);

        server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(src3.toURI().toString())));
        assertTrue(hook.didCloseCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        // open again
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(tdi));
        assertTrue(hook.didOpenCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        int nl3 = NbDocument.findLineNumber(d, p3.getOffset());
        assertEquals(line3, lineObject3.getLineNumber());
        assertEquals(line3, nl3);

        // close and release the document, too
        server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(src3.toURI().toString())));
        assertTrue(hook.didCloseCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        Reference<StyledDocument> refDoc = new WeakReference<>(d3);
        d3 = null;
        assertGC("Document should be collected", refDoc);
        assertNull(cake.getDocument());

        // open again
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(tdi));
        assertTrue(hook.didOpenCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        nl3 = NbDocument.findLineNumber(d, p3.getOffset());
        assertEquals(line3, lineObject3.getLineNumber());
        assertEquals(line3, nl3);
    }
    
    /**
     * Simulates Ctrl-N ve VScode plus paste of initial content, then save. According to the
     * report, DidOpen will come with an empty forced initial content. The DidChange comes that will
     * inject the pasted content. 
     * @throws Exception 
     */
    public void testSimulateNewUnnamedFile() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        
        String code = SAMPLE_CODE;
        // write in an initial code on the disk
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        
        FileObject f1 = FileUtil.toFileObject(src);
        OpenCloseHook hook = new OpenCloseHook();
        TextDocumentServiceImpl.HOOK_NOTIFICATION = hook::accept;

        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient(), client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        
        // open with empty initial content.
        String uriString = src.toURI().toString();
        server.getTextDocumentService().didOpen(
                new DidOpenTextDocumentParams(new TextDocumentItem(uriString, "java", 0, ""))
        );
        
        EditorCookie cake = f1.getLookup().lookup(EditorCookie.class);
        assertTrue(hook.didOpenCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(uriString, 1);
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
                id, Arrays.asList(
                    new TextDocumentContentChangeEvent(new Range(new Position(0, 0), new Position(0, 0)), 0, code)
                )
        ));
        
        assertTrue(hook.didChangeCompleted.tryAcquire(400, TimeUnit.MILLISECONDS));
        
        Document doc = cake.openDocument();
        assertEquals(code, doc.getText(0, doc.getLength()));
        
    }
    
    public void testCodeActionWithRemoval() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test { public String c(String s) {\nreturn s.toString();\n} }";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        assertDiags(diags); //errors
        List<Diagnostic> diagnostics = assertDiags(diags, "Warning:1:7-1:19");//hints
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(toURI(src));
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(1, 7), new Position(1, 19)), new CodeActionContext(Arrays.asList(diagnostics.get(0))))).get();
        String log = codeActions.toString();
        assertTrue(log, codeActions.size() >= 1);
        assertTrue(log, codeActions.get(0).isRight());
        CodeAction action = codeActions.get(0).getRight();
        assertEquals("Remove .toString()", action.getTitle());
        assertEquals(1, action.getEdit().getDocumentChanges().size());
        assertEquals(1, action.getEdit().getDocumentChanges().get(0).getLeft().getEdits().size());
        TextEdit edit = action.getEdit().getDocumentChanges().get(0).getLeft().getEdits().get(0);
        assertEquals(1, edit.getRange().getStart().getLine());
        assertEquals(8, edit.getRange().getStart().getCharacter());
        assertEquals(1, edit.getRange().getEnd().getLine());
        assertEquals(19, edit.getRange().getEnd().getCharacter());
        assertEquals("", edit.getNewText());
    }

    private List<Diagnostic> assertDiags(List<Diagnostic>[] diags, String... expected) {
        return assertDiags(diags, new AtomicBoolean(false), expected);
    }
    
    private List<Diagnostic> assertDiags(List<Diagnostic>[] diags, AtomicBoolean cancel, String... expected) {
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                    //ignore
                }
                if (cancel.get()) {
                    fail("Diagnostics not received");
                }
            }
            Set<String> actualDiags = diags[0].stream()
                                               .map(d -> d.getSeverity() + ":" +
                                                         d.getRange().getStart().getLine() + ":" + d.getRange().getStart().getCharacter() + "-" +
                                                         d.getRange().getEnd().getLine() + ":" + d.getRange().getEnd().getCharacter())
                                               .collect(Collectors.toSet());
            String diagsMessage = diags[0].stream()
                                          .map(d -> d.getSeverity() + ":" +
                                                    d.getRange().getStart().getLine() + ":" + d.getRange().getStart().getCharacter() + "-" +
                                                    d.getRange().getEnd().getLine() + ":" + d.getRange().getEnd().getCharacter() + ": " +
                                                    d.getMessage())
                                               .collect(Collectors.joining("\n"));
            assertEquals(diagsMessage, new HashSet<>(Arrays.asList(expected)), actualDiags);

            List<Diagnostic> result = diags[0];

            diags[0] = null;

            return result;
        }
    }

    public void testNavigator() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private int field;\n" +
                      "    public void method() {\n" +
                      "    }\n" +
                      "    class Inner {\n" +
                      "        public void innerMethod() {\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        FileUtil.refreshFor(getWorkDir());
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        List<Either<SymbolInformation, DocumentSymbol>> symbols = server.getTextDocumentService().documentSymbol(new DocumentSymbolParams(new TextDocumentIdentifier(toURI(src)))).get();
        String textualSymbols = "";
        String sep = "";
        for (Either<SymbolInformation, DocumentSymbol> sym : symbols) {
            assertTrue(sym.isRight());
            textualSymbols += sep;
            textualSymbols += toString(sym.getRight());
            sep = ", ";
        }
        String expected = "Class:Test:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 0\n" +
                          "    character = 0\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 8\n" +
                          "    character = 1\n" +
                          "  ]\n" +
                          "]:(Class:Inner:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 4\n" +
                          "    character = 4\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 7\n" +
                          "    character = 5\n" +
                          "  ]\n" +
                          "]:(Constructor:Inner:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 4\n" +
                          "    character = 4\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 4\n" +
                          "    character = 4\n" +
                          "  ]\n" +
                          "]:(), Method:innerMethod:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 5\n" +
                          "    character = 8\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 6\n" +
                          "    character = 9\n" +
                          "  ]\n" +
                          "]:()), Constructor:Test:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 0\n" +
                          "    character = 7\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 0\n" +
                          "    character = 7\n" +
                          "  ]\n" +
                          "]:(), Field:field:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 1\n" +
                          "    character = 4\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 1\n" +
                          "    character = 22\n" +
                          "  ]\n" +
                          "]:(), Method:method:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 2\n" +
                          "    character = 4\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 3\n" +
                          "    character = 5\n" +
                          "  ]\n" +
                          "]:())";
        assertEquals(expected, textualSymbols);
    }

    private String toString(DocumentSymbol sym) {
        return sym.getKind().toString() + ":" +
               sym.getName() + ":" +
               sym.getRange() + ":" +
               sym.getChildren()
                  .stream()
                  .map(this::toString)
                  .collect(Collectors.joining(", ", "(", ")"));
    }

    public void testGoToDefinition() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    private int field;\n" +
                      "    public void method(int ppp) {\n" +
                      "        System.err.println(field);\n" +
                      "        System.err.println(ppp);\n" +
                      "        new Other().test();\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        File otherSrc = new File(getWorkDir(), "Other.java");
        try (Writer w = new FileWriter(otherSrc)) {
            w.write("/**Some source*/\n" +
                    "public class Other {\n" +
                    "    public void test() { }\n" +
                    "}");
        }
        FileUtil.refreshFor(getWorkDir());
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(getWorkDir().toURI().toString());
        server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        Position pos = new Position(3, 30);
        List<? extends Location> definition = server.getTextDocumentService().definition(new DefinitionParams(new TextDocumentIdentifier(toURI(src)), pos)).get().getLeft();
        assertEquals(1, definition.size());
        assertEquals(toURI(src), definition.get(0).getUri());
        assertEquals(1, definition.get(0).getRange().getStart().getLine());
        assertEquals(16, definition.get(0).getRange().getStart().getCharacter());
        assertEquals(1, definition.get(0).getRange().getEnd().getLine());
        assertEquals(21, definition.get(0).getRange().getEnd().getCharacter());
        pos = new Position(4, 30);
        definition = server.getTextDocumentService().definition(new DefinitionParams(new TextDocumentIdentifier(toURI(src)), pos)).get().getLeft();
        assertEquals(1, definition.size());
        assertEquals(toURI(src), definition.get(0).getUri());
        assertEquals(2, definition.get(0).getRange().getStart().getLine());
        assertEquals(27, definition.get(0).getRange().getStart().getCharacter());
        assertEquals(2, definition.get(0).getRange().getEnd().getLine());
        assertEquals(30, definition.get(0).getRange().getEnd().getCharacter());
        pos = new Position(5, 22);
        definition = server.getTextDocumentService().definition(new DefinitionParams(new TextDocumentIdentifier(toURI(src)), pos)).get().getLeft();
        assertEquals(1, definition.size());
        assertEquals(toURI(otherSrc), definition.get(0).getUri());
        assertEquals(2, definition.get(0).getRange().getStart().getLine());
        assertEquals(16, definition.get(0).getRange().getStart().getCharacter());
        assertEquals(2, definition.get(0).getRange().getEnd().getLine());
        assertEquals(20, definition.get(0).getRange().getEnd().getCharacter());
    }

    public void testGoToTypeDefinition() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    private Other field;\n" +
                      "    public void test() {\n" +
                      "        System.err.println(field);\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        File otherSrc = new File(getWorkDir(), "Other.java");
        try (Writer w = new FileWriter(otherSrc)) {
            w.write("/**Some source*/\n" +
                    "public class Other {\n" +
                    "    public void test() { }\n" +
                    "}");
        }
        FileUtil.refreshFor(getWorkDir());
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(getWorkDir().toURI().toString());
        server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        Position pos = new Position(3, 30);
        List<? extends Location> typeDefinition = server.getTextDocumentService().typeDefinition(new TypeDefinitionParams(new TextDocumentIdentifier(toURI(src)), pos)).get().getLeft();
        assertEquals(1, typeDefinition.size());
        assertEquals(toURI(otherSrc), typeDefinition.get(0).getUri());
        assertEquals(1, typeDefinition.get(0).getRange().getStart().getLine());
        assertEquals(13, typeDefinition.get(0).getRange().getStart().getCharacter());
        assertEquals(1, typeDefinition.get(0).getRange().getEnd().getLine());
        assertEquals(18, typeDefinition.get(0).getRange().getEnd().getCharacter());
    }

    public void testGoToImplementations() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public interface Test {\n" +
                      "    void test();\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        File otherSrc = new File(getWorkDir(), "Other.java");
        try (Writer w = new FileWriter(otherSrc)) {
            w.write("/**Some source*/\n" +
                    "public class Other implements Test {\n" +
                    "    public void test() {}\n" +
                    "}\n");
        }
        FileUtil.refreshFor(getWorkDir());
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(getWorkDir().toURI().toString());
        server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        Position pos = new Position(1, 10);
        List<? extends Location> implementations = server.getTextDocumentService().implementation(new ImplementationParams(new TextDocumentIdentifier(toURI(src)), pos)).get().getLeft();
        assertEquals(1, implementations.size());
        assertEquals(toURI(otherSrc), implementations.get(0).getUri());
        assertEquals(2, implementations.get(0).getRange().getStart().getLine());
        assertEquals(16, implementations.get(0).getRange().getStart().getCharacter());
        assertEquals(2, implementations.get(0).getRange().getEnd().getLine());
        assertEquals(20, implementations.get(0).getRange().getEnd().getCharacter());
    }

    public void testGoToSuperImplementation() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test implements Other {\n" +
                      "    public void test() {\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        File otherSrc = new File(getWorkDir(), "Other.java");
        try (Writer w = new FileWriter(otherSrc)) {
            w.write("/**Some source*/\n" +
                    "public interface Other {\n" +
                    "    void test();\n" +
                    "}");
        }
        FileUtil.refreshFor(getWorkDir());
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams params) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        Position pos = new Position(1, 20);
        Object ret = server.getWorkspaceService().executeCommand(new ExecuteCommandParams(Server.JAVA_SUPER_IMPLEMENTATION, Arrays.asList(new Object[] {toURI(src), pos}))).get();
        assertNotNull(ret);
        Location[] locs = gson.fromJson(gson.toJsonTree(ret).getAsJsonArray(), Location[].class);
        assertNotNull(locs);
        assertEquals(1, locs.length);
        Location loc = locs[0];
        assertEquals(toURI(otherSrc), loc.getUri());
        assertEquals(2, loc.getRange().getStart().getLine());
        assertEquals(9, loc.getRange().getStart().getCharacter());
        assertEquals(2, loc.getRange().getEnd().getLine());
        assertEquals(13, loc.getRange().getEnd().getCharacter());
    }

    public void testFindDebugAttachConfigurations() throws Exception {
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        Object ret = server.getWorkspaceService().executeCommand(new ExecuteCommandParams(Server.JAVA_FIND_DEBUG_ATTACH_CONFIGURATIONS, Collections.emptyList())).get();
        assertNotNull(ret);
        DebugConnector[] connectors = gson.fromJson(gson.toJsonTree(ret).getAsJsonArray(), DebugConnector[].class);
        assertTrue(connectors.length > 0);
        boolean haveAttachToPort = false;
        boolean haveAttachToProcess = false;
        for (DebugConnector c : connectors) {
            if ("Attach to Port".equals(c.getName())) {
                haveAttachToPort = true;
                checkAttachToPort(c);
            }
            if ("Attach to Process".equals(c.getName())) {
                haveAttachToProcess = true;
                checkAttachToProcess(c);
            }
        }
        assertTrue(Arrays.toString(connectors), haveAttachToPort && haveAttachToProcess);
    }

    private void checkAttachToPort(DebugConnector c) {
        assertEquals("java8+", c.getType());
        List<String> arguments = c.getArguments();
        assertEquals(2, arguments.size());
        assertEquals("hostName", arguments.get(0));
        assertEquals("port", arguments.get(1));
        assertEquals(2, c.getDefaultValues().size());
    }

    private void checkAttachToProcess(DebugConnector c) {
        assertEquals("java8+", c.getType());
        List<String> arguments = c.getArguments();
        assertEquals(1, arguments.size());
        assertEquals("processId", arguments.get(0));
        assertEquals(1, c.getDefaultValues().size());
    }

    public void testOpenProjectOpenJDK() throws Exception {
        getWorkDir().mkdirs();

        FileObject root = FileUtil.toFileObject(getWorkDir());
        try (Writer w = new OutputStreamWriter(FileUtil.createData(root, "jdk/src/java.base/share/classes/java/lang/Object.java").getOutputStream(), StandardCharsets.UTF_8)) {
            w.write("package java.lang; public class Object {}");
        }
        FileUtil.createData(root, "jdk/src/java.base/share/classes/impl/Service.java");
        FileObject javaBaseMI = FileUtil.createData(root, "jdk/src/java.base/share/classes/module-info.java");
        try (Writer w = new OutputStreamWriter(javaBaseMI.getOutputStream(), StandardCharsets.UTF_8)) {
            w.write("module java.base { exports java.lang; }");
        }
        try (Writer w = new OutputStreamWriter(FileUtil.createData(root, "jdk/src/java.compiler/share/classes/module-info.java").getOutputStream(), StandardCharsets.UTF_8)) {
            w.write("module java.compiler { }");
        }

        List<Diagnostic>[] diags = new List[1];
        boolean[] indexingComplete = new boolean[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    synchronized (indexingComplete) {
                        indexingComplete[0] = true;
                        indexingComplete.notifyAll();
                    }
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setWorkspaceFolders(Arrays.asList(new WorkspaceFolder(root.getFileObject("jdk/src/java.base").toURI().toString())));
        InitializeResult result = server.initialize(initParams).get();
        synchronized (indexingComplete) {
            while (!indexingComplete[0]) {
                try {
                    indexingComplete.wait();
                } catch (InterruptedException ex) {
                    //ignore...
                }
            }
        }
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(javaBaseMI.toURI().toString(), "java", 0, javaBaseMI.asText("UTF-8"))));
        assertDiags(diags);
    }
    
    public void testMarkOccurrences() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public int method(int ppp) {\n" +
                      "        if (ppp < 0) return -1;\n" +
                      "        else if (ppp > 0) return 1;\n" +
                      "        else return 0;\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        FileUtil.refreshFor(getWorkDir());
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        assertTrue(result.getCapabilities().getDocumentHighlightProvider());
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        assertHighlights(server.getTextDocumentService().documentHighlight(new DocumentHighlightParams(new TextDocumentIdentifier(toURI(src)), new Position(1, 13))).get(),
                         "<none>:2:21-2:31", "<none>:3:26-3:35", "<none>:4:13-4:22");
        assertHighlights(server.getTextDocumentService().documentHighlight(new DocumentHighlightParams(new TextDocumentIdentifier(toURI(src)), new Position(1, 27))).get(),
                         "<none>:1:26-1:29", "<none>:2:12-2:15", "<none>:3:17-3:20");
    }

    public void testHover() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "/**\n" +
                      " * This is a test class with Javadoc.\n" +
                      " */\n" +
                      "public class Test {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        Test t = new Test();\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        FileUtil.refreshFor(getWorkDir());
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        assertTrue(result.getCapabilities().getHoverProvider());
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        Hover hover = server.getTextDocumentService().hover(new HoverParams(new TextDocumentIdentifier(toURI(src)), new Position(5, 10))).get();
        assertNotNull(hover);
        assertTrue(hover.getContents().isRight());
        MarkupContent content = hover.getContents().getRight();
        assertNotNull(content);
        assertEquals(content.getKind(), "markdown");
        assertEquals(content.getValue(), "**[](*0)**\n" +
                "\n" +
                "```\n" +
                "public class Test\n" +
                "extends Object\n" +
                "```\n" +
                "\n" +
                "This is a test class with Javadoc.\n" +
                "\n");
    }

    public void testAdvancedCompletion1() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    private void t(String s) {\n" +
                      "        \n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        List<Diagnostic>[] diags = new List[1];
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(toURI(getWorkDir()));
        InitializeResult result = server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));

        {
            VersionedTextDocumentIdentifier id1 = new VersionedTextDocumentIdentifier(toURI(src), 1);
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id1, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(2, 8), new Position(2, 8)), 0, "s."))));

            Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(2, 8 + "s.".length()))).get();
            assertTrue(completion.isRight());
            Optional<CompletionItem> lengthItem = completion.getRight().getItems().stream().filter(ci -> "length() : int".equals(ci.getLabel())).findAny();
            assertTrue(lengthItem.isPresent());
            assertEquals(InsertTextFormat.PlainText, lengthItem.get().getInsertTextFormat());
            assertEquals("length()", lengthItem.get().getInsertText());
            Optional<CompletionItem> substringItem = completion.getRight().getItems().stream().filter(ci -> ci.getLabel().startsWith("substring(") && ci.getLabel().contains(",")).findAny();
            assertTrue(substringItem.isPresent());
            assertEquals(InsertTextFormat.Snippet, substringItem.get().getInsertTextFormat());
            assertEquals("substring(${1:i}, ${2:i1})$0", substringItem.get().getInsertText());
        }

        {
            VersionedTextDocumentIdentifier id2 = new VersionedTextDocumentIdentifier(toURI(src), 1);
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(1, 1), new Position(1, 1)), 0, "@java.lang."))));

            Position afterJavaLang = new Position(1, 1 + "@java.lang.".length());

            {
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), afterJavaLang)).get();
                assertTrue(completion.isRight());
                Optional<CompletionItem> annotationItem = completion.getRight().getItems().stream().filter(ci -> "annotation".equals(ci.getLabel())).findAny();
                assertTrue(annotationItem.isPresent());
                assertEquals("annotation", annotationItem.get().getLabel());
                assertEquals(CompletionItemKind.Folder, annotationItem.get().getKind());
            }

            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(afterJavaLang, afterJavaLang), 0, "annotation."))));

            Position afterJavaLangAnnotation = new Position(1, afterJavaLang.getCharacter() + "annotation.".length());

            {
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), afterJavaLangAnnotation)).get();
                assertTrue(completion.isRight());
                completion.getRight().getItems().stream().forEach(ci -> System.err.println(ci.getLabel()));
                Optional<CompletionItem> targetItem = completion.getRight().getItems().stream().filter(ci -> "Target (java.lang.annotation)".equals(ci.getLabel())).findAny();
                assertTrue(targetItem.isPresent());
                assertEquals("Target (java.lang.annotation)", targetItem.get().getLabel()); //TODO: insert text '('!
                assertEquals(CompletionItemKind.Interface, targetItem.get().getKind());
            }

            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(afterJavaLangAnnotation, afterJavaLangAnnotation), 0, "Target("))));

            Position afterTarget = new Position(1, afterJavaLangAnnotation.getCharacter() + "Target(".length());

            {
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), afterTarget)).get();
                assertTrue(completion.isRight());
                completion.getRight().getItems().stream().forEach(ci -> System.err.println(ci.getLabel()));
                Optional<CompletionItem> methodItem = completion.getRight().getItems().stream().filter(ci -> "ElementType.METHOD".equals(ci.getLabel())).findAny();
                assertTrue(methodItem.isPresent());
                assertEquals(InsertTextFormat.PlainText, methodItem.get().getInsertTextFormat());
                assertEquals("ElementType.METHOD", methodItem.get().getInsertText());
                assertEquals(1, methodItem.get().getAdditionalTextEdits().size());
                assertEquals(0, methodItem.get().getAdditionalTextEdits().get(0).getRange().getStart().getLine());
                assertEquals(0, methodItem.get().getAdditionalTextEdits().get(0).getRange().getStart().getCharacter());
                assertEquals(0, methodItem.get().getAdditionalTextEdits().get(0).getRange().getEnd().getLine());
                assertEquals(0, methodItem.get().getAdditionalTextEdits().get(0).getRange().getEnd().getCharacter());
                assertEquals("\nimport java.lang.annotation.ElementType;\n\n", methodItem.get().getAdditionalTextEdits().get(0).getNewText());
            }

            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, 0), new Position(0, 0)), 0, "import java.lang.annotation.ElementType;"))));

            {
                //import already exists:
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), afterTarget)).get();
                assertTrue(completion.isRight());
                completion.getRight().getItems().stream().forEach(ci -> System.err.println(ci.getLabel()));
                Optional<CompletionItem> methodItem = completion.getRight().getItems().stream().filter(ci -> "ElementType.METHOD".equals(ci.getLabel())).findAny();
                assertTrue(methodItem.isPresent());
                assertEquals(InsertTextFormat.PlainText, methodItem.get().getInsertTextFormat());
                assertEquals("ElementType.METHOD", methodItem.get().getInsertText());
                assertNull(methodItem.get().getAdditionalTextEdits());
            }
        }
    }

    public void testAutoImportOnCompletion() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    private void t(String s) {\n" +
                      "        \n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        List<Diagnostic>[] diags = new List[1];
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(toURI(getWorkDir()));
        server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));

        {
            VersionedTextDocumentIdentifier id1 = new VersionedTextDocumentIdentifier(toURI(src), 1);
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id1, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(2, 8), new Position(2, 8)), 0, "ArrayL"))));

            Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(2, 8 + "ArrayL".length()))).get();
            assertTrue(completion.isRight());
            Optional<CompletionItem> arrayListItem = completion.getRight().getItems().stream().filter(ci -> "ArrayList (java.util)".equals(ci.getLabel())).findAny();
            assertTrue(arrayListItem.isPresent());
            assertNull(arrayListItem.get().getAdditionalTextEdits());
            CompletableFuture<CompletionItem> resolvedItem = server.getTextDocumentService().resolveCompletionItem(arrayListItem.get());
            assertEquals(1, resolvedItem.get().getAdditionalTextEdits().size());
            assertEquals(0, resolvedItem.get().getAdditionalTextEdits().get(0).getRange().getStart().getLine());
            assertEquals(0, resolvedItem.get().getAdditionalTextEdits().get(0).getRange().getStart().getCharacter());
            assertEquals(0, resolvedItem.get().getAdditionalTextEdits().get(0).getRange().getEnd().getLine());
            assertEquals(0, resolvedItem.get().getAdditionalTextEdits().get(0).getRange().getEnd().getCharacter());
            assertEquals("\nimport java.util.ArrayList;\n\n", resolvedItem.get().getAdditionalTextEdits().get(0).getNewText());
        }

        {
            VersionedTextDocumentIdentifier id2 = new VersionedTextDocumentIdentifier(toURI(src), 1);
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, 0), new Position(0, 0)), 0, "import java.util.ArrayList;\n"))));
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(3, 8), new Position(3, 8)), 0, "ArrayL"))));

            Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(3, 8 + "ArrayL".length()))).get();
            assertTrue(completion.isRight());
            Optional<CompletionItem> arrayListItem = completion.getRight().getItems().stream().filter(ci -> "ArrayList (java.util)".equals(ci.getLabel())).findAny();
            assertTrue(arrayListItem.isPresent());
            assertNull(arrayListItem.get().getAdditionalTextEdits());
            CompletableFuture<CompletionItem> resolvedItem = server.getTextDocumentService().resolveCompletionItem(arrayListItem.get());
            assertNull(resolvedItem.get().getAdditionalTextEdits());
        }
    }

    public void testFixImports() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    private void t() {\n" +
                      "        List l;\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        List<Diagnostic>[] diags = new List[1];
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void notifyProgress(ProgressParams params) {
            }

            @Override
            public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                }
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams params) {
                throw new UnsupportedOperationException("Unexpected message.");
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setInitializationOptions(new JsonParser().parse(
                "{ nbcodeCapabilities: { statusBarMessageSupport : true } }").getAsJsonObject());
        initParams.setRootUri(toURI(getWorkDir()));
        InitializeResult result = server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));

        Diagnostic unresolvable = assertDiags(diags, "Error:2:8-2:12").get(0);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(new TextDocumentIdentifier(toURI(src)), unresolvable.getRange(), new CodeActionContext(Arrays.asList(unresolvable)))).get();
        if (jdk9Plus()) {
            assertEquals(2, codeActions.size());
        }
    }

    public void testFindUsages() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    int val = new Test2().get();\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        File src2 = new File(getWorkDir(), "Test2.java");
        try (Writer w = new FileWriter(src2)) {
            w.write("public class Test2 extends Test {\n" +
                    "    Test t;\n" +
                    "    void m(Test p) {};\n" +
                    "    int get() { return t.val; };\n" +
                    "}\n");
        }
        List<Diagnostic>[] diags = new List[1];
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(getWorkDir().toURI().toString());
        InitializeResult result = server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));

        {
            ReferenceParams params = new ReferenceParams(new TextDocumentIdentifier(toURI(src)),
                                                         new Position(0, 15),
                                                         new ReferenceContext(false));

            Set<? extends String> locations = server.getTextDocumentService().references(params).get().stream().map(this::toString).collect(Collectors.toSet());
            Set<? extends String> expected = new HashSet<>(Arrays.asList("Test2.java:1:4-1:8", "Test2.java:0:27-0:31", "Test2.java:2:11-2:15"));

            assertEquals(expected, locations);
        }

        {
            ReferenceParams params = new ReferenceParams(new TextDocumentIdentifier(toURI(src)),
                                                         new Position(0, 15),
                                                         new ReferenceContext(true));

            Set<? extends String> locations = server.getTextDocumentService().references(params).get().stream().map(this::toString).collect(Collectors.toSet());
            Set<? extends String> expected = new HashSet<>(Arrays.asList("Test2.java:1:4-1:8", "Test2.java:0:27-0:31", "Test2.java:2:11-2:15", "Test.java:0:13-0:17"));

            assertEquals(expected, locations);
        }

        {
            ReferenceParams params = new ReferenceParams(new TextDocumentIdentifier(src2.toURI().toString()),
                                                         new Position(0, 29),
                                                         new ReferenceContext(true));

            Set<? extends String> locations = server.getTextDocumentService().references(params).get().stream().map(this::toString).collect(Collectors.toSet());
            Set<? extends String> expected = new HashSet<>(Arrays.asList("Test2.java:1:4-1:8", "Test2.java:0:27-0:31", "Test2.java:2:11-2:15", "Test.java:0:13-0:17"));

            assertEquals(expected, locations);
        }

        {
            ReferenceParams params = new ReferenceParams(new TextDocumentIdentifier(src2.toURI().toString()),
                                                         new Position(3, 10),
                                                         new ReferenceContext(true));

            Set<? extends String> locations = server.getTextDocumentService().references(params).get().stream().map(this::toString).collect(Collectors.toSet());
            Set<? extends String> expected = new HashSet<>(Arrays.asList("Test.java:1:26-1:29", "Test2.java:3:8-3:11"));

            assertEquals(expected, locations);
        }

        {
            ReferenceParams params = new ReferenceParams(new TextDocumentIdentifier(src2.toURI().toString()),
                                                         new Position(3, 27),
                                                         new ReferenceContext(true));

            Set<? extends String> locations = server.getTextDocumentService().references(params).get().stream().map(this::toString).collect(Collectors.toSet());
            Set<? extends String> expected = new HashSet<>(Arrays.asList("Test.java:1:8-1:11", "Test2.java:3:25-3:28"));

            assertEquals(expected, locations);
        }
    }

    public void testWorkspaceSymbols() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    public static class TestNested {}\n" +
                      "    public static void testMethod() {}\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(toURI(getWorkDir()));
        InitializeResult result = server.initialize(initParams).get();
        indexingComplete.await();
        List<? extends SymbolInformation> symbols = server.getWorkspaceService().symbol(new WorkspaceSymbolParams("Tes")).get();
        List<String> actual = symbols.stream().map(si -> si.getKind() + ":" + si.getName() + ":" + si.getContainerName() + ":" + toString(si.getLocation())).collect(Collectors.toList());
        assertEquals(Arrays.asList("Class:Test:null:Test.java:0:13-0:17",
                                   "Constructor:Test():Test:Test.java:0:7-0:7",
                                   "Method:testMethod():Test:Test.java:2:4-2:38",
                                   "Class:TestNested:Test:Test.java:1:24-1:34",
                                   "Constructor:TestNested():Test.TestNested:Test.java:1:18-1:18"),
                     actual);
    }

    public void testCodeActionGenerateVarFieldOrParam() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public String getName() {\n" +
                      "        return name;\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 17), new Position(2, 17)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 3);
        Optional<CodeAction> generateVariable =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Create local variable \"name\"".equals(a.getTitle()))
                           .findAny();
        assertTrue(generateVariable.isPresent());
        assertEquals(generateVariable.get().getKind(), CodeActionKind.QuickFix);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = generateVariable.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(2, 0),
                               new Position(2, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("        String name;\n",
                     fileChanges.get(0).getNewText());
        Optional<CodeAction> generateField =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Create field \"name\" in Test".equals(a.getTitle()))
                           .findAny();
        assertTrue(generateField.isPresent());
        assertEquals(generateField.get().getKind(), CodeActionKind.QuickFix);
        changes = generateField.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(1, 0),
                               new Position(1, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    private String name;\n",
                     fileChanges.get(0).getNewText());
        Optional<CodeAction> generateParameter =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Create parameter \"name\"".equals(a.getTitle()))
                           .findAny();
        assertTrue(generateParameter.isPresent());
        assertEquals(generateParameter.get().getKind(), CodeActionKind.QuickFix);
        changes = generateParameter.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(1, 26),
                               new Position(1, 26)),
                     fileChanges.get(0).getRange());
        assertEquals("String name",
                     fileChanges.get(0).getNewText());
    }

    public void testCodeActionGenerateMethod() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public String get(int value) {\n" +
                      "        return convertToString(value);\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 17), new Position(2, 17)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 1);
        Optional<CodeAction> generateMehtod =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Create method \"convertToString(int)\" in Test".equals(a.getTitle()))
                           .findAny();
        assertTrue(generateMehtod.isPresent());
        assertEquals(generateMehtod.get().getKind(), CodeActionKind.QuickFix);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = generateMehtod.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(4, 0),
                               new Position(4, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    private String convertToString(int value) {\n" +
                     "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testCodeActionGenerateClass() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public Hello hello;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(1, 14), new Position(2, 14)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 2);
        Optional<CodeAction> generateClass =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Create class \"Hello\" in Test".equals(a.getTitle()))
                           .findAny();
        assertTrue(generateClass.isPresent());
        assertEquals(generateClass.get().getKind(), CodeActionKind.QuickFix);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = generateClass.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(2, 0),
                               new Position(2, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    private static class Hello {\n" +
                     "\n" +
                     "        public Hello() {\n" +
                     "        }\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testCodeActionImplementAllAbstractMethodsInClass() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test implements Runnable {\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(0, 15), new Position(0, 15)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 2);
        Optional<CodeAction> implementAllAbstractMethods =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Implement all abstract methods".equals(a.getTitle()))
                           .findAny();
        assertTrue(implementAllAbstractMethods.isPresent());
        assertEquals(implementAllAbstractMethods.get().getKind(), CodeActionKind.QuickFix);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = implementAllAbstractMethods.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(1, 0),
                               new Position(1, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    @Override\n" +
                     "    public void run() {\n" +
                     "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testCodeActionImplementAllAbstractMethodsInAnonymousClass() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        Runnable r = new Runnable() {\n" +
                      "        };" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 35), new Position(2, 35)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 1);
        Optional<CodeAction> implementAllAbstractMethods =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Implement all abstract methods".equals(a.getTitle()))
                           .findAny();
        assertTrue(implementAllAbstractMethods.isPresent());
        assertEquals(implementAllAbstractMethods.get().getKind(), CodeActionKind.QuickFix);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = implementAllAbstractMethods.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(3, 0),
                               new Position(3, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("            @Override\n" +
                     "            public void run() {\n" +
                     "                throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                     "            }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testCodeActionImplementAllAbstractMethodsInEnum() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public enum Test implements Runnable {\n" +
                      "    A {\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(1, 5), new Position(1, 5)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 2);
        Optional<CodeAction> implementAllAbstractMethods =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Implement all abstract methods".equals(a.getTitle()))
                           .findAny();
        assertTrue(implementAllAbstractMethods.isPresent());
        assertEquals(implementAllAbstractMethods.get().getKind(), CodeActionKind.QuickFix);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = implementAllAbstractMethods.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(2, 0),
                               new Position(2, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("        @Override\n" +
                     "        public void run() {\n" +
                     "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                     "        }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testCodeActionIntroduceVariable() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        System.out.println(\"Hello World\");\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 8), new Position(2, 18)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 4);
        Optional<CodeAction> introduceVariable =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Introduce Variable...".equals(a.getTitle()))
                           .findAny();
        assertTrue(introduceVariable.isPresent());
        assertEquals(introduceVariable.get().getKind(), CodeActionKind.RefactorExtract);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = introduceVariable.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(4, fileChanges.size());
        assertEquals(new Range(new Position(0, 0),
                               new Position(0, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "import java.io.PrintStream;\n" +
                     "\n",
                     fileChanges.get(0).getNewText());
        assertEquals(new Range(new Position(2, 8),
                               new Position(2, 8)),
                     fileChanges.get(1).getRange());
        assertEquals("PrintStream out = ",
                     fileChanges.get(1).getNewText());
        assertEquals(new Range(new Position(2, 18),
                               new Position(2, 41)),
                     fileChanges.get(2).getRange());
        assertEquals("",
                     fileChanges.get(2).getNewText());
        assertEquals(new Range(new Position(3, 0),
                               new Position(3, 0)),
                     fileChanges.get(3).getRange());
        assertEquals("        out.println(\"Hello World\");\n",
                     fileChanges.get(3).getNewText());
        Command command = introduceVariable.get().getCommand();
        assertNotNull(command);
        assertEquals("java.rename.element.at", command.getCommand());
        List<Object> arguments = command.getArguments();
        assertNotNull(arguments);
        assertEquals(1, arguments.size());
        assertEquals("115", arguments.get(0).toString());
    }

    public void testCodeActionIntroduceConstant() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        System.out.println(\"Hello World\");\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 8), new Position(2, 18)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 4);
        Optional<CodeAction> introduceConstant =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Introduce Constant...".equals(a.getTitle()))
                           .findAny();
        assertTrue(introduceConstant.isPresent());
        assertEquals(introduceConstant.get().getKind(), CodeActionKind.RefactorExtract);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = introduceConstant.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(3, fileChanges.size());
        assertEquals(new Range(new Position(0, 0),
                               new Position(0, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "import java.io.PrintStream;\n" +
                     "\n",
                     fileChanges.get(0).getNewText());
        assertEquals(new Range(new Position(2, 8),
                               new Position(2, 18)),
                     fileChanges.get(1).getRange());
        assertEquals("OUT",
                     fileChanges.get(1).getNewText());
        assertEquals(new Range(new Position(4, 0),
                               new Position(4, 0)),
                     fileChanges.get(2).getRange());
        assertEquals("    private static PrintStream OUT = System.out;\n",
                     fileChanges.get(2).getNewText());
        Command command = introduceConstant.get().getCommand();
        assertNotNull(command);
        assertEquals("java.rename.element.at", command.getCommand());
        List<Object> arguments = command.getArguments();
        assertNotNull(arguments);
        assertEquals(1, arguments.size());
        assertEquals("168", arguments.get(0).toString());
    }

    public void testCodeActionIntroduceField() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        System.out.println(\"Hello World\");\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 8), new Position(2, 18)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 4);
        Optional<CodeAction> introduceField =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Introduce Field...".equals(a.getTitle()))
                           .findAny();
        assertTrue(introduceField.isPresent());
        assertEquals(introduceField.get().getKind(), CodeActionKind.RefactorExtract);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = introduceField.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(3, fileChanges.size());
        assertEquals(new Range(new Position(0, 0),
                               new Position(0, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "import java.io.PrintStream;\n" +
                     "\n",
                     fileChanges.get(0).getNewText());
        assertEquals(new Range(new Position(2, 8),
                               new Position(2, 15)),
                     fileChanges.get(1).getRange());
        assertEquals("",
                     fileChanges.get(1).getNewText());
        assertEquals(new Range(new Position(4, 0),
                               new Position(4, 0)),
                     fileChanges.get(2).getRange());
        assertEquals("    private static PrintStream out = System.out;\n",
                     fileChanges.get(2).getNewText());
        Command command = introduceField.get().getCommand();
        assertNotNull(command);
        assertEquals("java.rename.element.at", command.getCommand());
        List<Object> arguments = command.getArguments();
        assertNotNull(arguments);
        assertEquals(1, arguments.size());
        assertEquals("168", arguments.get(0).toString());
    }

    public void testCodeActionIntroduceMethod() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        System.out.println(\"Hello World\");\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }

        List<Diagnostic>[] diags = new List[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 8), new Position(2, 18)), new CodeActionContext(diags[0]))).get();
        assertTrue(codeActions.size() >= 4);
        Optional<CodeAction> introduceMethod =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> "Introduce Method...".equals(a.getTitle()))
                           .findAny();
        assertTrue(introduceMethod.isPresent());
        assertEquals(introduceMethod.get().getKind(), CodeActionKind.RefactorExtract);
        List<Either<TextDocumentEdit, ResourceOperation>> changes = introduceMethod.get().getEdit().getDocumentChanges();
        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isLeft());
        TextDocumentEdit edit = changes.get(0).getLeft();
        assertEquals(edit.getTextDocument().getUri(), uri);
        List<TextEdit> fileChanges = edit.getEdits();
        assertNotNull(fileChanges);
        assertEquals(3, fileChanges.size());
        assertEquals(new Range(new Position(0, 0),
                               new Position(0, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "import java.io.PrintStream;\n" +
                     "\n",
                     fileChanges.get(0).getNewText());
        assertEquals(new Range(new Position(2, 8),
                               new Position(2, 18)),
                     fileChanges.get(1).getRange());
        assertEquals("method()",
                     fileChanges.get(1).getNewText());
        assertEquals(new Range(new Position(3, 0),
                               new Position(3, 0)),
                     fileChanges.get(2).getRange());
        assertEquals("    }\n" +
                     "\n" +
                     "    private static PrintStream method() {\n" +
                     "        return System.out;\n",
                     fileChanges.get(2).getNewText());
        Command command = introduceMethod.get().getCommand();
        assertNotNull(command);
        assertEquals("java.rename.element.at", command.getCommand());
        List<Object> arguments = command.getArguments();
        assertNotNull(arguments);
        assertEquals(1, arguments.size());
        assertEquals("174", arguments.get(0).toString());
    }

    public void testCodeActionGetterSetter() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private final String f1;\n" +
                      "    private String f2;\n" +
                      "    private final String f3;\n" +
                      "    private final String f4;\n" +
                      "    public String getF4() { return f4; }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 6), new Position(2, 6)), new CodeActionContext(Arrays.asList()))).get();
        assertEquals(4, codeActions.size());
        Optional<CodeAction> generateGetterSetter =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateGetterSetterFor("f2").equals(a.getTitle()))
                           .findAny();
        assertTrue(generateGetterSetter.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateGetterSetter.get().getCommand().getCommand(), generateGetterSetter.get().getCommand().getArguments())).get();
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(6, 0),
                               new Position(6, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    public String getF2() {\n" +
                     "        return f2;\n" +
                     "    }\n" +
                     "\n" +
                     "    public void setF2(String f2) {\n" +
                     "        this.f2 = f2;\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testCodeActionGenerateConstructor() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private final String f1;\n" +
                      "    private String f2;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                return CompletableFuture.completedFuture(params.getItems().stream().filter(item -> item.isPicked()).collect(Collectors.toList()));
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(1, 6), new Position(1, 6)), new CodeActionContext(Arrays.asList()))).get();
        assertEquals(2, codeActions.size());
        Optional<CodeAction> generateConstructor =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateConstructor().equals(a.getTitle()))
                           .findAny();
        assertTrue(generateConstructor.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateConstructor.get().getCommand().getCommand(), generateConstructor.get().getCommand().getArguments())).get();
        int cnt = 0;
        while(edit[0] == null && cnt++ < 10) {
            Thread.sleep(1000);
        }
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(3, 0),
                               new Position(3, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    public Test(String f1) {\n" +
                     "        this.f1 = f1;\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testSourceActionGetterSetter() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private final String f1;\n" +
                      "    private String f2;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(3, 0), new Position(3, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(9, codeActions.size());
        Optional<CodeAction> generateGetterSetter =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateGetterSetterFor("f2").equals(a.getTitle()))
                           .findAny();
        assertTrue(generateGetterSetter.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateGetterSetter.get().getCommand().getCommand(), generateGetterSetter.get().getCommand().getArguments())).get();
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(3, 0),
                               new Position(3, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    public String getF2() {\n" +
                     "        return f2;\n" +
                     "    }\n" +
                     "\n" +
                     "    public void setF2(String f2) {\n" +
                     "        this.f2 = f2;\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(fileChanges.get(0).getRange(), 0, fileChanges.get(0).getNewText()))));
        codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(3, 0), new Position(3, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(7, codeActions.size());
        Optional<CodeAction> generateGetter =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateGetterFor("f1").equals(a.getTitle()))
                           .findAny();
        assertTrue(generateGetter.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateGetter.get().getCommand().getCommand(), generateGetter.get().getCommand().getArguments())).get();
        assertEquals(1, edit[0].getChanges().size());
        fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(11, 0),
                               new Position(11, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    public String getF1() {\n" +
                     "        return f1;\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testSourceActionConstructor() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test extends Exception {\n" +
                      "    private final String f1;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                return CompletableFuture.completedFuture(params.getItems().size() > 2 ? params.getItems().subList(0, 2) : params.getItems());
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 0), new Position(2, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(7, codeActions.size());
        Optional<CodeAction> generateConstructor =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateConstructor().equals(a.getTitle()))
                           .findAny();
        assertTrue(generateConstructor.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateConstructor.get().getCommand().getCommand(), generateConstructor.get().getCommand().getArguments())).get();
        int cnt = 0;
        while(edit[0] == null && cnt++ < 10) {
            Thread.sleep(1000);
        }
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(2, 0),
                               new Position(2, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    public Test(String f1) {\n" +
                     "        this.f1 = f1;\n" +
                     "    }\n" +
                     "\n" +
                     "    public Test(String f1, String string) {\n" +
                     "        super(string);\n" +
                     "        this.f1 = f1;\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testSourceActionEqualsHashCode() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "import java.util.Objects;\n" +
                      "\n" +
                      "public class Test {\n" +
                      "    private final String f1;\n" +
                      "    private java.util.List<String> f2;\n" +
                      "\n" +
                      "    @Override\n" +
                      "    public int hashCode() {\n" +
                      "        int hash = 3;\n" +
                      "        hash = 71 * hash + Objects.hashCode(this.f1);\n" +
                      "        hash = 71 * hash + Objects.hashCode(this.f2);\n" +
                      "        return hash;\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                return CompletableFuture.completedFuture(params.getItems().size() > 2 ? params.getItems().subList(0, 2) : params.getItems());
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(5, 0), new Position(5, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(9, codeActions.size());
        Optional<CodeAction> generateEquals =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateEquals().equals(a.getTitle()))
                           .findAny();
        assertTrue(generateEquals.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateEquals.get().getCommand().getCommand(), generateEquals.get().getCommand().getArguments())).get();
        int cnt = 0;
        while(edit[0] == null && cnt++ < 10) {
            Thread.sleep(1000);
        }
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(13, 0),
                               new Position(13, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    @Override\n" +
                     "    public boolean equals(Object obj) {\n" +
                     "        if (this == obj) {\n" +
                     "            return true;\n" +
                     "        }\n" +
                     "        if (obj == null) {\n" +
                     "            return false;\n" +
                     "        }\n" +
                     "        if (getClass() != obj.getClass()) {\n" +
                     "            return false;\n" +
                     "        }\n" +
                     "        final Test other = (Test) obj;\n" +
                     "        if (!Objects.equals(this.f1, other.f1)) {\n" +
                     "            return false;\n" +
                     "        }\n" +
                     "        if (!Objects.equals(this.f2, other.f2)) {\n" +
                     "            return false;\n" +
                     "        }\n" +
                     "        return true;\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testSourceActionToString() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private final String f1;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                return CompletableFuture.completedFuture(params.getItems().size() > 2 ? params.getItems().subList(0, 2) : params.getItems());
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 0), new Position(2, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(7, codeActions.size());
        Optional<CodeAction> generateToString =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateToString().equals(a.getTitle()))
                           .findAny();
        assertTrue(generateToString.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateToString.get().getCommand().getCommand(), generateToString.get().getCommand().getArguments())).get();
        int cnt = 0;
        while(edit[0] == null && cnt++ < 10) {
            Thread.sleep(1000);
        }
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(2, 0),
                               new Position(2, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    @Override\n" +
                     "    public String toString() {\n" +
                     "        StringBuilder sb = new StringBuilder();\n" +
                     "        sb.append(\"Test{f1=\").append(f1);\n" +
                     "        sb.append('}');\n" +
                     "        return sb.toString();\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testSourceActionDelegateMethod() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private final String f1;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                return CompletableFuture.completedFuture(params.getItems().size() > 2 ? params.getItems().subList(0, 2) : params.getItems());
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 0), new Position(2, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(7, codeActions.size());
        Optional<CodeAction> generateDelegateMethod =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateDelegateMethod().equals(a.getTitle()))
                           .findAny();
        assertTrue(generateDelegateMethod.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateDelegateMethod.get().getCommand().getCommand(), generateDelegateMethod.get().getCommand().getArguments())).get();
        int cnt = 0;
        while(edit[0] == null && cnt++ < 10) {
            Thread.sleep(1000);
        }
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(2, fileChanges.size());
        assertEquals(new Range(new Position(0, 0),
                               new Position(0, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\nimport java.util.stream.IntStream;\n\n",
                     fileChanges.get(0).getNewText());
        assertEquals(new Range(new Position(2, 0),
                               new Position(2, 0)),
                     fileChanges.get(1).getRange());
        assertEquals("\n" +
                     "    public IntStream chars() {\n" +
                     "        return f1.chars();\n" +
                     "    }\n" +
                     "\n" +
                     "    public IntStream codePoints() {\n" +
                     "        return f1.codePoints();\n" +
                     "    }\n",
                     fileChanges.get(1).getNewText());
    }

    public void testSourceActionOverrideMethod() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private final String f1;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                return CompletableFuture.completedFuture(params.getItems().size() > 2 ? params.getItems().subList(0, 2) : params.getItems());
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 0), new Position(2, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(7, codeActions.size());
        Optional<CodeAction> generateOverrideMethod =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateOverrideMethod().equals(a.getTitle()))
                           .findAny();
        assertTrue(generateOverrideMethod.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateOverrideMethod.get().getCommand().getCommand(), generateOverrideMethod.get().getCommand().getArguments())).get();
        int cnt = 0;
        while(edit[0] == null && cnt++ < 10) {
            Thread.sleep(1000);
        }
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(1, fileChanges.size());
        assertEquals(new Range(new Position(2, 0),
                               new Position(2, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\n" +
                     "    @Override\n" +
                     "    protected void finalize() throws Throwable {\n" +
                     "        super.finalize(); //To change body of generated methods, choose Tools | Templates.\n" +
                     "    }\n" +
                     "\n" +
                     "    @Override\n" +
                     "    public String toString() {\n" +
                     "        return super.toString(); //To change body of generated methods, choose Tools | Templates.\n" +
                     "    }\n",
                     fileChanges.get(0).getNewText());
    }

    public void testSourceActionLogger() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private final String f1;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        WorkspaceEdit[] edit = new WorkspaceEdit[1];
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new NbCodeLanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                edit[0] = params.getEdit();
                return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
            }

            @Override
            public void showStatusBarMessage(ShowStatusMessageParams params) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
                return CompletableFuture.completedFuture(params.getItems().size() > 2 ? params.getItems().subList(0, 2) : params.getItems());
            }

            @Override
            public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
                return CompletableFuture.completedFuture("LOGGER");
            }

            @Override
            public void notifyTestProgress(TestProgressParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public NbCodeClientCapabilities getNbCodeCapabilities() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disposeTextEditorDecoration(String params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        server.initialize(new InitializeParams()).get();
        String uri = src.toURI().toString();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "java", 0, code)));
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(2, 0), new Position(2, 0)), new CodeActionContext(Arrays.asList(), Arrays.asList(CodeActionKind.Source)))).get();
        assertEquals(7, codeActions.size());
        Optional<CodeAction> generateLogger =
                codeActions.stream()
                           .filter(Either::isRight)
                           .map(Either::getRight)
                           .filter(a -> Bundle.DN_GenerateLogger().equals(a.getTitle()))
                           .findAny();
        assertTrue(generateLogger.isPresent());
        server.getWorkspaceService().executeCommand(new ExecuteCommandParams(generateLogger.get().getCommand().getCommand(), generateLogger.get().getCommand().getArguments())).get();
        int cnt = 0;
        while(edit[0] == null && cnt++ < 10) {
            Thread.sleep(1000);
        }
        assertEquals(1, edit[0].getChanges().size());
        List<TextEdit> fileChanges = edit[0].getChanges().get(uri);
        assertNotNull(fileChanges);
        assertEquals(2, fileChanges.size());
        assertEquals(new Range(new Position(0, 0),
                               new Position(0, 0)),
                     fileChanges.get(0).getRange());
        assertEquals("\nimport java.util.logging.Logger;\n\n",
                     fileChanges.get(0).getNewText());
        assertEquals(new Range(new Position(1, 0),
                               new Position(1, 0)),
                     fileChanges.get(1).getRange());
        assertEquals("\n" +
                     "    private static final Logger LOGGER = Logger.getLogger(Test.class.getName());\n",
                     fileChanges.get(1).getNewText());
    }

    public void testRenameDocumentChangesCapabilitiesRenameOp() throws Exception {
        doTestRename(init -> {
                        WorkspaceEditCapabilities wec = new WorkspaceEditCapabilities();
                        wec.setDocumentChanges(true);
                        wec.setResourceOperations(Arrays.asList("rename"));
                        WorkspaceClientCapabilities wcc = new WorkspaceClientCapabilities();
                        wcc.setWorkspaceEdit(wec);
                        init.setCapabilities(new ClientCapabilities(wcc, null, null));
                     },
                     cf -> {
                         WorkspaceEdit edit = cf.get();
                         assertTrue(edit.getChanges().isEmpty());
                         Set<String> actual = edit.getDocumentChanges().stream().map(this::toString).collect(Collectors.toSet());
                         Set<String> expected = new HashSet<>(Arrays.asList("Test2.java:[3:25-3:28=>nue]", "Test2.java:[1:8-1:11=>nue]"));
                         assertEquals(expected, actual);
                     },
                     cf -> {
                         WorkspaceEdit edit = cf.get();
                         assertTrue(edit.getChanges().isEmpty());
                         Set<String> actual = edit.getDocumentChanges().stream().map(this::toString).collect(Collectors.toSet());
                         Set<String> expected = new HashSet<>(Arrays.asList("Test.java:[0:27-0:31=>TestNew, 1:4-1:8=>TestNew, 2:11-2:15=>TestNew]", "Test.java:[0:13-0:17=>TestNew]", "Test.java=>TestNew.java"));
                         assertEquals(expected, actual);
                     });
    }
    
    public void testRenameDocumentChangesCapabilitiesNoRenameOp() throws Exception {
        doTestRename(init -> {
                        WorkspaceEditCapabilities wec = new WorkspaceEditCapabilities();
                        wec.setDocumentChanges(true);
                        WorkspaceClientCapabilities wcc = new WorkspaceClientCapabilities();
                        wcc.setWorkspaceEdit(wec);
                        init.setCapabilities(new ClientCapabilities(wcc, null, null));
                     },
                     cf -> {
                         WorkspaceEdit edit = cf.get();
                         assertTrue(edit.getChanges().isEmpty());
                         Set<String> actual = edit.getDocumentChanges().stream().map(this::toString).collect(Collectors.toSet());
                         Set<String> expected = new HashSet<>(Arrays.asList("Test2.java:[3:25-3:28=>nue]", "Test2.java:[1:8-1:11=>nue]"));
                         assertEquals(expected, actual);
                     },
                     cf -> {
                         WorkspaceEdit edit = cf.get();
                         assertTrue(edit.getChanges().isEmpty());
                         Set<String> actual = edit.getDocumentChanges().stream().map(this::toString).collect(Collectors.toSet());
                         Set<String> expected = new HashSet<>(Arrays.asList("Test.java:[0:27-0:31=>TestNew, 1:4-1:8=>TestNew, 2:11-2:15=>TestNew]", "Test.java:[0:13-0:17=>TestNew]", "Test.java=>TestNew.java"));
                         assertEquals(expected, actual);
                     });
    }
    
    private void doTestRename(Consumer<InitializeParams> settings,
                              Validator<CompletableFuture<WorkspaceEdit>> validateFieldRename,
                              Validator<CompletableFuture<WorkspaceEdit>> validateClassRename) throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        try (Writer w = new FileWriter(new File(src.getParentFile(), ".test-project"))) {}
        String code = "public class Test {\n" +
                      "    int val = new Test2().get();\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        File src2 = new File(getWorkDir(), "Test2.java");
        try (Writer w = new FileWriter(src2)) {
            w.write("public class Test2 extends Test {\n" +
                    "    Test t;\n" +
                    "    void m(Test p) {};\n" +
                    "    int get() { return t.val; };\n" +
                    "}\n");
        }
        List<Diagnostic>[] diags = new List[1];
        CountDownLatch indexingComplete = new CountDownLatch(1);
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (diags) {
                    diags[0] = params.getDiagnostics();
                    diags.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams params) {
                if (Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    indexingComplete.countDown();
                } else {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initParams = new InitializeParams();
        initParams.setRootUri(getWorkDir().toURI().toString());
        settings.accept(initParams);
        InitializeResult result = server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));

        {
            RenameParams params = new RenameParams(new TextDocumentIdentifier(src2.toURI().toString()),
                                                   new Position(3, 27),
                                                   "nue");

            validateFieldRename.validate(server.getTextDocumentService().rename(params));
        }

        {
            RenameParams params = new RenameParams(new TextDocumentIdentifier(src.toURI().toString()),
                                                   new Position(0, 15),
                                                   "TestNew");

            validateClassRename.validate(server.getTextDocumentService().rename(params));
        }

    }

    public void testNoErrorAndHintsFor() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    private String field;\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        File otherSrc = new File(getWorkDir(), "Other.java");
        try (Writer w = new FileWriter(otherSrc)) {
            w.write("/**Some source*/\n" +
                    "public class Other {\n" +
                    "    public void test() { }\n" +
                    "}");
        }
        Map<String, List<Integer>> publishedDiagnostics = new HashMap<>();
        FileUtil.refreshFor(getWorkDir());
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
                synchronized (publishedDiagnostics) {
                    publishedDiagnostics.computeIfAbsent(params.getUri(), uri -> new ArrayList<>())
                                        .add(params.getDiagnostics().size());
                    publishedDiagnostics.notifyAll();
                }
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        Position pos = new Position(1, 14);
        List<? extends Location> definition = server.getTextDocumentService().definition(new DefinitionParams(new TextDocumentIdentifier(toURI(src)), pos)).get().getLeft();
        assertEquals(1, definition.size());
        String jlStringURI = definition.get(0).getUri();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(jlStringURI, "java", 0, URLMapper.findFileObject(new URL(jlStringURI)).asText())));
        String otherSrcURI = toURI(otherSrc);
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(otherSrcURI, "java", 0, FileUtil.toFileObject(otherSrc).asText())));
        synchronized (publishedDiagnostics) {
            while (publishedDiagnostics.get(otherSrcURI) == null || publishedDiagnostics.get(otherSrcURI).size() != 2) {
                publishedDiagnostics.wait();
            }
        }
        assertEquals(Arrays.asList(0, 0), publishedDiagnostics.get(jlStringURI));
    }

    public void testCodeFolding() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "/*\n" +
                      " * comment\n" +
                      " */\n" +
                      "import java.util.List;\n" +
                      "import java.util.Set;\n" +
                      "public class Test {\n" +
                      "    /**\n" +
                      "     * javadoc\n" +
                      "     */\n" +
                      "    public void test() {\n" +
                      "        return s.toString();\n" +
                      "    }\n" +
                      "    public static class Test {\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LanguageClient() {
            @Override
            public void telemetryEvent(Object arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams arg0) {
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void logMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        assertNotNull(result.getCapabilities().getFoldingRangeProvider());
        assertTrue(result.getCapabilities().getFoldingRangeProvider().isRight());
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        List<FoldingRange> folds = server.getTextDocumentService().foldingRange(new FoldingRangeRequestParams(new TextDocumentIdentifier(toURI(src)))).get();

        assertEquals(5, folds.size());

        assertEquals(0, folds.get(0).getStartLine());
        assertEquals(0, (int) folds.get(0).getStartCharacter());
        assertEquals(2, folds.get(0).getEndLine());
        assertEquals(3, (int) folds.get(0).getEndCharacter());
        assertEquals("comment", folds.get(0).getKind());

        assertEquals(3, folds.get(1).getStartLine());
        assertEquals(7, (int) folds.get(1).getStartCharacter());
        assertEquals(4, folds.get(1).getEndLine());
        assertEquals(21, (int) folds.get(1).getEndCharacter());
        assertEquals("imports", folds.get(1).getKind());

        assertEquals(9, folds.get(2).getStartLine());
        assertEquals(23, (int) folds.get(2).getStartCharacter());
        assertEquals(11, folds.get(2).getEndLine());
        assertEquals(5, (int) folds.get(2).getEndCharacter());
        assertEquals("region", folds.get(2).getKind());

        assertEquals(6, folds.get(3).getStartLine());
        assertEquals(4, (int) folds.get(3).getStartCharacter());
        assertEquals(8, folds.get(3).getEndLine());
        assertEquals(7, (int) folds.get(3).getEndCharacter());
        assertEquals("comment", folds.get(3).getKind());

        assertEquals(12, folds.get(4).getStartLine());
        assertEquals(29, (int) folds.get(4).getStartCharacter());
        assertEquals(13, folds.get(4).getEndLine());
        assertEquals(5, (int) folds.get(4).getEndCharacter());
        assertEquals("region", folds.get(4).getKind());
    }

    public void testAnnotationCompletion() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "@java.lang.Supp public class Test { }";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient(), client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, 15))).get();
        assertTrue(completion.isRight());
        List<String> actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Interface:SuppressWarnings (java.lang)"), actualItems);
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(toURI(src));
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, 1), new Position(0, 15)), 14, "SuppressWarnings(v"))));
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, 19))).get();
        actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertTrue(actualItems.contains("Property:value"));
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, 19), new Position(0, 19)), 0, "alue=\"\""))));
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, 25))).get();
        actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertTrue(actualItems.contains("Text:\"empty-statement\""));
    }

    interface Validator<T> {
        public void validate(T t) throws Exception;
    }

    private String toString(Either<TextDocumentEdit, ResourceOperation> e) {
        if (e.isLeft()) {
            TextDocumentEdit ted = e.getLeft();
            VersionedTextDocumentIdentifier td = ted.getTextDocument();

            return toString(td) + ":" + ted.getEdits().stream().map(this::toString).collect(Collectors.joining(", ", "[", "]"));
        } else {
            switch (e.getRight().getKind()) {
                case "rename":
                    RenameFile rf = (RenameFile) e.getRight();
                    return uriToString(rf.getOldUri()) + "=>" + uriToString(rf.getNewUri());
                default:
                    throw new IllegalStateException(e.getRight().getKind());
            }
        }
    }

    private String toString(VersionedTextDocumentIdentifier td) {
        return uriToString(td.getUri())/* + "(" + td.getVersion() + ")"*/;
    }

    private String toString(TextEdit edit) {
        return toString(edit.getRange()) + "=>" + edit.getNewText();
    }

    private String toString(Location location) {
        String path = location.getUri();
        String simpleName = path.substring(path.lastIndexOf('/') + 1);
        return simpleName + ":" + toString(location.getRange());
    }

    private String uriToString(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    private String toString(Range range) {
        return       range.getStart().getLine() + ":" + range.getStart().getCharacter() +
               "-" + range.getEnd().getLine() + ":" + range.getEnd().getCharacter();
    }

    private void assertHighlights(List<? extends DocumentHighlight> highlights, String... expected) {
        Set<String> stringHighlights = new HashSet<>();
        for (DocumentHighlight h : highlights) {
            DocumentHighlightKind kind = h.getKind();
            stringHighlights.add((kind != null ? kind.name() : "<none>") + ":" +
                                 h.getRange().getStart().getLine() + ":" + h.getRange().getStart().getCharacter() + "-" +
                                 h.getRange().getEnd().getLine() + ":" + h.getRange().getEnd().getCharacter());
        }
        assertEquals(new HashSet<>(Arrays.asList(expected)),
                     stringHighlights);
    }

    private String toURI(File f) {
        return Utilities.toURI(f).toString();
    }

    private static boolean jdk9Plus() {
        String version = System.getProperty("java.version");
        if (version == null || version.startsWith("1.")) {
            return false;
        }
        return true;
    }

    //make sure files can access other files in the same directory:
    @ServiceProvider(service=ClassPathProvider.class, position=100)
    public static final class ClassPathProviderImpl implements ClassPathProvider {

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.SOURCE.equals(type) && file.isData()) {
                return ClassPathSupport.createClassPath(file.getParent());
            }
            if (ClassPath.BOOT.equals(type)) {
                return BootClassPathUtil.getBootClassPath();
            }
            return null;
        }

    }

    @ServiceProvider(service=AnnotationProcessingQueryImplementation.class, position=100)
    public static final class AnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation {

        private final Result result = new Result() {
            @Override
            public Set<? extends Trigger> annotationProcessingEnabled() {
                return EnumSet.allOf(Trigger.class);
            }

            @Override
            public Iterable<? extends String> annotationProcessorsToRun() {
                return Collections.emptyList();
            }
            @Override
            public URL sourceOutputDirectory() {
                return null;
            }
            @Override
            public Map<? extends String, ? extends String> processorOptions() {
                return Collections.emptyMap();
            }
            @Override
            public void addChangeListener(ChangeListener l) {
            }
            @Override
            public void removeChangeListener(ChangeListener l) {
            }
        };

        @Override
        public Result getAnnotationProcessingOptions(FileObject file) {
            return result;
        }

    }

    //tests may run as a project, so that indexing works properly:
    @ServiceProvider(service=ProjectFactory.class)
    public static class TestProjectFactory implements ProjectFactory {

        @Override
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject(".test-project") != null;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory)) {
                ClassPath source = ClassPathSupport.createClassPath(projectDirectory);
                Lookup lookup = Lookups.fixed(new ProjectOpenedHook() {
                        @Override
                        protected void projectOpened() {
                            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {source});
                        }

                        @Override
                        protected void projectClosed() {
                            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {source});
                        }
                    }, new ClassPathProvider() {
                        @Override
                        public ClassPath findClassPath(FileObject file, String type) {
                            switch (type) {
                                case ClassPath.SOURCE: return source;
                                case ClassPath.BOOT: return BootClassPathUtil.getBootClassPath();
                            }
                            return null;
                        }
                    }
                );
                return new Project() {
                    @Override
                    public FileObject getProjectDirectory() {
                        return projectDirectory;
                    }

                    @Override
                    public Lookup getLookup() {
                        return lookup;
                    }
                };
            }
            return null;
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }
    }
    
    public void testCancelProgressHandle() throws Exception {
        
        class LC extends LspClient {
            CountDownLatch progressStart = new CountDownLatch(1);
            CountDownLatch progressEnd = new CountDownLatch(1);
            
            volatile String token;
            volatile int perCent;
            
            @Override
            public void notifyProgress(ProgressParams params) {
                assertEquals(token, params.getToken().getLeft());
                if (params.getValue() instanceof WorkDoneProgressReport) {
                    WorkDoneProgressReport rep = (WorkDoneProgressReport)params.getValue();
                    perCent = Math.max(perCent, rep.getPercentage());
                }
                if (params.getValue().getKind() == WorkDoneProgressKind.end) {
                    progressEnd.countDown();
                }
            }

            @Override
            public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
                assertNull(params.getToken().getRight());
                assertNotNull(params.getToken().getLeft());
                token = params.getToken().getLeft();
                progressStart.countDown();
                return CompletableFuture.completedFuture(null);
            }
            
            @Override
            public void publishDiagnostics(PublishDiagnosticsParams params) {
            }

            @Override
            public void showMessage(MessageParams params) {
                if (!Server.INDEXING_COMPLETED.equals(params.getMessage())) {
                    throw new UnsupportedOperationException("Unexpected message.");
                }
            }
        };
        LC lc = new LC();
        File wdBase = getWorkDir();
        Path srcDir = Files.createDirectories(wdBase.toPath().resolve(Paths.get("src", "main", "java")));
        Files.write(srcDir.resolve("Test.java"), Arrays.asList(
                "public class Test { }"
        ));
        
        Path pomFile = wdBase.toPath().resolve("pom.xml");
        Files.write(pomFile, Arrays.asList(
            "<project xmlns='http://maven.apache.org/POM/4.0.0'>",
            "   <modelVersion>4.0.0</modelVersion>",
            "   <artifactId>m</artifactId>" +
            "   <groupId>g</groupId>" +
            "   <version>1.0-SNAPSHOT</version>" +
            "</project>"
        ));
        // force initialization, so that System.err / out are captured in their original state.
        
        IOProvider prov = IOProvider.getDefault();
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(lc, client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeParams initP = new InitializeParams();
        WorkspaceFolder wf = new WorkspaceFolder(wdBase.toURI().toString());
        initP.setWorkspaceFolders(Collections.singletonList(wf));
        InitializeResult result = server.initialize(initP).get();
        
        // now invoke the build
        ExecuteCommandParams ecp = new ExecuteCommandParams();
        ecp.setCommand("java.build.workspace");
        CompletableFuture<Object> buildF = server.getWorkspaceService().executeCommand(ecp);
        lc.progressStart.await();
        // let's cancel in the middle
        assertNotNull(lc.token);
        server.cancelProgress(new WorkDoneProgressCancelParams(Either.forLeft(lc.token)));
        lc.progressEnd.await();
        
        // and finally check that the build interrupted before reaching 100%
        assertTrue(lc.perCent < 100);
    }

    public void testFileModificationDiags() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test {\n" +
                      "    public void run(String str) {\n" +
                      "        System.err.println(1);\n" +
                      "        String s = str.substring(0);\n" +
                      "    }\n" +
                      "}\n";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient(), client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "java", 0, code)));
        assertDiags(diags);//errors
        assertDiags(diags, "Warning:3:15-3:16");//hints
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(toURI(src), 0);
        CountDownLatch waitForErrorLatch = new CountDownLatch(1);
        JavaErrorProvider.computeDiagsCallback = key -> {
            if (ErrorProvider.Kind.ERRORS == key) {
                waitForErrorLatch.countDown();
                server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(2, 27), new Position(2, 28)), 1, "1"))));
                JavaErrorProvider.computeDiagsCallback = null;
            }
        };
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(2, 27), new Position(2, 27)), 0, "d"))));
        assertDiags(diags, "Warning:3:15-3:16");//errors
        assertDiags(diags, "Warning:3:15-3:16");//hints
        //verify no more diags coming:
        synchronized (diags) {
            long timeout = 1000;
            long start = System.currentTimeMillis();
            while (diags[0] == null && (System.currentTimeMillis() - start) < timeout) {
                try {
                    diags.wait(timeout / 10);
                } catch (InterruptedException ex) {
                    //ignore
                }
            }
            assertNull(diags[0]);
        }
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(2, 1), new Position(2, 1)), 0, "    \n    "))));
        assertDiags(diags, "Warning:4:15-4:16");//errors
        assertDiags(diags, "Warning:4:15-4:16");//hints
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(4, 1), new Position(4, 1)), 0, " "))));
        assertDiags(diags, "Warning:4:16-4:17");//errors
        assertDiags(diags, "Warning:4:16-4:17");//hints
    }

    public void testDeclarativeHints() throws Exception {
        File src = new File(getWorkDir(), "test.hint");
        src.getParentFile().mkdirs();
        String code = "$1.length();;";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LspClient(), client.getInputStream(), client.getOutputStream());
        serverLauncher.startListening();
        LanguageServer server = serverLauncher.getRemoteProxy();
        InitializeResult result = server.initialize(new InitializeParams()).get();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(toURI(src), "jackpot-hint", 0, code)));
        assertDiags(diags, "Error:0:0-0:2");//errors
        assertDiags(diags, "Error:0:0-0:2");//hints
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(toURI(src));
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, 11), new Position(0, 11)), 0, " :: $1 instanceof java.lang.String"))));
        Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(toURI(src)), new Position(0, 5))).get();
        assertTrue(completion.isRight());
        List<String> actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Method:length() : int"), actualItems);
    }

    static {
        System.setProperty("SourcePath.no.source.filter", "true");
    }
}
