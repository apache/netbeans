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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.Test;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import static org.junit.Assert.*;

/**
 *
 * @author lahvac
 */
public class TextDocumentSyncServerCapabilityHandlerTest {

    private static final String MIME_TYPE = "application/mock-txt";
    private static final List<String> eventLog = new ArrayList<>();

    @Test
    public void testOpenClose() throws Exception {
        MockServices.setServices(MimeDataProviderImpl.class, MockMimeResolver.class);

        new TextDocumentSyncServerCapabilityHandler.Init().run();

        FileObject folder = FileUtil.createMemoryFileSystem().getRoot().createFolder("myfolder");
        FileObject file = folder.createData("data.mock-txt");
        EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
        ((CloneableEditorSupport) ec).setMIMEType(MIME_TYPE);
        Document doc = ec.openDocument();
        JEditorPane pane = new JEditorPane() {
            @Override
            public boolean isFocusOwner() {
                return true;
            }
        };

        pane.setDocument(doc);

        String uri = Utils.toURI(file);

        SwingUtilities.invokeLater(() -> {
            EditorApiPackageAccessor.get().register(pane);
        });

        assertEvents("didOpen: " + uri + "/" + MIME_TYPE + "/0/");

        NbDocument.runAtomic((StyledDocument) doc, () -> {
            try {
                doc.insertString(0, "text", null);
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        });

        assertEvents("didChange: " + uri + "/1/[0:0-0:0 => text]");

        NbDocument.runAtomic((StyledDocument) doc, () -> {
            try {
                doc.remove(2, 1);
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        });

        assertEvents("didChange: " + uri + "/2/[0:2-0:3 => ]");

        assertTrue(DataObject.getRegistry().getModifiedSet().contains(DataObject.find(file)));

        //TODO: send save event:
//        LifecycleManager.getDefault().saveAll();
//
//        assertEvents("didSave: " + uri);

        SwingUtilities.invokeLater(() -> {
            EditorApiPackageAccessor.get().forceRelease(pane);
        });

        assertEvents("didClose: " + uri);

        SwingUtilities.invokeLater(() -> {
            EditorApiPackageAccessor.get().register(pane);
        });

        assertEvents("didOpen: " + uri + "/" + MIME_TYPE + "/0/tet");

        SwingUtilities.invokeLater(() -> {
            EditorApiPackageAccessor.get().forceRelease(pane);
        });

        assertEvents("didClose: " + uri);
    }

    private void assertEvents(String... events) {
        synchronized (eventLog) {
            long timeout = System.currentTimeMillis() + 10000000;

            while (System.currentTimeMillis() < timeout && eventLog.size() < events.length) {
                try {
                    eventLog.wait(timeout - System.currentTimeMillis());
                } catch (InterruptedException ex) {
                }
            }
            assertEquals(Arrays.asList(events), eventLog);
            eventLog.clear();
        }
    }

    public static final class MimeDataProviderImpl implements MimeDataProvider {
        @Override
        public Lookup getLookup(MimePath mp) {
            assertEquals("application/mock-txt", mp.getPath());
            return Lookups.fixed(new MockLSP(), new NbEditorKit() {
                @Override
                public String getContentType() {
                    return "application/mock-txt";
                }
            });
        }
    }

    public static final class MockLSP implements LanguageServerProvider {
        @Override
        public LanguageServerProvider.LanguageServerDescription startServer(Lookup lookup) {
            try {
                final MockProcess process = new MockProcess();
                ServerSocket srv = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                Thread serverThread = new Thread(() -> {
                    try {
                        Socket server = srv.accept();

                        LSPLauncher.createServerLauncher(new TestLanguageServer(), server.getInputStream(), server.getOutputStream()).startListening().get();
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                });
                serverThread.start();
                Socket client = new Socket(srv.getInetAddress(), srv.getLocalPort());

                return LanguageServerProvider.LanguageServerDescription.create(client.getInputStream(), client.getOutputStream(), process);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    public final static class MockMimeResolver extends MIMEResolver {

        public MockMimeResolver() {
        }

        @Override
        public String findMIMEType(FileObject fo) {
            return fo.hasExt("mock-txt") ? "application/mock-txt" : null;
        }
    }

    static final class MockProcess extends Process {
        final ByteArrayInputStream in;
        final ByteArrayOutputStream out;

        public MockProcess() {
            this.in = new ByteArrayInputStream(new byte[0]);
            this.out = new ByteArrayOutputStream();
        }

        @Override
        public OutputStream getOutputStream() {
            return out;
        }

        @Override
        public InputStream getInputStream() {
            return in;
        }

        @Override
        public InputStream getErrorStream() {
            return in;
        }

        @Override
        public int waitFor() throws InterruptedException {
            throw new InterruptedException();
        }

        @Override
        public boolean isAlive() {
            return true;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }
    }

    private static final class TestLanguageServer implements LanguageServer {

        @Override
        public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
            ServerCapabilities caps = new ServerCapabilities();
            caps.setTextDocumentSync(TextDocumentSyncKind.Incremental);
            InitializeResult initResult = new InitializeResult(caps);
            return CompletableFuture.completedFuture(initResult);
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
            return new TextDocumentService() {
                @Override
                public void didOpen(DidOpenTextDocumentParams params) {
                    TextDocumentItem td = params.getTextDocument();
                    synchronized (eventLog) {
                        eventLog.add("didOpen: " + td.getUri() + "/" + td.getLanguageId() + "/" + td.getVersion() + "/" + td.getText());
                        eventLog.notifyAll();
                    }
                }

                @Override
                public void didChange(DidChangeTextDocumentParams params) {
                    VersionedTextDocumentIdentifier td = params.getTextDocument();
                    List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
                    synchronized (eventLog) {
                        eventLog.add("didChange: " + td.getUri() + "/" + td.getVersion() + "/" + changes.stream().map(c -> range2String(c.getRange()) + " => " + c.getText()).collect(Collectors.joining(", ", "[", "]")));
                        eventLog.notifyAll();
                    }
                }

                @Override
                public void didClose(DidCloseTextDocumentParams params) {
                    TextDocumentIdentifier td = params.getTextDocument();
                    synchronized (eventLog) {
                        eventLog.add("didClose: " + td.getUri());
                        eventLog.notifyAll();
                    }
                }

                @Override
                public void didSave(DidSaveTextDocumentParams params) {
                    TextDocumentIdentifier td = params.getTextDocument();
                    synchronized (eventLog) {
                        eventLog.add("didSave: " + td.getUri() + "/" + params.getText());
                        eventLog.notifyAll();
                    }
                }
            };
        }

        @Override
        public WorkspaceService getWorkspaceService() {
            return new WorkspaceService() {
                @Override
                public void didChangeConfiguration(DidChangeConfigurationParams params) {
                    throw new IllegalStateException("Should not be called.");
                }

                @Override
                public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
                    throw new IllegalStateException("Should not be called.");
                }
            };
        }

    }

    private static String range2String(Range range) {
        return       range.getStart().getLine() + ":" + range.getStart().getCharacter() +
               "-" + range.getEnd().getLine() + ":" + range.getEnd().getCharacter();
    }
}
