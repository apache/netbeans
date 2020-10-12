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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public final class Debugger {

    private Debugger() {
    }

    public static void startDebugger(InputStream in, OutputStream out) {
        final DebugAdapterContext context = new DebugAdapterContext();
        NbProtocolServer server = new NbProtocolServer(context);
        Launcher<IDebugProtocolClient> serverLauncher = DSPLauncher.createServerLauncher(server, in, out);
        context.setClient(serverLauncher.getRemoteProxy());
        Future<Void> runningServer = serverLauncher.startListening();
        try {
            runningServer.get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
