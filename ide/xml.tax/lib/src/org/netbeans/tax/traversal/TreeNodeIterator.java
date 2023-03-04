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
package org.netbeans.tax.traversal;

import org.netbeans.tax.TreeNode;
import org.netbeans.tax.InvalidStateException;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeIterator {
    /** Root node of this iterator. */
    private TreeNode root;

    /** Determines which node types are presented. */
    private int whatToShow;

    /** Filter to screen nodes. */
    private TreeNodeFilter filter;

    /** State of iterator. */
    private boolean valid;


    /** Creates new TreeNodeIterator.
     * @param node root node
     * @param wTS what to show
     * @param f used filter
     */
    public TreeNodeIterator (TreeNode node, int wTS, TreeNodeFilter f) {
        root = node;
        whatToShow = wTS;
        filter = f;
        
        valid = true;
    }
    
    
    /**
     */
    public TreeNode getRoot () {
        return root;
    }
    
    /**
     */
    public int getWhatToShow () {
        return whatToShow;
    }
    
    /**
     */
    public TreeNodeFilter getFilter () {
        return filter;
    }
    
    /**
     */
    public TreeNode nextNode () throws InvalidStateException {
        if (!!! valid) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_TreeNodeIterator.nextNode"));
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }
    
    /**
     */
    public TreeNode previousNode () throws InvalidStateException {
        if (!!! valid) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_TreeNodeIterator.previousNode"));
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeIterator.nextNode ()"); // NOI18N
        return null;
    }
    
    /**
     */
    public void detach () {
        valid = false;
    }
    
}
