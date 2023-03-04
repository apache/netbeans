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
import org.netbeans.tax.NotSupportedException;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeWalker {

    /** Root node of this iterator. */
    private TreeNode root;

    /** Determines which node types are presented. */
    private int whatToShow;

    /** Filter to screen nodes. */
    private TreeNodeFilter filter;

    /** Current node. */
    private TreeNode currentNode;


    //
    // init
    //

    /** Creates new TreeNodeIterator. */
    public TreeNodeWalker (TreeNode node, int wTS, TreeNodeFilter f) {
        root = node;
        whatToShow = wTS;
        filter = f;
        
        currentNode = root;
    }
    
    
    //
    // itself
    //
    
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
    public TreeNode getCurrentNode () {
        return currentNode;
    }
    
    /**
     */
    public void setCurrentNode (TreeNode curNode) throws NotSupportedException {
        if (curNode == null) {
            throw new NotSupportedException (Util.THIS.getString ("EXC_invalid_current_node_value"));
        }
        
        currentNode = curNode;
    }
    
    /**
     */
    public TreeNode parentNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.parentNode ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode firstChild () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.firstChild ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode lastChild () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.lastChild ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode previousSibling () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.previousSibling ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode nextSibling () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.nextSibling ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode previousNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.previousNode ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode nextNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.nextNode ()"); // NOI18N
        
        return null;
    }
    
}
