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
package org.netbeans.modules.java.lsp.server.explorer.api;

import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Parameters for server > client notification about a changed node.
 * For nodes/nodeChanged notification. See {@link TreeViewService}.
 * @author sdedic
 */
public class NodeChangedParams {
    @NonNull
    private int rootId;
    
    private Integer nodeId;
    
    public NodeChangedParams(int rootId) {
        this.rootId = rootId;
        this.nodeId = null;
    }

    public NodeChangedParams(int rootId, int nodeId) {
        this.rootId = rootId;
        this.nodeId = nodeId;
    }

    @Pure
    @NonNull
    public int getRootId() {
        return rootId;
    }

    @Pure
    public Integer getNodeId() {
        return nodeId;
    }

    // needed for testing, as GSON deserializes the structure on the client side.
    public NodeChangedParams() {
    }

    public void setRootId(int rootId) {
        this.rootId = rootId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }
}
