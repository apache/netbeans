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
package org.netbeans.spi.lsp.server;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.lsp.server.NbCodeLanguageClient;

/**
 * Contributes commands to the LSP client. Implementations are to be registered in the global Lookup. Each implementation must
 * declare the supported commands in advance.
 * 
 * @author sdedic
 */
public interface ClientCommandProvider {
    /**
     * Processes a command. Only commands previously reported by {@link #getCommands} are to be expected, the infrastructure dispatches
     * commands only to provider(s) that have declared support for them.
     * 
     * @param client the client which executed the command.
     * @param command the command
     * @param arguments arguments
     * @return completion handle or {@code null} to reject the command.
     */
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments);
    
    /**
     * Returns a set of supported commands. Called early during client initialization handshake to collect all supported
     * commands for the LSP client.
     * @return Set of supported commands. 
     */
    public Set<String> getCommands();
}
