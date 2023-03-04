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
package org.netbeans.swing.outline;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;

/** A trivial extension to TreeWillExpandListener, to allow listeners to be
 * notified if another TreeWillExpandListener vetoes a pending expansion.
 * If a TreeExpansionListener added to an instance of TreePathSupport implements
 * this interface, it will be notified by the TreePathSupport if some other
 * listener vetoes expanding a node.
 * <p>
 * This interface is primarily used to avoid memory leaks if a TreeWillExpandListener
 * constructs some data structure (like a TableModelEvent that is a translation
 * of a TreeExpansionEvent) for use when the expansion actually occurs, to notify
 * it that the pending TableModelEvent will never be fired.  It is not of much
 * interest to the rest of the world.
 *
 * @author  Tim Boudreau
 */
public interface ExtTreeWillExpandListener extends TreeWillExpandListener {

    /**
     * Called when another listener vetoes a pending expansion.
     * @param event The vetoed event
     * @param exception The veto exception.
     */
    public void treeExpansionVetoed (TreeExpansionEvent event, 
        ExpandVetoException exception);
    
}
