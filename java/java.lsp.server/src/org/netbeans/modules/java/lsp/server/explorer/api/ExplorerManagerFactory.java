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
package org.netbeans.modules.java.lsp.server.explorer.api;

import java.util.concurrent.CompletionStage;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;

/**
 * Factory that produces an ExplorerManager that serves node in a specified tree structure. The
 * Factory has to be registered as a named service on path {@code Explorers/&lt;id>}; first registration
 * wins.
 * <p>
 * The interface is to be registered as a <b>lookup service</b> on path <code>Explorers/&lt;id></code> where 
 * <b>id</b> is a string unique identifier of the explorer view. A few of them will be published & documented
 * in various APIs as the interface matures. Currently only the LSP client is able to obtain data for a named
 * explorer view, but a general API to display an explorer will be provided. See example registration in 
 * {@link ProjectExplorer}. An annotation to register an explorer view may be created in the future.
 * <p>
 * In order for the LSP client to obtain a new view, it needs to issue a request for <code>nodes/explorerManager</code> path.
 * The message interface is {@link TreeViewService}.
 * <p>
 * Note: this interface will become an API independent of LSP.
 * @author sdedic
 */
public interface ExplorerManagerFactory {
    /**
     * Returns an ExplorerManager instance for the tree type. May return {@code null} to indicate the 
     * manager cannot be created or the tree type is not supported by the factory. Specific creation errors
     * may be indicated by the failed CompletionStage.
     * @param id tree type.
     * @param context context passed to supplemental services.
     * @return handle to explorer manager creation process.
     */
    public CompletionStage<ExplorerManager> createManager(String id, Lookup context);
}
