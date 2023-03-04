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
package org.netbeans.modules.refactoring.api.ui;

import java.awt.datatransfer.Transferable;
import org.openide.nodes.Node;

/**
 * ExplorerContext is refactoring specific context, which is passed via Lookup
 * to ContextAwareAction in following cases:
 * <ol>
 *   <li>
 *     Instant rename in Explorer. Use {@link ExplorerContext#getNewName()} to 
 *     get a new name.
 *   </li>
 *   <li>
 *     Nodes are transfered using DnD. Use 
 *     {@link ExplorerContext#getTargetNode()} and 
 *     {@link ExplorerContext#getTransferable()}.
 *   </li>
 *   <li>
 *     Nodes are moved using Cut/Paste. Use 
 *     {@link ExplorerContext#getTargetNode()} and 
 *     {@link ExplorerContext#getTransferable()}.
 *   </li>
 *   <li>
 *     Nodes are copied using Copy/Paste. Use 
 *     {@link ExplorerContext#getTargetNode()} and 
 *     {@link ExplorerContext#getTransferable()}.
 *   </li>
 *   <li>
 *     Nodes are deleted from Explorer. Use {@link ExplorerContext#isDelete()}
 *   </li>
 * </ol>
 * 
 * @author Jan Becicka
 */
public final class ExplorerContext {
    private Node targetNode;
    private Transferable transferable;
    private String newName;
    private boolean isDelete;
    
    /**
     * @return target node for move and copy operations
     */
    public Node getTargetNode() {
        return targetNode;
    }

    /**
     * Setter for delete property
     * @param isDelete 
     */
    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * @return true if delete was performed
     */
    public boolean isDelete() {
        return isDelete;
    }

    /**
     * Setter fot targetNode property
     * @param targetNode 
     */
    public void setTargetNode(Node targetNode) {
        this.targetNode = targetNode;
    }

    /**
     * 
     * @return Transferable if move or copy is performed
     */
    public Transferable getTransferable() {
        return transferable;
    }

    /**
     * Setter for transferable property
     * @param transferable 
     */
    public void setTransferable(Transferable transferable) {
        this.transferable = transferable;
    }
    
    /**
     * @return newName entered by user during inplace rename
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Setter for newName property
     * @param newName 
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }
    
}
