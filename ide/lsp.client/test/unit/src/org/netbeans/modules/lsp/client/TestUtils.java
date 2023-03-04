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
package org.netbeans.modules.lsp.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import static org.junit.Assert.assertTrue;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class TestUtils {

    public static final String MIME_TYPE = "application/mock-txt";
    public static final String TEXT_PLAIN = "text/plain";
   
    public static final class MimeDataProviderImpl implements MimeDataProvider {
        @Override
        public Lookup getLookup(MimePath mp) {
            switch (mp.getPath()) {
                case MIME_TYPE:
                    return Lookups.fixed(new MockLSP(), new NbEditorKit() {
                        @Override
                        public String getContentType() {
                            return MIME_TYPE;
                        }
                    });
                case TEXT_PLAIN:
                    return Lookups.fixed(new NbEditorKit() {
                        @Override
                        public String getContentType() {
                            return TEXT_PLAIN;
                        }
                    });
                case "": return Lookup.EMPTY;
                default: throw new AssertionError(mp.getPath());
            }
        }
    }

    public static class MockLSP implements LanguageServerProvider {
        public static Supplier<LanguageServer> createServer = () -> { throw new UnsupportedOperationException(); };
        @Override
        public LanguageServerProvider.LanguageServerDescription startServer(Lookup lookup) {
            try {
                final MockProcess process = new MockProcess();
                ServerSocket srv = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                Thread serverThread = new Thread(() -> {
                    try {
                        Socket server = srv.accept();

                        LSPLauncher.createServerLauncher(createServer.get(), server.getInputStream(), server.getOutputStream()).startListening().get();
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

    public static final class MockMimeResolver extends MIMEResolver {

        public MockMimeResolver() {
        }

        @Override
        public String findMIMEType(FileObject fo) {
            switch (fo.getExt()) {
                case "mock-txt": return MIME_TYPE;
                case "txt": return TEXT_PLAIN;
                default: return null;
            }
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
    
    public static class BaseTextDocumentServiceImpl implements TextDocumentService {
        @Override
        public void didOpen(DidOpenTextDocumentParams params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void didChange(DidChangeTextDocumentParams params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void didClose(DidCloseTextDocumentParams params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void didSave(DidSaveTextDocumentParams params) {
            throw new UnsupportedOperationException();
        }
    }

    public static class BaseWorkspaceServiceImpl implements WorkspaceService {

        @Override
        public void didChangeConfiguration(DidChangeConfigurationParams params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
            throw new UnsupportedOperationException();
        }
    }

}
