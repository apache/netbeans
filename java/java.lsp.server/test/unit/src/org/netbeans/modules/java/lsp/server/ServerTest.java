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
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class ServerTest extends NbTestCase {
    
    public ServerTest(String name) {
        super(name);
    }
    
    @Test
    public void testMain() throws Exception {
        File src = new File(getDataDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code = "public class Test { int i = \"\".hashCode(); }";
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        ServerSocket srv = new ServerSocket(0);
        new Thread(() -> {
            try {
                Socket server = srv.accept();
                Server.run(server.getInputStream(), server.getOutputStream());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }).start();
        Socket client = new Socket(InetAddress.getLocalHost(), srv.getLocalPort());
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
        assertDiags(diags);
        int hashCodeStart = code.indexOf("hashCode");
        Either<List<CompletionItem>, CompletionList> completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(0, hashCodeStart + 2))).get();
        assertTrue(completion.isRight());
        List<String> actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Method:hashCode"), actualItems);
        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(1);
        id.setUri(src.toURI().toString());
        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(id, Arrays.asList(new TextDocumentContentChangeEvent(new Range(new Position(0, hashCodeStart), new Position(0, hashCodeStart + "hashCode".length())), "hashCode".length(), "equ"))));
        assertDiags(diags, "Error:0:31-0:34");
        completion = server.getTextDocumentService().completion(new CompletionParams(new TextDocumentIdentifier(src.toURI().toString()), new Position(0, hashCodeStart + 2))).get();
        actualItems = completion.getRight().getItems().stream().map(ci -> ci.getKind() + ":" + ci.getLabel()).collect(Collectors.toList());
        assertEquals(Arrays.asList("Method:equals", "Method:equalsIgnoreCase"), actualItems);
    }

    private void assertDiags(List<Diagnostic>[] diags, String... expected) {
        synchronized (diags) {
            while (diags[0] == null) {
                try {
                    diags.wait();
                } catch (InterruptedException ex) {
                    //ignore
                }
            }
            List<String> actualDiags = diags[0].stream()
                                               .map(d -> d.getSeverity() + ":" + 
                                                         d.getRange().getStart().getLine() + ":" + d.getRange().getStart().getCharacter() + "-" +
                                                         d.getRange().getEnd().getLine() + ":" + d.getRange().getEnd().getCharacter())
                                               .collect(Collectors.toList());
            assertEquals(Arrays.asList(expected), actualDiags);
            diags[0] = null;
        }
    }
}
