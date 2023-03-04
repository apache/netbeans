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
package org.netbeans.modules.java.lsp.server.debugging.launch;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;

/**
 *
 * @author martin
 */
public final class NbDisconnectRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(NbDisconnectRequestHandler.class.getName());

    public CompletableFuture<Void> disconnect(DisconnectArguments arguments, DebugAdapterContext context) {
        destroyDebugSession(arguments, context);
        context.getBreakpointManager().disposeBreakpoints();
        return CompletableFuture.completedFuture(null);
    }

    private void destroyDebugSession(DisconnectArguments arguments, DebugAdapterContext context) {
        NbDebugSession debugSession = context.getDebugSession();
        if (debugSession != null) {
            if (Boolean.TRUE.equals(arguments.getTerminateDebuggee()) && !context.isAttached()) {
                debugSession.terminate();
            } else {
                debugSession.detach();
            }
        } else {
            context.requestProcessTermination();
        }
    }

}
