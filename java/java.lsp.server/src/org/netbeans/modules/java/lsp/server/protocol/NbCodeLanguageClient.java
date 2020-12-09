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
package org.netbeans.modules.java.lsp.server.protocol;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * An extension to the standard LanguageClient that adds several messages missing
 * from the official LSP protocol.s
 * @author sdedic
 */
public interface NbCodeLanguageClient extends LanguageClient {
    
    /**
     * Shows a message in the status bar. Log- and Info-type messages are shown "as is".
     * The other message types can be decorated by an icon according to {@link MessageParams#getType}.
     * The message will be hidden after specified number of milliseconds; 0 means the client
     * controls when the message is hidden.
     * 
     * @param params message type and text.
     */
    @JsonNotification("window/showStatusBarMessage")
    public void showStatusBarMessage(@NonNull ShowStatusMessageParams params);
    
    /**
     * Returns extended code capabilities.
     * @return code capabilities.
     */
    public NbCodeClientCapabilities getNbCodeCapabilities();
    
    public default boolean isRequestDispatcherThread() {
        return Boolean.TRUE.equals(Server.DISPATCHERS.get());
    }
}
