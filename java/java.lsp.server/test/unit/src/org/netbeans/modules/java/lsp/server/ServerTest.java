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
package org.netbeans.modules.java.lsp.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class ServerTest extends NbTestCase {

    private Socket client;
    private Thread serverThread;

    public ServerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
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
                CacheFolderProvider.getCacheFolderForRoot(Places.getUserDirectory().toURI().toURL(), EnumSet.noneOf(CacheFolderProvider.Kind.class), CacheFolderProvider.Mode.EXISTENT);

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
        serverThread.stop();
    }

    public void testMain() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test { int i = \"\".hashCode(); public void run() { this.test(); } /**Test.*/public void test() {} }";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        List<Diagnostic>[] diags = new List[1];
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
            public void showMessage(MessageParams arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
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
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));
        assertDiags(diags);//errors
        assertDiags(diags);//hints
        int hashCodeStart = code.indexOf("hashCode");
        Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(0, hashCodeStart + 2))).get();
        assertTrue(completion.isRight());
        List<String> actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Method:hashCode() : int"), actualItems);
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(src.toURI().toString());
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, hashCodeStart), new Position(0, hashCodeStart + "hashCode".length())), "hashCode".length(), "equ"))));
        assertDiags(diags, "Error:0:31-0:34");//errors
        assertDiags(diags, "Error:0:31-0:34");//hints
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(0, hashCodeStart + 2))).get();
        actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Method:equals(Object anObject) : boolean", "Method:equalsIgnoreCase(String anotherString) : boolean"), actualItems);
        int testStart = code.indexOf("test") + "equ".length() - "hashCode".length();
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(0, testStart + 3))).get();
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
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(0, 0))).get();
        actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertTrue(actualItems.contains("Keyword:interface"));
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, hashCodeStart), new Position(0, hashCodeStart + "equ".length())), "equ".length(), "hashCode"))));
        int closingBrace = code.lastIndexOf("}");
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, closingBrace), new Position(0, closingBrace)), 0, "public String c(Object o) {\nreturn o;\n}"))));
        List<Diagnostic> diagnostics = assertDiags(diags, "Error:1:0-1:9"); //errors
        assertDiags(diags, "Error:1:0-1:9");//hints
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(1, 0), new Position(1, 9)), new CodeActionContext(Arrays.asList(diagnostics.get(0))))).get();
        String log = codeActions.toString();
        assertEquals(log, 2, codeActions.size());
        assertTrue(log, codeActions.get(0).isRight());
        CodeAction action = codeActions.get(0).getRight();
        assertEquals("Cast ...o to String", action.getTitle());
        assertEquals(1, action.getEdit().getDocumentChanges().size());
        assertEquals(1, action.getEdit().getDocumentChanges().get(0).getEdits().size());
        TextEdit edit = action.getEdit().getDocumentChanges().get(0).getEdits().get(0);
        assertEquals(1, edit.getRange().getStart().getLine());
        assertEquals(7, edit.getRange().getStart().getCharacter());
        assertEquals(1, edit.getRange().getEnd().getLine());
        assertEquals(7, edit.getRange().getEnd().getCharacter());
        assertEquals("(String) ", edit.getNewText());
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, closingBrace), new Position(0, closingBrace)), 0, "public  void assignToSelf(Object o) { o = o; }"))));
        assertDiags(diags, "Error:1:0-1:9");//errors
        assertDiags(diags, "Error:1:0-1:9", "Warning:0:148-0:153", "Warning:0:152-0:153");//hints
    }

    public void testCodeActionWithRemoval() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test { public String c(String s) {\nreturn s.toString();\n} }";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        List<Diagnostic>[] diags = new List[1];
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
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));
        assertDiags(diags); //errors
        List<Diagnostic> diagnostics = assertDiags(diags, "Warning:1:7-1:19");//hints
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(src.toURI().toString());
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(id, new Range(new Position(1, 7), new Position(1, 19)), new CodeActionContext(Arrays.asList(diagnostics.get(0))))).get();
        String log = codeActions.toString();
        assertEquals(log, 1, codeActions.size());
        assertTrue(log, codeActions.get(0).isRight());
        CodeAction action = codeActions.get(0).getRight();
        assertEquals("Remove .toString()", action.getTitle());
        assertEquals(1, action.getEdit().getDocumentChanges().size());
        assertEquals(1, action.getEdit().getDocumentChanges().get(0).getEdits().size());
        TextEdit edit = action.getEdit().getDocumentChanges().get(0).getEdits().get(0);
        assertEquals(1, edit.getRange().getStart().getLine());
        assertEquals(8, edit.getRange().getStart().getCharacter());
        assertEquals(1, edit.getRange().getEnd().getLine());
        assertEquals(19, edit.getRange().getEnd().getCharacter());
        assertEquals("", edit.getNewText());
    }

    private List<Diagnostic> assertDiags(List<Diagnostic>[] diags, String... expected) {
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                    //ignore
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));
        List<Either<SymbolInformation, DocumentSymbol>> symbols = server.getTextDocumentService().documentSymbol(new DocumentSymbolParams(new TextDocumentIdentifier(src.toURI().toString()))).get();
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
                          "]:(Method:innerMethod:Range [\n" +
                          "  start = Position [\n" +
                          "    line = 5\n" +
                          "    character = 8\n" +
                          "  ]\n" +
                          "  end = Position [\n" +
                          "    line = 6\n" +
                          "    character = 9\n" +
                          "  ]\n" +
                          "]:()), Field:field:Range [\n" +
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));
        Position pos = new Position(3, 30);
        List<? extends Location> definition = server.getTextDocumentService().definition(new TextDocumentPositionParams(new TextDocumentIdentifier(src.toURI().toString()), pos)).get();
        assertEquals(1, definition.size());
        assertEquals(src.toURI().toString(), definition.get(0).getUri());
        assertEquals(1, definition.get(0).getRange().getStart().getLine());
        assertEquals(4, definition.get(0).getRange().getStart().getCharacter());
        assertEquals(1, definition.get(0).getRange().getEnd().getLine());
        assertEquals(22, definition.get(0).getRange().getEnd().getCharacter());
        pos = new Position(4, 30);
        definition = server.getTextDocumentService().definition(new TextDocumentPositionParams(new TextDocumentIdentifier(src.toURI().toString()), pos)).get();
        assertEquals(1, definition.size());
        assertEquals(src.toURI().toString(), definition.get(0).getUri());
        assertEquals(2, definition.get(0).getRange().getStart().getLine());
        assertEquals(23, definition.get(0).getRange().getStart().getCharacter());
        assertEquals(2, definition.get(0).getRange().getEnd().getLine());
        assertEquals(30, definition.get(0).getRange().getEnd().getCharacter());
        pos = new Position(5, 22);
        definition = server.getTextDocumentService().definition(new TextDocumentPositionParams(new TextDocumentIdentifier(src.toURI().toString()), pos)).get();
        assertEquals(1, definition.size());
        assertEquals(otherSrc.toURI().toString(), definition.get(0).getUri());
        assertEquals(2, definition.get(0).getRange().getStart().getLine());
        assertEquals(4, definition.get(0).getRange().getStart().getCharacter());
        assertEquals(2, definition.get(0).getRange().getEnd().getLine());
        assertEquals(26, definition.get(0).getRange().getEnd().getCharacter());
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
        assertTrue(result.getCapabilities().getDocumentHighlightProvider());
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));
        assertHighlights(server.getTextDocumentService().documentHighlight(new TextDocumentPositionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(1, 13))).get(),
                         "<none>:2:21-2:31", "<none>:3:26-3:35", "<none>:4:13-4:22");
        assertHighlights(server.getTextDocumentService().documentHighlight(new TextDocumentPositionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(1, 27))).get(),
                         "<none>:1:26-1:29", "<none>:2:12-2:15", "<none>:3:17-3:20");
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
        InitializeResult result = server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));

        {
            VersionedTextDocumentIdentifier id1 = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id1, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(2, 8), new Position(2, 8)), 0, "s."))));

            Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(2, 8 + "s.".length()))).get();
            assertTrue(completion.isRight());
            Optional<CompletionItem> lengthItem = completion.getRight().getItems().stream().filter(ci -> "length() : int".equals(ci.getLabel())).findAny();
            assertTrue(lengthItem.isPresent());
            assertEquals(InsertTextFormat.PlainText, lengthItem.get().getInsertTextFormat());
            assertEquals("length()", lengthItem.get().getInsertText());
            Optional<CompletionItem> substringItem = completion.getRight().getItems().stream().filter(ci -> ci.getLabel().startsWith("substring(") && ci.getLabel().contains(",")).findAny();
            assertTrue(substringItem.isPresent());
            assertEquals(InsertTextFormat.PlainText, substringItem.get().getInsertTextFormat());
            assertEquals("substring(", substringItem.get().getInsertText());
        }

        {
            VersionedTextDocumentIdentifier id2 = new VersionedTextDocumentIdentifier(src.toURI().toString(), 1);
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(1, 1), new Position(1, 1)), 0, "@java.lang."))));

            Position afterJavaLang = new Position(1, 1 + "@java.lang.".length());

            {
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), afterJavaLang)).get();
                assertTrue(completion.isRight());
                Optional<CompletionItem> annotationItem = completion.getRight().getItems().stream().filter(ci -> "annotation".equals(ci.getLabel())).findAny();
                assertTrue(annotationItem.isPresent());
                assertEquals("annotation", annotationItem.get().getLabel());
                assertEquals(CompletionItemKind.Folder, annotationItem.get().getKind());
            }

            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(afterJavaLang, afterJavaLang), 0, "annotation."))));

            Position afterJavaLangAnnotation = new Position(1, afterJavaLang.getCharacter() + "annotation.".length());

            {
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), afterJavaLangAnnotation)).get();
                assertTrue(completion.isRight());
                completion.getRight().getItems().stream().forEach(ci -> System.err.println(ci.getLabel()));
                Optional<CompletionItem> targetItem = completion.getRight().getItems().stream().filter(ci -> "Target".equals(ci.getLabel())).findAny();
                assertTrue(targetItem.isPresent());
                assertEquals("Target", targetItem.get().getLabel()); //TODO: insert text '('!
                assertEquals(CompletionItemKind.Interface, targetItem.get().getKind());
            }

            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id2, Arrays.asList(new TextDocumentContentChangeEvent(new Range(afterJavaLangAnnotation, afterJavaLangAnnotation), 0, "Target("))));

            Position afterTarget = new Position(1, afterJavaLangAnnotation.getCharacter() + "Target(".length());

            {
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), afterTarget)).get();
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
                Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), afterTarget)).get();
                assertTrue(completion.isRight());
                completion.getRight().getItems().stream().forEach(ci -> System.err.println(ci.getLabel()));
                Optional<CompletionItem> methodItem = completion.getRight().getItems().stream().filter(ci -> "ElementType.METHOD".equals(ci.getLabel())).findAny();
                assertTrue(methodItem.isPresent());
                assertEquals(InsertTextFormat.PlainText, methodItem.get().getInsertTextFormat());
                assertEquals("ElementType.METHOD", methodItem.get().getInsertText());
                assertEquals(0, methodItem.get().getAdditionalTextEdits().size());
            }
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
        InitializeResult result = server.initialize(initParams).get();
        indexingComplete.await();
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(src.toURI().toString(), "java", 0, code)));

        Diagnostic unresolvable = assertDiags(diags, "Error:2:8-2:12").get(0);
        List<Either<Command, CodeAction>> codeActions = server.getTextDocumentService().codeAction(new CodeActionParams(new TextDocumentIdentifier(src.toURI().toString()), unresolvable.getRange(), new CodeActionContext(Arrays.asList(unresolvable)))).get();
        assertEquals(2, codeActions.size());
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

    static {
        System.setProperty("SourcePath.no.source.filter", "true");
    }
}
