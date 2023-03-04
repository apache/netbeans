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
package org.netbeans.tax;

import java.beans.PropertyChangeListener;

import org.netbeans.tax.event.TreeEventManager;
import org.netbeans.tax.event.TreeEvent;

/**
 * Tree node adds notion of owner document.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class TreeNode extends TreeObject {

    /** */
    public static final String PROP_NODE = "this"; // NOI18N


    //
    // init
    //

    /** Creates new TreeNode. */
    protected TreeNode () {
    }


    /**
     * Creates new TreeNode - copy constructor.
     */
    protected TreeNode (TreeNode node) {
        super (node);
    }
    
    
    //
    // itself
    //
    
    /**
     * Traverse to the owner document and return it.
     */
    public abstract TreeDocumentRoot getOwnerDocument ();
    
    
    //
    // Event support
    //
    
    /** Get assigned event manager assigned to ownerDocument.
     * If this node does not have its one, it returns null;
     * @return assigned event manager (may be null).
     */
    public final TreeEventManager getEventManager () {
        if ( getOwnerDocument () == null ) {
            return null;
        }
        return getOwnerDocument ().getRootEventManager ();
    }
    
}
