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
package org.netbeans.modules.java.lsp.server.protocol;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider;
import org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider.LspIO;
import org.netbeans.modules.nbcode.integration.LspInputOutputProvider;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class WorkspaceContextTest {

    public WorkspaceContextTest() {
    }

    @Test
    public void testPrintLinesNoNewLines() {
        List<MessageParams> msgs = new ArrayList<>();
        MockLanguageClient mlc = new MockLanguageClient(msgs);
        WorkspaceIOContext wc = new WorkspaceIOContext() {
            @Override
            protected LanguageClient client() {
                return mlc;
            }
        };

        wc.stdOut("ahoj");
        wc.stdOut("\n");
        wc.stdErr("there!");
        wc.stdErr("\n");

        assertEquals("Two messages", 2, msgs.size());

        assertEquals("ahoj", msgs.get(0).getMessage());
        assertEquals("there!", msgs.get(1).getMessage());
    }
    
    /**
     * WorkspaceIOContext is a dead input, but must allow to be close()d, returning -1
     * from its read().
     */
    @Test
    public void testReadDoesntBlockClose() throws Exception {
        List<MessageParams> msgs = new ArrayList<>();
        MockLanguageClient mlc = new MockLanguageClient(msgs);
        WorkspaceIOContext wc = new WorkspaceIOContext() {
            @Override
            protected LanguageClient client() {
                return mlc;
            }
        };
        
        //LspIO io = LspIOAccessor.createIO("Test", wc, Lookup.EMPTY);
        MockLookup.setInstances(wc);
        AbstractLspInputOutputProvider ioProvider = new LspInputOutputProvider();
        LspIO lspIo = ioProvider.getIO("Test", true, Lookup.EMPTY);

        Reader inReader = ioProvider.getIn(lspIo); 
        CountDownLatch closeLatch = new CountDownLatch(1);
        final Thread readerThread = Thread.currentThread();
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            inReader.close();
            closeLatch.countDown();
            return null;
        }, 300, TimeUnit.MILLISECONDS);
        
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            readerThread.interrupt();
        }, 1000, TimeUnit.MILLISECONDS);
        int r = inReader.read();
        
        assert r == -1;
        assertTrue(closeLatch.await(500, TimeUnit.MILLISECONDS));
    }

    private static final class MockLanguageClient implements LanguageClient {
        private final List<MessageParams> messages;

        MockLanguageClient(List<MessageParams> messages) {
            this.messages = messages;
        }

        @Override
        public void telemetryEvent(Object object) {
            fail();
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            fail();
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            fail();
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            fail();
            return null;
        }

        @Override
        public void logMessage(MessageParams message) {
            messages.add(message);
        }
    }
}
