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
package org.netbeans.modules.java.lsp.server;

import java.util.concurrent.Future;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.java.lsp.server.debugging.NbProtocolServer;
import org.netbeans.modules.java.lsp.server.protocol.NbLspServer;

import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 * A session that provides associated LSP and DAP servers.
 *
 * @author martin
 */
public final class LspSession {

    private final ProxyLookup.Controller lspServices = new ProxyLookup.Controller();
    private final ProxyLookup.Controller dapServices = new ProxyLookup.Controller();
    private final Lookup sessionLookup;

    private volatile NbLspServer lspServer;
    private volatile NbProtocolServer dapServer;

    LspSession() {
        sessionLookup = new ProxyLookup(
                new ProxyLookup(lspServices),
                new ProxyLookup(dapServices),
                Lookup.getDefault()
        );
    }

    /**
     * Set the launched LSP server.
     */
    void setLspServer(NbLspServer lspServer) {
        setServerLookup(lspServer, lspServices);
        this.lspServer = lspServer;
    }

    /**
     * Set the launched DAP server.
     */
    void setDapServer(NbProtocolServer dapServer) {
        setServerLookup(dapServer, dapServices);
        this.dapServer = dapServer;
    }

    private static void setServerLookup(ScheduledServer server, ProxyLookup.Controller lookupControler) {
        if (server == null) {
            lookupControler.setLookups();
        } else {
            Lookup l = server.getServerLookup();
            if (l != null) {
                lookupControler.setLookups(l);
            }
        }
    }

    /**
     * Get the session's LSP server, if any.
     *
     * @return the LSP server, or <code>null</code>
     */
    @CheckForNull
    public NbLspServer getLspServer() {
        return this.lspServer;
    }

    /**
     * Get the session lookup.
     */
    public Lookup getLookup() {
        return sessionLookup;
    }

    /**
     * Get the session's DAP server, if any.
     *
     * @return the DAP server, or <code>null</code>
     */
    @CheckForNull
    public NbProtocolServer getDapServer() {
        return this.dapServer;
    }

    /**
     * A base interface of the LSP/DAP server.
     */
    public interface ScheduledServer {

        /**
         * A future that represents a running server. The future finishes when
         * the server finishes.
         */
        Future<Void> getRunningFuture();

        default Lookup getServerLookup() {
            return null;
        }
    }
}
