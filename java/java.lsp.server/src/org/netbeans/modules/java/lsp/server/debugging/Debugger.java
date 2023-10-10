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
package org.netbeans.modules.java.lsp.server.debugging;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.netbeans.modules.java.lsp.server.LspSession;
import org.netbeans.modules.java.lsp.server.progress.OperationContext;

import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author lahvac
 */
public final class Debugger {

    private Debugger() {
    }

    public static NbProtocolServer startDebugger(Pair<InputStream, OutputStream> io, LspSession session) {
        final DebugAdapterContext context = new DebugAdapterContext(session);
        NbProtocolServer server = new NbProtocolServer(context);

        Launcher<IDebugProtocolClient> serverLauncher = DSPLauncher.createServerLauncher(
                server, io.first(), io.second(), null, (d) -> new ConsumeWithLookup(d, session));
        context.setClient(serverLauncher.getRemoteProxy());
        Future<Void> runningServer = serverLauncher.startListening();
        server.setRunningFuture(runningServer);
        return server;
    }

    private static class ConsumeWithLookup implements MessageConsumer {
        private final MessageConsumer delegate;
        private final LspSession lspSession;
        private OperationContext topContext;
        private Lookup debugSessionLookup;

        public ConsumeWithLookup(MessageConsumer delegate, LspSession session) {
            this.delegate = delegate;
            this.lspSession = session;
        }
        
        @Override
        public void consume(Message message) throws MessageIssueException, JsonRpcException {
            InstanceContent ic = new InstanceContent();
            Lookup ll = debugSessionLookup;
            final OperationContext ctx;
            if (ll == null) {
                ll = new ProxyLookup(new AbstractLookup(ic), lspSession.getLookup());
                synchronized (this) {
                    if (debugSessionLookup == null) {
                        debugSessionLookup = ll;
                    }
                }
                // HACK: piggyback on LSP's client.
            }
            if (topContext == null) {
                topContext = OperationContext.find(lspSession.getLookup());
            }
            if (topContext != null) {
                ctx = topContext.operationContext();
                ctx.disableCancels();
                ic.add(ctx);
            } else {
                ctx = null;
            }
            Lookups.executeWith(ll, () -> {
                try {
                    delegate.consume(message);
                } finally {
                    if (ctx != null) {
                        ctx.stop();
                    }
                }
            });
        }
    }
}
